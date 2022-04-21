package com.pda.uhf_g.ui.fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pda.serialport.Tools;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.gg.reader.api.protocol.gx.LogBaseGJbInfo;
import com.gg.reader.api.protocol.gx.LogBaseGbInfo;
import com.handheld.uhfr.UHFRManager;
import com.pda.uhf_g.MainActivity;
import com.pda.uhf_g.R;
import com.pda.uhf_g.adapter.EPCListViewAdapter;
import com.pda.uhf_g.adapter.RecycleViewAdapter;
import com.pda.uhf_g.entity.TagInfo;
import com.pda.uhf_g.ui.base.BaseFragment;
import com.pda.uhf_g.util.CheckCommunication;
import com.pda.uhf_g.util.ExcelUtil;
import com.pda.uhf_g.util.GlobalClient;
import com.pda.uhf_g.util.LogUtil;
import com.pda.uhf_g.util.UtilSound;
import com.uhf.api.cls.Reader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class InventoryFragment extends BaseFragment {


    @BindView(R.id.textView_all_tags)
    TextView tvAllTag ;
    @BindView(R.id.textView_speed)
    TextView tvSpeed ;
    @BindView(R.id.textView_readCount)
    TextView tvReadCount ;
    @BindView(R.id.textView_timeCount)
    TextView tvTime ;
    @BindView(R.id.button_inventory)
    Button btnInventory ;
    @BindView(R.id.button_cus_read)
    Button btnCusRead ;
    @BindView(R.id.button_excel)
    Button btnExcel ;
    @BindView(R.id.button_clean)
    Button btnClean ;


    @BindView(R.id.checkbox_multi_tag)
    CheckBox checkBoxMultiTag ;
    @BindView(R.id.checkbox_tid)
    CheckBox checkBoxTid ;
    @BindView(R.id.checkbox_loop)
    CheckBox checkBoxLoop ;
    @BindView(R.id.listview_epc)
    ListView listViewEPC ;

    private EPCListViewAdapter epcListViewAdapter ;
    private Map<String, TagInfo> tagInfoMap = new LinkedHashMap<String, TagInfo>();//
    private List<TagInfo> tagInfoList = new ArrayList<TagInfo>();//
    private MainActivity mainActivity ;
    private int isCtesius = 0;
    private Long index = 1l;//
    private Handler mHandler = new Handler();
    private Handler soundHandler = new Handler();
    private Runnable timeTask = null;
    private Runnable soundTask = null ;
    private int time = 0;
    private boolean isReader = false;
    private Timer timer ;
    private boolean isSound = true;
    private boolean[] isChecked = new boolean[]{false, false, false};//
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private KeyReceiver keyReceiver;

    private boolean isMulti = false;// multi mode flag

    public UHFRManager mUhfrManager;//uhf

    private final int MSG_INVENROTY = 1 ;

    private final int MSG_INVENROTY_TIME = 1001 ;

    private long lastCount = 0 ;//
    private long speed = 0 ;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_INVENROTY:

                    //
                    tvReadCount.setText("" + getReadCount(tagInfoList));
                    //
                    tvAllTag.setText("" + tagInfoList.size());
                    long currentCount =  getReadCount(tagInfoList);
                    speed = currentCount - lastCount ;
                    if(speed >= 0){
                        lastCount = currentCount ;
                        tvSpeed.setText(speed + "");
                    }
                    break ;

                case MSG_INVENROTY_TIME:
                    //
                    time++ ;
                    tvTime.setText(secToTime(time) + "s" );
                    break ;
            }
        }
    };



    //
    private void initPane() {
        index = 1l;
        time = 0;
        lastCount = 0 ;
        tagInfoMap.clear();
        tagInfoList.clear();
        epcListViewAdapter.notifyDataSetChanged();
        tvAllTag.setText(0 + "");
        tvReadCount.setText(0 + "");
        tvTime.setText("00:00:00" + " s");
        tvSpeed.setText(0 + "");

    }



    //
    private long getReadCount(List<TagInfo> tagInfoList) {
        long readCount = 0;
        for (int i = 0; i < tagInfoList.size(); i++) {
            readCount += tagInfoList.get(i).getCount();
        }
        return readCount;
    }
    public InventoryFragment() {

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("pang", "onResume()");
        mUhfrManager.setCancleInventoryFilter();
        //
        registerKeyCodeReceiver();
        //getModuleInfo() ;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("pang", "onPause()");
        stopInventory();
        //
        mainActivity.unregisterReceiver(keyReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    private Runnable invenrotyThread = new Runnable() {
        @Override
        public void run() {
            LogUtil.e("invenrotyThread is running");
            List<Reader.TAGINFO> listTag = null;
            //6C
                //
                if (isMulti) {
                    listTag = mUhfrManager.tagInventoryRealTime();
                }else{
                    if (checkBoxTid.isChecked()) {
                        //
                        listTag = mUhfrManager.tagEpcTidInventoryByTimer((short) 50) ;
                    }else{
                        listTag = mUhfrManager.tagInventoryByTimer((short) 50); ;
                    }
                }
            if (listTag == null) {
                LogUtil.e("listTag = null");
                //
                if(checkBoxMultiTag.isChecked()){
                    mUhfrManager.asyncStopReading();
                    mUhfrManager.asyncStartReading();
                }
            }
                //
                if (listTag != null && !listTag.isEmpty()) {
                    LogUtil.e("inventory listTag size = " + listTag.size());
//                    if (!checkBoxLoop.isChecked()) {
                        //
                        UtilSound.play(1, 0);
//                    }
                    for (Reader.TAGINFO taginfo : listTag) {

                        //
                        Map<String, TagInfo> infoMap = pooled6cData(taginfo);
                        tagInfoList.clear();
                        tagInfoList.addAll(infoMap.values());
                        //
                        mainActivity.listEPC.clear();
                        mainActivity.listEPC.addAll(infoMap.keySet());

                    }
                    epcListViewAdapter.notifyDataSetChanged();
                    handler.sendEmptyMessage(MSG_INVENROTY);

                }else{
                    //
                    speed = 0 ;
                }
            //
            if (checkBoxLoop.isChecked()) {
                handler.postDelayed(invenrotyThread, 0) ;
            }else{
                if(timer != null){
                    timer.cancel();
                    timer = null ;
                }
                handler.sendEmptyMessage(MSG_INVENROTY_TIME);
                isReader = false ;
            }

        }
    } ;





    public Map<String, TagInfo> pooled6cData(Reader.TAGINFO info) {
        String epcAndTid = Tools.Bytes2HexString(info.EpcId, info.EpcId.length)
                 ;
        if (tagInfoMap.containsKey(epcAndTid)) {
            TagInfo tagInfo = tagInfoMap.get(epcAndTid);
            Long count = tagInfo.getCount();
            count++;
            tagInfo.setRssi(info.RSSI + "");
            tagInfo.setCount(count);
            tagInfo.setIsShowTid(checkBoxTid.isChecked());
            if (info.EmbededData != null && info.EmbededDatalen > 0) {
                tagInfo.setTid(Tools.Bytes2HexString(info.EmbededData, info.EmbededDatalen));
            }
            tagInfoMap.put(epcAndTid, tagInfo);
        } else {
            TagInfo tag = new TagInfo();
            tag.setIndex(index);
            tag.setType("6C");
            tag.setEpc(Tools.Bytes2HexString(info.EpcId, info.EpcId.length));
            tag.setCount(1l);
            tag.setIsShowTid(checkBoxTid.isChecked());
            if (info.EmbededData != null && info.EmbededDatalen > 0) {
                tag.setTid(Tools.Bytes2HexString(info.EmbededData, info.EmbededDatalen));
            }
            tag.setRssi(info.RSSI + "");
            tagInfoMap.put(epcAndTid, tag);
            index++;
        }
        return tagInfoMap;
    }





    @OnClick(R.id.button_clean)
    public void clear() {
        initPane();
        mainActivity.listEPC.clear();
    }


    @OnClick(R.id.button_inventory)
    public void invenroty() {
        //操作之前判定模块是否正常初始化
        if(!mainActivity.isConnectUHF){
            showToast(R.string.communication_timeout);
            return ;
        }
        if (!isReader) {
            inventoryEPC();
        }else{
            stopInventory() ;
        }

    }


    private void setEnabled(boolean isEnable) {
        checkBoxLoop.setEnabled(isEnable);
        checkBoxLoop.setEnabled(isEnable);
        checkBoxMultiTag.setEnabled(isEnable);
        checkBoxTid.setEnabled(isEnable);
        btnExcel.setEnabled(isEnable);

    }





    private void inventoryEPC() {
        isReader = true ;
        speed = 0 ;//
        if (checkBoxLoop.isChecked()) {
            //
            btnInventory.setText(R.string.stop_inventory);
            setEnabled(false) ;
            soundTask();
        }
        showToast(R.string.start_inventory);
        //
        mUhfrManager.setGen2session(isMulti);
        if (isMulti) {
            mUhfrManager.asyncStartReading() ;

        }


        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(MSG_INVENROTY_TIME);
                }
            }, 1000, 1000);
        }

        //启动盘存线程
        handler.postDelayed(invenrotyThread, 0);
    }


    private void stopInventory()  {
        if (mainActivity.isConnectUHF) {
            if(isReader){
                if (checkBoxMultiTag.isChecked()) {
                    mUhfrManager.asyncStopReading();
                }
                handler.removeCallbacks(invenrotyThread);
                soundHandler.removeCallbacks(soundTask);
                isReader = false ;
                if (timer != null) {
                    timer.cancel();
                    timer = null ;
                }

                btnInventory.setText(R.string.start_inventory);
            }

        } else {
            showToast(R.string.communication_timeout);
        }
        isReader = false ;
        setEnabled(true) ;
    }





    //
    public String secToTime(long time) {
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        time = time * 1000;
        String hms = formatter.format(time);
        return hms;
    }



    private ArrayList<ArrayList<String>> getRecordData(List<TagInfo> infos) {
        ArrayList<ArrayList<String>> recordList = new ArrayList<>();
        for (int i = 0; i < infos.size(); i++) {
            ArrayList<String> beanList = new ArrayList<String>();
            TagInfo info = infos.get(i);
            beanList.add(info.getIndex() + "");
            beanList.add(info.getType());
            beanList.add(info.getEpc() != null ? info.getEpc() : "");
            beanList.add(info.getTid() != null ? info.getTid() : "");
            beanList.add(info.getUserData() != null ? info.getUserData() : "");
            beanList.add(info.getReservedData() != null ? info.getReservedData() : "");
            beanList.add(info.getCount() + "");
//            beanList.add(dateFormat.format(info.getReadTime()));
            recordList.add(beanList);
        }
        return recordList;
    }

    public void notifySystemToScan(String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(filePath);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        mainActivity.sendBroadcast(intent);
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

    @OnClick(R.id.button_excel)
    public void fab_excel() {
        if (!isReader) {
            List<PermissionItem> permissonItems = new ArrayList<PermissionItem>();
            permissonItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, mainActivity.getResources().getString(R.string.store), me.weyye.hipermission.R.drawable.permission_ic_storage));
            HiPermission.create(mainActivity)
                    .title(mainActivity.getResources().getString(R.string.export_excel_need_permission))
                    .permissions(permissonItems)
                    .checkMutiPermission(new PermissionCallback() {
                        @Override
                        public void onClose() {
                            Log.e("onClose", "onClose");
                        }

                        @Override
                        public void onFinish() {
                            Log.e("onFinish", "onFinish");
                            String filePath = Environment.getExternalStorageDirectory() + "/Download/";
                            String fileName = "Tag_" + dateFormat.format(new Date()) + ".xls";
                            String[] title = {"Index", "Type", "EPC", "TID", "UserData", "ReservedData", "TotalCount"};//, "ReadTime"
                            if (tagInfoList.size() > 0) {
                                try {
                                    ExcelUtil.initExcel(filePath, fileName, title);
                                    ExcelUtil.writeObjListToExcel(getRecordData(tagInfoList), filePath + fileName, this);
                                    showToast("Export success " + "Path=" + filePath + fileName);
                                    notifySystemToScan(filePath + fileName);
                                } catch (Exception ex) {
                                    showToast("Export Failed");
                                }
                            } else {
                                showToast("No Data");
                            }
                        }

                        @Override
                        public void onDeny(String permission, int position) {
                            Log.e("onDeny", "onDeny");
                        }

                        @Override
                        public void onGuarantee(String permission, int position) {
                            Log.e("onGuarantee", "onGuarantee");
                        }
                    });
        } else {
//            ToastUtils.showText(getResources().getString(R.string.read_card_being));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false) ;
        ButterKnife.bind(this, view);
        mUhfrManager = UHFRManager.getInstance();
        initView();
        LogUtil.e("onCreateView()");
        //
        UtilSound.initSoundPool(mainActivity);
        return view;
    }

    private void initView() {
        epcListViewAdapter = new EPCListViewAdapter(mainActivity, tagInfoList);
        listViewEPC.setAdapter(epcListViewAdapter);
        checkBoxMultiTag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isMulti = isChecked ;
            }
        });
    }



    private void registerKeyCodeReceiver() {
        keyReceiver = new KeyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        filter.addAction("android.intent.action.FUN_KEY");
        mainActivity.registerReceiver(keyReceiver, filter);
    }


    //
    private class KeyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyCode", 0);
            LogUtil.e("keyCode = " + keyCode);
            if (keyCode == 0) {
                keyCode = intent.getIntExtra("keycode", 0);
            }
            boolean keyDown = intent.getBooleanExtra("keydown", false);
            if (keyDown) {
//                ToastUtils.showText("KeyReceiver:keyCode = down" + keyCode);
            } else {
//                ToastUtils.showText("KeyReceiver:keyCode = up" + keyCode);
                switch (keyCode) {
                    case KeyEvent.KEYCODE_F1:
                        break;
                    case KeyEvent.KEYCODE_F2:
                        break;
                    case KeyEvent.KEYCODE_F5:
                        break;
                    case KeyEvent.KEYCODE_F3://C510x
                    case KeyEvent.KEYCODE_F4://6100
                    case KeyEvent.KEYCODE_F7://H3100
                        invenroty();
                        break;
                }
            }
        }

    }

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {

        }
    } ;

    //
    private void soundTask() {
        soundTask = new Runnable() {
            @Override
            public void run() {
//                int rateValue = 1 ;
                LogUtil.e("rateValue = " + speed);
                if (speed != 0) {
                    UtilSound.play(1, 0);
                }
                soundHandler.postDelayed(this, 40);
            }
        };
        soundHandler.postDelayed(soundTask, 0);
    }



}