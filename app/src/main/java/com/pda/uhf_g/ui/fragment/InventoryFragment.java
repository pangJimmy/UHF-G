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

/**
 * 盘存界面，用于盘存6C/6B/GB/GJB标签
 * 模块固件版本说明：X.X.1.X表示通用版本（1或者3表示通用版本支持6c/b; 5表示GJB，6表示GB，7表示GJB和GB）
 */
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
    private Map<String, TagInfo> tagInfoMap = new LinkedHashMap<String, TagInfo>();//去重数据源
    private List<TagInfo> tagInfoList = new ArrayList<TagInfo>();//适配器所需数据源
    private MainActivity mainActivity ;
    private int isCtesius = 0;
    private Long index = 1l;//索引
    private Handler mHandler = new Handler();
    private Handler soundHandler = new Handler();
    private Runnable timeTask = null;
    private Runnable soundTask = null ;
    private int time = 0;
    private boolean isReader = false;
    private Timer timer ;
    private boolean isSound = true;
    private boolean[] isChecked = new boolean[]{false, false, false};//标识读0-epc与1-user
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private KeyReceiver keyReceiver;

    private boolean isMulti = false;// multi mode flag

    public UHFRManager mUhfrManager;//uhf

    private final int MSG_INVENROTY = 1 ;

    private final int MSG_INVENROTY_TIME = 1001 ;

    private long lastCount = 0 ;//用于计算速度
    private long speed = 0 ;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_INVENROTY:

                    //总次数
                    tvReadCount.setText("" + getReadCount(tagInfoList));
                    //总标签数
                    tvAllTag.setText("" + tagInfoList.size());
                    long currentCount =  getReadCount(tagInfoList);
                    speed = currentCount - lastCount ;
                    if(speed >= 0){
                        lastCount = currentCount ;
                        tvSpeed.setText(speed + "");
                    }
                    break ;

                case MSG_INVENROTY_TIME:
                    //每秒刷新一次速度
                    time++ ;
                    tvTime.setText(secToTime(time) + "s" );
                    break ;
            }
        }
    };



    //初始化面板
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



    //获取读取总次数
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
        //注册按键
        registerKeyCodeReceiver();
        //getModuleInfo() ;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("pang", "onPause()");
        stopInventory();
        //注销按键监听
        mainActivity.unregisterReceiver(keyReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    //盘存线程
    private Runnable invenrotyThread = new Runnable() {
        @Override
        public void run() {
            LogUtil.e("invenrotyThread is running");
            List<Reader.TAGINFO> listTag = null;
            //6C标签
                //多标签盘存
                if (isMulti) {
                    listTag = mUhfrManager.tagInventoryRealTime();
                }else{
                    if (checkBoxTid.isChecked()) {
                        //盘存时带TID
                        listTag = mUhfrManager.tagEpcTidInventoryByTimer((short) 50) ;
                    }else{
                        listTag = mUhfrManager.tagInventoryByTimer((short) 50); ;
                    }
                }
            if (listTag == null) {
                LogUtil.e("listTag = null");
                //多标签状态重置
                if(checkBoxMultiTag.isChecked()){
                    mUhfrManager.asyncStopReading();
                    mUhfrManager.asyncStartReading();
                }
            }
                //盘存列表
                if (listTag != null && !listTag.isEmpty()) {
                    LogUtil.e("inventory listTag size = " + listTag.size());
//                    if (!checkBoxLoop.isChecked()) {
                        //单次盘存，不用另外线程播放声音
                        UtilSound.play(1, 0);
//                    }
                    for (Reader.TAGINFO taginfo : listTag) {

                        //去除重复的EPC号
                        Map<String, TagInfo> infoMap = pooled6cData(taginfo);
                        tagInfoList.clear();
                        tagInfoList.addAll(infoMap.values());
                        //将EPC数据作为全局变量
                        mainActivity.listEPC.clear();
                        mainActivity.listEPC.addAll(infoMap.keySet());

                    }
                    epcListViewAdapter.notifyDataSetChanged();
                    handler.sendEmptyMessage(MSG_INVENROTY);

                }else{
                    //更新速率
                    speed = 0 ;
                }
            //是否连续盘存
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




    //去重6C
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




    /***清除***/
    @OnClick(R.id.button_clean)
    public void clear() {
        initPane();
        mainActivity.listEPC.clear();
    }

    /***盘存EPC***/
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

    /**
     * 盘存期间其他勾选设置为不可用
     */
    private void setEnabled(boolean isEnable) {
        checkBoxLoop.setEnabled(isEnable);
        checkBoxLoop.setEnabled(isEnable);
        checkBoxMultiTag.setEnabled(isEnable);
        checkBoxTid.setEnabled(isEnable);
        btnExcel.setEnabled(isEnable);

    }




    /***盘存EPC***/
    private void inventoryEPC() {
        isReader = true ;
        speed = 0 ;//初始清零
        if (checkBoxLoop.isChecked()) {
            //连续盘存
            btnInventory.setText(R.string.stop_inventory);
            setEnabled(false) ;
            soundTask();
        }
        showToast(R.string.start_inventory);
        //设置盘存模式是否为多标签模式
        mUhfrManager.setGen2session(isMulti);
        if (isMulti) {
            mUhfrManager.asyncStartReading() ;

        }

        //启动计时,每秒发一次
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

    /**停止盘存**/
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





    //格式化时间
    public String secToTime(long time) {
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        time = time * 1000;
        String hms = formatter.format(time);
        return hms;
    }


    /**
     * 将数据集合 转化成ArrayList<ArrayList<String>>
     *
     * @return
     */
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
        //初始化声音池
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


    /**注册按键监听**/
    private void registerKeyCodeReceiver() {
        keyReceiver = new KeyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        filter.addAction("android.intent.action.FUN_KEY");
        mainActivity.registerReceiver(keyReceiver, filter);
    }


    //按键广播接收
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
                    case KeyEvent.KEYCODE_F3:
                        break;
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

    //提示音线程
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