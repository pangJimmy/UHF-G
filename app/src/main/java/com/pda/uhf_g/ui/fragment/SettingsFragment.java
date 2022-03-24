package com.pda.uhf_g.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import com.handheld.uhfr.UHFRManager;
import com.pda.uhf_g.MainActivity;
import com.pda.uhf_g.R;
import com.pda.uhf_g.ui.base.BaseFragment;
import com.pda.uhf_g.util.LogUtil;
import com.pda.uhf_g.util.SharedUtil;
import com.uhf.api.cls.Reader;

/**
 *
 */
public class SettingsFragment extends BaseFragment {

    @BindView(R.id.spinner_work_freq)
    Spinner spinnerWorkFreq;
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
    @BindView(R.id.button_query_inventory_type)
    Button buttonQueryInventory;
    @BindView(R.id.button_set_inventory_type)
    Button buttonSetInventory;
    @BindView(R.id.button_query_session)
    Button buttonQuerySession;
    @BindView(R.id.button_set_session)
    Button buttonSetSession;

    @BindView(R.id.checkbox_fastid)
    CheckBox checkBoxFastid;

    //    private SharedPreferences mSharedPreferences;
    //工作频率
    private String[] arrayWorkFreq;
    //Session
    private String[] arraySession;
    //输出功率
    private String[] arrayPower;
    //Q值
    private String[] arrayQvalue;
    //盘存AB面
    private String[] arrayInventoryType;


    private Reader.Region_Conf workFreq;    //工作频率
    private int power = 33; //输出功率
    private int session = 1; //session
    private int qvalue = 1;//Q值
    private int target = 0; //A|B面
    private UHFRManager uhfrManager;
    private MainActivity mainActivity;

    private SharedUtil sharedUtil;
    Reader.READER_ERR err;

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
            return;
        }
        String workFreqStr = "";
        Reader.Region_Conf region = uhfrManager.getRegion();
        LogUtil.e("workFraq = " + region.value());

        if (region == Reader.Region_Conf.RG_NA) {
            //北美902_928
            spinnerWorkFreq.setSelection(2);
            workFreqStr = arrayWorkFreq[2];
        } else if (region == Reader.Region_Conf.RG_PRC) {
            //中国1_920_925
            spinnerWorkFreq.setSelection(0);
            workFreqStr = arrayWorkFreq[0];
        } else if (region == Reader.Region_Conf.RG_EU3) {
            //欧洲865_867
            spinnerWorkFreq.setSelection(3);
            workFreqStr = arrayWorkFreq[3];
        } else if (region == Reader.Region_Conf.RG_PRC2) {
            //中国2_840_845
            spinnerWorkFreq.setSelection(1);
            workFreqStr = arrayWorkFreq[1];

        }

        showToast(mainActivity.getResources().getString(R.string.work_freq) + workFreqStr);

    }

    /**
     * 查询输出功率
     */
    @OnClick(R.id.button_query_power)
    void queryPower() {
        if (!mainActivity.isConnectUHF) {
            showToast(R.string.communication_timeout);
            return;
        }
        int[] powerArray = uhfrManager.getPower();
        if (powerArray != null && powerArray.length > 0) {
            LogUtil.e("powerArray = " + powerArray[0]);
            spinnerPower.setSelection(powerArray[0]);
            showToast(mainActivity.getResources().getString(R.string.power) + powerArray[0] + "dB");
        } else {
            showToast(R.string.query_fail);
        }
    }

    /**
     * 查询session
     */
    @OnClick(R.id.button_query_session)
    void querySession() {
        if (!mainActivity.isConnectUHF) {
            showToast(R.string.communication_timeout);
            return;
        }
        int session = uhfrManager.getGen2session();
        if (session != -1) {
            spinnerSession.setSelection(session);
            showToast("Session" + session);
        } else {
            showToast(R.string.query_fail);
        }
        LogUtil.e("session = " + session);

    }

    /**
     * 查询Q值
     */
    @OnClick(R.id.button_query_qvalue)
    void queryQvalue() {
        if (!mainActivity.isConnectUHF) {
            showToast(R.string.communication_timeout);
            return;
        }
        int qvalue = uhfrManager.getQvalue();
        if (qvalue != -1) {
            spinnerQvalue.setSelection((qvalue ));
            showToast("Q = " + qvalue);
        } else {
            showToast(R.string.query_fail);
        }
        LogUtil.e("qvalue = " + qvalue);
    }

    /***
     * 查询温度
     */
    @OnClick(R.id.button_query_temp)
    void queryTemp() {
        int temp = uhfrManager.getTemperature();
//        LogUtil.e("temp = " + temp) ;
//        uhfrManager.get
    }

    /**
     * 查询盘存参数AB面
     */
    @OnClick(R.id.button_query_inventory_type)
    void queryInventory() {
        if (!mainActivity.isConnectUHF) {
            showToast(R.string.communication_timeout);
            return;
        }
        target = uhfrManager.getTarget();
        LogUtil.e("Target = " + target);
        if (target != -1) {
            spinnerInventoryType.setSelection(target);
            showToast(mainActivity.getResources().getString(R.string.inventory_type) + arrayInventoryType[target]);
        } else {
            showToast(R.string.query_fail);
        }

    }


    /**
     * 设置输出功率
     */
    @OnClick(R.id.button_set_power)
    void setPower() {
        if (!mainActivity.isConnectUHF) {
            showToast(R.string.communication_timeout);
            return;
        }
        err = uhfrManager.setPower(power, power);
        if (err == Reader.READER_ERR.MT_OK_ERR) {
            showToast(R.string.set_success);
            sharedUtil.savePower(power);
        } else {
            //5101 仅支持30db
            showToast(R.string.set_fail);
        }
    }

    /**
     * 设置工作频率
     */
    @OnClick(R.id.button_set_work_freq)
    void setWorkFreq() {
        if (!mainActivity.isConnectUHF) {
            showToast(R.string.communication_timeout);
            return;
        }
        Log.e("zeng-","setworkFraq:"+workFreq);
        err = uhfrManager.setRegion(workFreq);
        if (err == Reader.READER_ERR.MT_OK_ERR) {
            showToast(R.string.set_success);
            sharedUtil.savePower(workFreq.value());
        } else {
            //5101 仅支持30db
            showToast(R.string.set_fail);
        }
    }

    /**
     * 设置session
     */
    @OnClick(R.id.button_set_session)
    void setSession() {
        if (!mainActivity.isConnectUHF) {
            showToast(R.string.communication_timeout);
            return;
        }
        boolean flag = uhfrManager.setGen2session(session);
        if (flag) {
            showToast(R.string.set_success);
            sharedUtil.saveSession(session);
        } else {
            showToast(R.string.set_fail);
        }
    }


    /**
     * 设置Q值
     */
    @OnClick(R.id.button_set_qvalue)
    void setQvalue() {
        if (!mainActivity.isConnectUHF) {
            showToast(R.string.communication_timeout);
            return;
        }
        boolean flag = uhfrManager.setQvaule(qvalue);
        if (flag) {
            showToast(R.string.set_success);
            sharedUtil.saveQvalue(qvalue);
        } else {
            showToast(R.string.set_fail);
        }
    }


    /**
     * 设置Target
     */
    @OnClick(R.id.button_set_inventory_type)
    void setTarget() {
        if (!mainActivity.isConnectUHF) {
            showToast(R.string.communication_timeout);
            return;
        }
        boolean flag = uhfrManager.setTarget(target);
        Log.e("zeng -", "setTarget:" + target);
        if (flag) {
            showToast(R.string.set_success);
            sharedUtil.saveTarget(target);
        } else {
            showToast(R.string.set_fail);
        }
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
        uhfrManager = UHFRManager.getInstance();
        initView();
        Click();
        return view;
    }

    void Click() {
        buttonFreqQuery.performClick();
        buttonQueryInventory.performClick();
        buttonQuerySession.performClick();
        buttonQueryInventory.performClick();
        buttonQueryPower.performClick();
    }

    private void initView() {
        arrayWorkFreq = mainActivity.getResources().getStringArray(R.array.work_freq);
        arraySession = mainActivity.getResources().getStringArray(R.array.session_arrays);
        arrayPower = mainActivity.getResources().getStringArray(R.array.power_arrays);
        arrayQvalue = mainActivity.getResources().getStringArray(R.array.q_value_arrays);
        arrayInventoryType = mainActivity.getResources().getStringArray(R.array.inventory_type_arrays);

        sharedUtil = new SharedUtil(mainActivity);
        //获取保存的设置
        spinnerPower.setSelection(sharedUtil.getPower());
        int freq = sharedUtil.getWorkFreq();
        Log.e("zeng-","freq:"+freq);
        if (Reader.Region_Conf.valueOf(freq) == Reader.Region_Conf.RG_NA) {
            spinnerWorkFreq.setSelection(2);
        } else if (Reader.Region_Conf.valueOf(freq) == Reader.Region_Conf.RG_PRC) {
            spinnerWorkFreq.setSelection(0);
        } else if (Reader.Region_Conf.valueOf(freq) == Reader.Region_Conf.RG_EU3) {
            spinnerWorkFreq.setSelection(3);
        } else if (Reader.Region_Conf.valueOf(freq) == Reader.Region_Conf.RG_PRC2) {
            spinnerWorkFreq.setSelection(1);
        }
        spinnerSession.setSelection(sharedUtil.getSession());
        spinnerQvalue.setSelection((sharedUtil.getQvalue() - 1));


        //工作频率
        spinnerWorkFreq.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String workFreqStr = arrayWorkFreq[position];
                switch (position) {
                    case 0:
                        //中国1_920_925
                        workFreq = Reader.Region_Conf.RG_PRC;
                        break;
                    case 1:
                        //中国2_840_845
                        workFreq = Reader.Region_Conf.RG_PRC2;
                        break;
                    case 2:
                        //北美_902_928
                        workFreq = Reader.Region_Conf.RG_NA;
                        break;
                    case 3:
                        //欧洲865_867
                        workFreq = Reader.Region_Conf.RG_EU3;
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //输出功率
        spinnerPower.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                power = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //session
        spinnerSession.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                session = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Q值
        spinnerQvalue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                qvalue = position ;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //
        spinnerInventoryType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                target = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //fastid
        checkBoxFastid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                uhfrManager.setFastID(b);
            }
        });

    }
}