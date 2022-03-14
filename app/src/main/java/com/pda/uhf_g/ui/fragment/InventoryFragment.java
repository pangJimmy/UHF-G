package com.pda.uhf_g.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.handheld.uhfr.UHFRManager;
import com.pda.uhf_g.MainActivity;
import com.pda.uhf_g.R;
import com.pda.uhf_g.adapter.RecycleViewAdapter;
import com.pda.uhf_g.entity.TagInfo;
import com.pda.uhf_g.ui.base.BaseFragment;
import com.pda.uhf_g.util.CheckCommunication;
import com.pda.uhf_g.util.GlobalClient;
import com.pda.uhf_g.util.LogUtil;
import com.pda.uhf_g.util.UtilSound;
import com.uhf.api.cls.Reader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
                    LogUtil.e("EPC = "  + Tools.Bytes2HexString(taginfo.EpcId, taginfo.EpcId.length) + "\n");
//                    LogUtil.e("TID = "  + Tools.Bytes2HexString(taginfo.EmbededData, taginfo.EmbededData.length));
                }
                handler.sendEmptyMessage(MSG_INVENROTY);
            }else{
                //多标签状态重置
                if(checkBoxMultiTag.isChecked()){
                    mUhfrManager.asyncStopReading();
                    mUhfrManager.asyncStartReading();
                }
            }
            //是否连续盘存
            if (checkBoxLoop.isChecked()) {
                handler.postDelayed(invenrotyThread, 0) ;
            }

        }
    } ;



    //订阅
//    public void subHandler(GClient client) {
//        client.onTagEpcLog = new HandlerTagEpcLog() {
//            public void log(String readerName, LogBaseEpcInfo info) {
//                if (null != info && 0 == info.getResult()) {
////                    if (isSound)
////                        UtilSound.play(1, 0);
////                    System.out.println(info);
//                    mainActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Map<String, TagInfo> infoMap = pooled6cData(info);
//                            tagInfoList.clear();
//                            tagInfoList.addAll(infoMap.values());
//                        }
//                    });
//                }
//            }
//        };
//        client.onTagEpcOver = new HandlerTagEpcOver() {
//            public void log(String readerName, LogBaseEpcOver info) {
//                handlerStop.sendEmptyMessage(new Message().what = 1);
//            }
//        };
//        client.onTag6bLog = new HandlerTag6bLog() {
//            public void log(String readerName, LogBase6bInfo info) {
////                System.out.println(info);
//                //带上userData一起读则返回result与userData
//                if (null != info && info.getResult() == 0) {
////                    if (isSound)
////                        UtilSound.play(1, 0);
//                    mainActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Map<String, TagInfo> infoMap = pooled6bData(info);
//                            tagInfoList.clear();
//                            tagInfoList.addAll(infoMap.values());
//                        }
//                    });
//                }
//            }
//        };
//        client.onTag6bOver = new HandlerTag6bOver() {
//            public void log(String readerName, LogBase6bOver info) {
//                handlerStop.sendEmptyMessage(new Message().what = 1);
//            }
//        };
//        client.onTagGbLog = new HandlerTagGbLog() {
//            public void log(String readerName, LogBaseGbInfo info) {
////                System.out.println(info);
//                if (null != info && info.getResult() == 0) {
////                    if (isSound)
////                        UtilSound.play(1, 0);
//                    mainActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Map<String, TagInfo> infoMap = pooledGbData(info);
//                            tagInfoList.clear();
//                            tagInfoList.addAll(infoMap.values());
//                        }
//                    });
//                }
//
//            }
//        };
//        client.onTagGbOver = new HandlerTagGbOver() {
//            public void log(String readerName, LogBaseGbOver info) {
//                handlerStop.sendEmptyMessage(new Message().what = 1);
//            }
//        };
//        client.onGpiOver = new HandlerGpiOver() {
//            @Override
//            public void log(String s, LogAppGpiOver logAppGpiOver) {
//                System.out.println(logAppGpiOver);
//            }
//        };
//        client.onGpiStart = new HandlerGpiStart() {
//            @Override
//            public void log(String s, LogAppGpiStart logAppGpiStart) {
//                System.out.println(logAppGpiStart);
//            }
//        };
//    }


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
            tagInfoMap.put(epcAndTid, tagInfo);
        } else {
            TagInfo tag = new TagInfo();
            tag.setIndex(index);
            tag.setType("6C");
            tag.setEpc(Tools.Bytes2HexString(info.EpcId, info.EpcId.length));
            tag.setCount(1l);
            if (info.EmbededData != null) {
                tag.setTid(Tools.Bytes2HexString(info.EmbededData, info.EmbededData.length));
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
//    public Map<String, TagInfo> pooledGbData(LogBaseGbInfo info) {
//        if (tagInfoMap.containsKey(info.getTid() + info.getEpc())) {
//            TagInfo tagInfo = tagInfoMap.get(info.getTid() + info.getEpc());
//            Long count = tagInfoMap.get(info.getTid() + info.getEpc()).getCount();
//            count++;
//            tagInfo.setRssi(info.getRssi() + "");
//            tagInfo.setCount(count);
//            tagInfoMap.put(info.getTid() + info.getEpc(), tagInfo);
//        } else {
//            TagInfo tag = new TagInfo();
//            tag.setIndex(index);
//            tag.setType("GB");
//            tag.setEpc(info.getEpc());
//            tag.setCount(1l);
//            tag.setUserData(info.getUserdata());
//            tag.setTid(info.getTid());
//            tag.setRssi(info.getRssi() + "");
//            tagInfoMap.put(info.getTid() + info.getEpc(), tag);
//            index++;
//        }
////        handlerStop.sendEmptyMessage(2);
//        return tagInfoMap;
//    }
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

    /***盘存EPC***/
    private void inventoryEPC() {
        isReader = true ;
        //判断盘存哪一类标签
        if (radioGroup.getCheckedRadioButtonId() == R.id.type_c) {
            //盘存6C标签
            inventory6C() ;
        }else if(radioGroup.getCheckedRadioButtonId() == R.id.type_b){
            //盘存6B标签
        }
    }

    /**停止盘存**/
    private void stopInventory() {
        if (mainActivity.isConnectUHF) {
            //多标签
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
    }

    /***盘存6C标签**/
    private void inventory6C() {
        if (checkBoxLoop.isChecked()) {
            //连续盘存
            btnInventory.setText(R.string.stop_inventory);
        }
        if (checkBoxMultiTag.isChecked()) {
            //多标签读取
            mUhfrManager.setGen2session(true);
            mUhfrManager.asyncStartReading();
        }
        //启动盘存线程
        computedSpeed() ;
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
        handler.postDelayed(timeTask, 0); //新版本 可能为0
    }


    //格式化时间
    public String secToTime(long time) {
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        time = time * 1000;
        String hms = formatter.format(time);
        return hms;
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
                    case KeyEvent.KEYCODE_F3:
                        break;
                    case KeyEvent.KEYCODE_F4://6100
                        break;
                    case KeyEvent.KEYCODE_F5:
                        break;
                    case KeyEvent.KEYCODE_F7://H3100
                        invenroty();
                        break;
                }
            }
        }

    }
}