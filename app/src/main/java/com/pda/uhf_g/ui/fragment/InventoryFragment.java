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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.gg.reader.api.protocol.gx.LogBaseGJbInfo;
import com.gg.reader.api.protocol.gx.LogBaseGbInfo;
import com.handheld.uhfr.UHFRManager;
import com.pda.uhf_g.MainActivity;
import com.pda.uhf_g.R;
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

/**
 * 盘存界面，用于盘存6C/6B/GB/GJB标签
 * 模块固件版本说明：X.X.1.X表示通用版本（1或者3表示通用版本支持6c/b; 5表示GJB，6表示GB，7表示GJB和GB）
 */
public class InventoryFragment extends BaseFragment {

    /**6C通用版本**/
    private final String VERSION_6C = "1";
    /**6C/6B版本**/
    private final String VERSION_6B = "3";
    /**国军标版本**/
    private final String VERSION_GJB = "5";
    /**国标版本**/
    private final String VERSION_GB = "6";
    /**国军标和国标版本**/
    private final String VERSION_GB_GJB = "7";

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

    @BindView(R.id.recycle)
    RecyclerView recyclerView ;//EPC list
    @BindView(R.id.radiogroup_type)
    RadioGroup radioGroup ;
    @BindView(R.id.type_c)
    RadioButton radioButton6C ;
    @BindView(R.id.type_b)
    RadioButton radioButton6B ;
    @BindView(R.id.type_gb)
    RadioButton radioButtonGB ;
    @BindView(R.id.type_gjb)
    RadioButton radioButtonGJB ;

    @BindView(R.id.checkbox_multi_tag)
    CheckBox checkBoxMultiTag ;
    @BindView(R.id.checkbox_tid)
    CheckBox checkBoxTid ;
    @BindView(R.id.checkbox_loop)
    CheckBox checkBoxLoop ;

    private Map<String, TagInfo> tagInfoMap = new LinkedHashMap<String, TagInfo>();//去重数据源
    private List<TagInfo> tagInfoList = new ArrayList<TagInfo>();//适配器所需数据源
    private MainActivity mainActivity ;
    private int isCtesius = 0;
    private Long index = 1l;//索引
    private Handler mHandler = new Handler();
    private Handler soundHandler = new Handler();
    private Runnable timeTask = null;
    private int time = 0;
    private boolean isReader = false;
    private boolean isSound = true;
    private RecycleViewAdapter adapter;
    private boolean[] isChecked = new boolean[]{false, false, false};//标识读0-epc与1-user
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private KeyReceiver keyReceiver;

    public UHFRManager mUhfrManager;//uhf

    private final int MSG_INVENROTY = 1 ;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_INVENROTY:
                    UtilSound.play(1, 0);
                    upDataPane();
                    break ;

            }
        }
    };



    //初始化面板
    private void initPane() {
        index = 1l;
        time = 0;
        tagInfoMap.clear();
        tagInfoList.clear();
        adapter.notifyData(tagInfoList);
        tvAllTag.setText(0 + "");
        tvReadCount.setText(0 + "");
        tvTime.setText("00:00:00" + " (s)");
        tvSpeed.setText(0 + " (t/s)");
        adapter.setThisPosition(null);

    }

    //更新面板
    private void upDataPane() {
        adapter.notifyData(tagInfoList);
        tvReadCount.setText(getReadCount(tagInfoList) + "");
        tvAllTag.setText(tagInfoList.size() + "");
    }

    //初始化RecycleView
    public void initRecycleView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mainActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        RecyclerView rv = (RecyclerView) findViewById(R.id.recycle);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(mainActivity, 1));
        adapter = new RecycleViewAdapter(tagInfoList);
        recyclerView.setAdapter(adapter);

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
        // Required empty public constructor
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
            List<LogBaseGbInfo> listGBTag ;
            List<LogBaseGJbInfo> listGJBTag ;
            //6C标签
            if(radioButton6C.isChecked()){
                //多标签盘存
                if (checkBoxMultiTag.isChecked()) {
                    listTag = mUhfrManager.tagInventoryRealTime();
                }else{
                    if (checkBoxTid.isChecked()) {
                        //盘存时带TID
                        listTag = mUhfrManager.tagEpcTidInventoryByTimer((short) 20) ;
                    }else{
                        listTag = mUhfrManager.tagInventoryByTimer((short) 20); ;
                    }
                }
                //盘存列表
                if (listTag != null && listTag.size() > 0) {

                    for (Reader.TAGINFO taginfo : listTag) {
                        //去除重复的EPC号
                        Map<String, TagInfo> infoMap = pooled6cData(taginfo);
                        tagInfoList.clear();
                        tagInfoList.addAll(infoMap.values());
                        //将EPC数据作为全局变量
                        mainActivity.listEPC.clear();
                        mainActivity.listEPC.addAll(infoMap.keySet());
//                    LogUtil.e("EPC = "  + Tools.Bytes2HexString(taginfo.EpcId, taginfo.EpcId.length) + "\n");
//                    LogUtil.e("TID = "  + Tools.Bytes2HexString(taginfo.EmbededData, taginfo.EmbededData.length));
                    }
                    handler.sendEmptyMessage(MSG_INVENROTY);
                }else{
                    LogUtil.e("listTag = null");
                    //多标签状态重置
                    if(checkBoxMultiTag.isChecked()){
                        mUhfrManager.asyncStopReading();
                        mUhfrManager.asyncStartReading();
                    }
                }
            } else if (radioButtonGB.isChecked()) {
                //国标标签
                listGBTag = mUhfrManager.inventoryGBTag(checkBoxTid.isChecked(), (short) 20);
                //盘存列表
                if (listGBTag != null && listGBTag.size() > 0) {
                    LogUtil.e("listGBTag size = "+ listGBTag.size());
                    for (LogBaseGbInfo taginfo : listGBTag) {
                        //去除重复的EPC号
                        Map<String, TagInfo> infoMap = pooledGbData(taginfo);
                        tagInfoList.clear();
                        tagInfoList.addAll(infoMap.values());
                        //将EPC数据作为全局变量
                        mainActivity.listEPC.clear();
                        mainActivity.listEPC.addAll(infoMap.keySet());
                    }
                    handler.sendEmptyMessage(MSG_INVENROTY);
                }
            }else if(radioButtonGJB.isChecked()){
                //国军标标签
                listGJBTag = mUhfrManager.inventoryGJBTag(checkBoxTid.isChecked(), (short) 20);
                //盘存列表
                if (listGJBTag != null && listGJBTag.size() > 0) {
                    LogUtil.e("listGJBTag size = "+ listGJBTag.size());
                    for (LogBaseGJbInfo taginfo : listGJBTag) {
                        //去除重复的EPC号
                        Map<String, TagInfo> infoMap = pooledGJbData(taginfo);
                        tagInfoList.clear();
                        tagInfoList.addAll(infoMap.values());
                        //将EPC数据作为全局变量
                        mainActivity.listEPC.clear();
                        mainActivity.listEPC.addAll(infoMap.keySet());
                    }
                    handler.sendEmptyMessage(MSG_INVENROTY);
                }
            }


            //是否连续盘存
            if (checkBoxLoop.isChecked()) {
                handler.postDelayed(invenrotyThread, 0) ;
            }else{
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
//
//    //去重6B
//    public Map<String, TagInfo> pooled6bData(LogBase6bInfo info) {
//        if (tagInfoMap.containsKey(info.getTid())) {
//            TagInfo tagInfo = tagInfoMap.get(info.getTid());
//            Long count = tagInfoMap.get(info.getTid()).getCount();
//            count++;
//            tagInfo.setRssi(info.getRssi() + "");
//            tagInfo.setCount(count);
//            tagInfoMap.put(info.getTid(), tagInfo);
//        } else {
//            TagInfo tag = new TagInfo();
//            tag.setIndex(index);
//            tag.setType("6B");
//            tag.setCount(1l);
//            tag.setUserData(info.getUserdata());
//            if (info.getTid() != null) {
//                tag.setTid(info.getTid());
//            }
//            tag.setRssi(info.getRssi() + "");
//            tagInfoMap.put(info.getTid(), tag);
//            index++;
//        }
////        handlerStop.sendEmptyMessage(2);
//        return tagInfoMap;
//    }
//
//    //去重GB
    public Map<String, TagInfo> pooledGbData(LogBaseGbInfo info) {
        String gbepc = info.getEpc() ;
        if (tagInfoMap.containsKey(gbepc)) {
            TagInfo tagInfo = tagInfoMap.get(gbepc);
            Long count = tagInfoMap.get(gbepc).getCount();
            count++;
            tagInfo.setRssi(info.getRssi() + "");
            tagInfo.setCount(count);
            tagInfo.setIsShowTid(checkBoxTid.isChecked());
            tagInfo.setTid(info.getTid());
            tagInfoMap.put(gbepc, tagInfo);
            LogUtil.e("count = " + count);
        } else {
            TagInfo tag = new TagInfo();
            tag.setIndex(index);
            tag.setType("GB");
            tag.setEpc(info.getEpc());
            tag.setCount(1l);
            tag.setUserData(info.getUserdata());
            tag.setIsShowTid(checkBoxTid.isChecked());
            tag.setTid(info.getTid());
            tag.setRssi(info.getRssi() + "");
            tagInfoMap.put(gbepc, tag);
            index++;
        }
//        handlerStop.sendEmptyMessage(2);
        return tagInfoMap;
    }

    //    //去重GJB
    public Map<String, TagInfo> pooledGJbData(LogBaseGJbInfo info) {
        String gbepc = info.getEpc() ;
        if (tagInfoMap.containsKey(gbepc)) {
            TagInfo tagInfo = tagInfoMap.get(gbepc);
            Long count = tagInfoMap.get(gbepc).getCount();
            count++;
            tagInfo.setRssi(info.getRssi() + "");
            tagInfo.setIsShowTid(checkBoxTid.isChecked());
            tagInfo.setCount(count);
            tagInfoMap.put(gbepc, tagInfo);
            LogUtil.e("count = " + count);
        } else {
            TagInfo tag = new TagInfo();
            tag.setIndex(index);
            tag.setType("GJB");
            tag.setEpc(info.getEpc());
            tag.setCount(1l);
            tag.setUserData(info.getUserdata());
            tag.setIsShowTid(checkBoxTid.isChecked());
            tag.setTid(info.getTid());
            tag.setRssi(info.getRssi() + "");
            tagInfoMap.put(gbepc, tag);
            index++;
        }
//        handlerStop.sendEmptyMessage(2);
        return tagInfoMap;
    }
    /**
     * 获取UHF模块版本信息，根据版本号信息，判定模块支持哪些标签
     */
    private String getBaseVersion() {
        String version = null ;
        LogUtil.e("getBaseVersion()");
        //操作之前判定模块是否正常初始化
        if(!mainActivity.isConnectUHF){
            showToast(R.string.communication_timeout);
            return version;
        }
        version = mUhfrManager.getHardware() ;
        //xl模块MODOULE_SLR1200, version 为空时通讯失败，MODOULE开头为xl模块，X.X.1.X为gx模块
        if (version != null && !version.startsWith("MODOULE")) {
            LogUtil.e("version = " + version);
            String[] arrays = version.split("\\.");
            if (arrays.length > 2) {
                /**
                 * 获取固件gx
                 * X.X.1.X表示通用版本（1或者3表示通用版本支持6c/b; 5表示GJB，6表示GB，7表示GJB和GB）
                 * baseVersions='1.1.3.15'
                 */
                version = arrays[2];
            }
        }
        return version;
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
        radioGroup.setEnabled(isEnable);
        btnExcel.setEnabled(isEnable);
        radioButton6C.setEnabled(isEnable);
        radioButton6B.setEnabled(isEnable);
        radioButtonGB.setEnabled(isEnable);
        radioButtonGJB.setEnabled(isEnable);
    }

    /***
     * 恢复设置可用
     */
//    private void setEnable() {
//        checkBoxLoop.setFocusable(true);
//        checkBoxMultiTag.setFocusable(true);
//        checkBoxTid.setFocusable(true);
//        radioGroup.setFocusable(true);
//    }


    /***盘存EPC***/
    private void inventoryEPC() {
        isReader = true ;
        //判断盘存哪一类标签
        if (radioGroup.getCheckedRadioButtonId() == R.id.type_c) {
            //盘存6C标签
            inventory6C() ;
        }else if(radioGroup.getCheckedRadioButtonId() == R.id.type_b){
            //盘存6B标签
        } else if (radioGroup.getCheckedRadioButtonId() == R.id.type_gb
                || radioGroup.getCheckedRadioButtonId() == R.id.type_gjb) {
            //盘存国标标签或者国军标
            inventoryGB();
        }
    }

    /**停止盘存**/
    private void stopInventory() {
        if (mainActivity.isConnectUHF) {
            //多标签，多标签模式默认设置Session2, Q值为4
            if (checkBoxMultiTag.isChecked()) {
                mUhfrManager.asyncStopReading();
            }
            handler.removeCallbacks(invenrotyThread);
            handler.removeCallbacks(timeTask);
            isReader = false ;
            btnInventory.setText(R.string.start_inventory);
        } else {
            showToast(R.string.communication_timeout);
        }
        setEnabled(true) ;
    }

    /***盘存6C标签**/
    private void inventory6C() {
        if (checkBoxLoop.isChecked()) {
            //连续盘存
            btnInventory.setText(R.string.stop_inventory);
            setEnabled(false) ;
        }
        if (checkBoxMultiTag.isChecked()) {
            //多标签读取
            mUhfrManager.setGen2session(true);
            mUhfrManager.asyncStartReading();
        }else{
            mUhfrManager.setGen2session(false);
        }
        //计时器
        computedSpeed() ;
        //启动盘存线程
        handler.postDelayed(invenrotyThread, 0);
    }

    /***盘存国标标签**/
    private void inventoryGB() {
        if (checkBoxLoop.isChecked()) {
            //连续盘存
            btnInventory.setText(R.string.stop_inventory);
            setEnabled(false) ;
        }
        //计时器
        computedSpeed() ;
        //启动盘存线程
        handler.postDelayed(invenrotyThread, 0);
    }


    //一秒刷新计算
    long rateValue = 0;
    private void computedSpeed() {
        Map<String, Long> rateMap = new Hashtable<String, Long>();
        timeTask = new Runnable() {
            @Override
            public void run() {
                time++;
                tvTime.setText(secToTime(time) + " (s)");//新版本 这行位置可能与上一行要替换
                long before = 0;
                long after = 0;
                Long afterValue = rateMap.get("after");
                if (null != afterValue) {
                    before = afterValue;
                }
                adapter.notifyData(tagInfoList);
                long readCounts = getReadCount(tagInfoList);
                tvReadCount.setText(readCounts + "");
                tvAllTag.setText(tagInfoList.size() + "");
                rateMap.put("after", readCounts);
                after = readCounts;
                if (after >= before) {
                    rateValue = after - before;
                    tvSpeed.setText(rateValue + " (t/s)");
                }
                //连续盘存模式下，每隔1s循环执行run方法
                if (checkBoxLoop.isChecked()) {
                    handler.postDelayed(timeTask, 1000);
                }

            }
        };
        //延迟一秒执行
        handler.postDelayed(timeTask, 0);
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
        initRecycleView();


        //初始化声音池
        UtilSound.initSoundPool(mainActivity);
        return view;
    }

    /**注册按键监听**/
    private void registerKeyCodeReceiver() {
        keyReceiver = new KeyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        filter.addAction("android.intent.action.FUN_KEY");
        mainActivity.registerReceiver(keyReceiver, filter);
    }

    private void initView() {
        String version = getBaseVersion() ;
        if (version == null) {
            return ;
        }
        switch (version) {

            case VERSION_6B:
                radioButton6B.setVisibility(View.VISIBLE);
                radioButtonGB.setVisibility(View.GONE);
                radioButtonGJB.setVisibility(View.GONE);

                break ;
            case VERSION_GJB:
                radioButton6B.setVisibility(View.GONE);
                radioButtonGJB.setVisibility(View.VISIBLE);
                radioButtonGB.setVisibility(View.GONE);
                break ;
            case VERSION_GB:
                radioButton6B.setVisibility(View.GONE);
                radioButtonGB.setVisibility(View.VISIBLE);
                radioButtonGJB.setVisibility(View.GONE);

                break ;
            case VERSION_GB_GJB:
                radioButton6B.setVisibility(View.GONE);
                radioButtonGB.setVisibility(View.VISIBLE);
                radioButtonGJB.setVisibility(View.VISIBLE);

                break ;
            case VERSION_6C:
            default:
                radioButton6B.setVisibility(View.GONE);
                radioButtonGB.setVisibility(View.GONE);
                radioButtonGJB.setVisibility(View.GONE);
                break;
        }

        //选择要操作的标签类型
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.type_c:
                        mainActivity.tagType = mainActivity.TAG_6C ;
                        break;
                    case R.id.type_b:
                        mainActivity.tagType = mainActivity.TAG_6B ;
                        break;
                    case R.id.type_gb:
                        mainActivity.tagType = mainActivity.TAG_GB ;
                        break;
                    case R.id.type_gjb:
                        mainActivity.tagType = mainActivity.TAG_GJB ;
                        break;

                }
            }
        });
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
}