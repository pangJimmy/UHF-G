package com.pda.uhf_g.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import com.handheld.uhfr.UHFRManager;
import com.pda.uhf_g.MainActivity;
import com.pda.uhf_g.R;
import com.pda.uhf_g.ui.base.BaseFragment;
import com.pda.uhf_g.util.LogUtil;
import com.uhf.api.cls.Reader;

/**
 *
 */
public class SettingsFragment extends BaseFragment {

    @BindView(R.id.spinner_work_freq)
    Spinner spinnerWorkFreq ;
    @BindView(R.id.spinner_power)
    Spinner spinnerPower;
    @BindView(R.id.spinner_session)
    Spinner spinnerSession;
    @BindView(R.id.spinner_q_value)
    Spinner spinnerQvalue;
    @BindView(R.id.spinner_inventory_type)
    Spinner spinnerInventoryType;
    @BindView(R.id.button_query_work_freq)
    Button buttonFreqQuery;
    @BindView(R.id.button_set_work_freq)
    Button buttonFreqSet;
    @BindView(R.id.editText_temp)
    EditText editTextTemp;
    @BindView(R.id.button_query_power)
    Button buttonQueryPower;
    @BindView(R.id.button_set_power)
    Button buttonSetPower;
    @BindView(R.id.button_query_inventory)
    Button buttonQueryInventory;
    @BindView(R.id.button_set_inventory)
    Button buttonSetInventory;
    //工作频率
    private String[] arrayWorkFreq ;
    //Session
    private String[] arraySession ;
    //输出功率
    private String[] arrayPower ;
    //Q值
    private String[] arrayQvalue ;
    //盘存AB面
    private String[] arrayInventoryType ;



    private Reader.Region_Conf workFreq ;
    private UHFRManager uhfrManager ;
    private MainActivity mainActivity;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * 查询工作频段
     */
    @OnClick(R.id.button_query_work_freq)
    void queryFreq() {
        if (!mainActivity.isConnectUHF) {
            showToast(R.string.communication_timeout);
            return ;
        }
        Reader.Region_Conf region = uhfrManager.getRegion() ;
        LogUtil.e("workFraq = " + region.value()) ;
        if(region == Reader.Region_Conf.RG_NA){
            //北美902_928
            spinnerWorkFreq.setSelection(0);
        }else if(region == Reader.Region_Conf.RG_PRC){
            //中国1_920_925
            spinnerWorkFreq.setSelection(1);
        }else if(region == Reader.Region_Conf.RG_EU3){
            //欧洲865_867
            spinnerWorkFreq.setSelection(1);
        }


    }

    /**
     * 查询输出功率
     */
    @OnClick(R.id.button_query_power)
    void queryPower() {
        int [] powerArray = uhfrManager.getPower() ;
        if (powerArray != null && powerArray.length > 0) {
            LogUtil.e("powerArray = " + powerArray[0]) ;
        }
    }

    /***
     * 查询温度
     */
    @OnClick(R.id.button_query_temp)
    void queryTemp() {
        int temp = uhfrManager.getTemperature() ;
        LogUtil.e("temp = " + temp) ;
//        uhfrManager.get
    }

    /**
     * 查询盘存参数
     */
    @OnClick(R.id.button_query_inventory)
    void queryInventory() {
        int session = uhfrManager.getGen2session() ;
        LogUtil.e("session = " + session) ;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        uhfrManager = UHFRManager.getInstance() ;
        initView();

        return view ;
    }

    private void initView() {
        arrayWorkFreq = mainActivity.getResources().getStringArray(R.array.work_freq);
        arraySession = mainActivity.getResources().getStringArray(R.array.session_arrays);
        arrayPower = mainActivity.getResources().getStringArray(R.array.power_arrays);
        arrayQvalue = mainActivity.getResources().getStringArray(R.array.q_value_arrays);
        arrayInventoryType = mainActivity.getResources().getStringArray(R.array.inventory_type_arrays);

        //工作频率
        spinnerWorkFreq.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String workFreqStr = arrayWorkFreq[position];
                switch (position) {
                    case 0:
                        //北美_902_928
                        workFreq = Reader.Region_Conf.RG_NA ;
                        break;
                    case 1:

                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}