package com.pda.uhf_g.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.handheld.uhfr.UHFRManager;
import com.pda.uhf_g.MainActivity;
import com.pda.uhf_g.R;
import com.pda.uhf_g.ui.base.BaseFragment;
import com.pda.uhf_g.util.LogUtil;
import com.uhf.api.cls.Reader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pda.serialport.Tools;

/**
 * 读、写、锁定、销毁操作
 *
 */
public class ReadWriteTag extends BaseFragment {


    @BindView(R.id.spinner_epc)
    Spinner spinnerEPC ;
    @BindView(R.id.radio_membank)
    RadioGroup radioGroupMembank ;

    @BindView(R.id.checkbox_filter)
    CheckBox checkBoxFilter ;
    @BindView(R.id.editText_start_addr)
    EditText editTextStartAddr ;
    @BindView(R.id.editText_len)
    EditText editTextLen ;
    @BindView(R.id.editText_access_password)
    EditText editTextAccessPassword ;
    @BindView(R.id.editText_write_data)
    EditText editTextWriteData ;
    @BindView(R.id.editText_read_data)
    EditText editTextReadData ;
    @BindView(R.id.button_read)
    Button buttonRead ;
    @BindView(R.id.button_write)
    Button buttonWrite ;
    @BindView(R.id.button_clean)
    Button buttonClean ;

    @BindView(R.id.sipnner_lock_data)
    Spinner spinnerLockData ;
    @BindView(R.id.sipnner_lock_type)
    Spinner spinnerLockType ;
    @BindView(R.id.editText_lock_password)
    EditText editTextLockPassword ;
    @BindView(R.id.button_lock)
    Button buttonLock ;
    @BindView(R.id.editText_kill_password)
    EditText editTextKillPassword ;
    @BindView(R.id.button_kill)
    Button buttonKill ;

    @BindView(R.id.editText_new_epc)
    EditText editTextNewEPC ;
    @BindView(R.id.button_modify)
    Button buttonModify ;

    private UHFRManager mUhfrManager;
    private MainActivity mainActivity;
    private String epcStr ;
    private int membank ;
    private int startAddr ;
    private int len ;
    private byte[] accessPassword ;

    private boolean isEPCNULL = true ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read_write_tag, container, false) ;
        ButterKnife.bind(this, view);
        mUhfrManager = UHFRManager.getInstance();

        initView();
        return view;
    }

    private void initView() {
        //EPC列表
        if (mainActivity.listEPC != null && mainActivity.listEPC.size() > 0) {
            spinnerEPC.setAdapter(new ArrayAdapter<String>(mainActivity, android.R.layout.simple_spinner_dropdown_item, mainActivity.listEPC));
            isEPCNULL = false ;
        }
        spinnerEPC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //选择需要操作的EPC
                epcStr = mainActivity.listEPC.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        radioGroupMembank.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //"RESERVED", "EPC" , "TID", "USER"
                switch (checkedId) {
                    case R.id.radioButton_epc:
                        membank = 1 ;
                        //默认选择EPC区读取数据时从2开始读写
                        editTextStartAddr.setText("2");
                        break;
                    case R.id.radioButton_tid:
                        membank = 2 ;
                        editTextStartAddr.setText("0");
                        break;
                    case R.id.radioButton_user:
                        membank = 3 ;
                        editTextStartAddr.setText("0");
                        break;
                    case R.id.radioButton_password:
                        membank = 0 ;
                        editTextStartAddr.setText("0");
                        break;
                }
            }
        });
    }

    /***
     * 读数据
     */
    @OnClick(R.id.button_read)
    void readData() {
        checkParam(false);
        byte[] readData = new byte[len * 2];
        byte[] epc = Tools.HexString2Bytes(epcStr);
        Reader.READER_ERR er = Reader.READER_ERR.MT_OK_ERR;
        LogUtil.e("membank = " + membank + ", startAddr = " + startAddr  + ",len =  " + len + ", access = "  +  accessPassword);

        if (checkBoxFilter.isChecked()) {
            //fbank: 1 epc,2 tid ,3 user, 一般使用EPC过滤即选择对应的EPC号的标签进行读写
            readData = mUhfrManager.getTagDataByFilter(membank, startAddr, len, accessPassword, (short) 1000, epc, 1, 2, true);
        }else{
            er = mUhfrManager.getTagData(membank, startAddr, len, readData, accessPassword, (short) 1000);
        }

        if(er== Reader.READER_ERR.MT_OK_ERR && readData!=null){
            editTextReadData.append("Read data:" + Tools.Bytes2HexString(readData, readData.length) + "\n");
        }else{
            showToast(R.string.read_fail);
        }
    }

    /**
     * 写数据
     */
    @OnClick(R.id.button_write)
    void write() {
        checkParam(true) ;
        String writeDataStr = editTextWriteData.getText().toString().trim() ;
        if(writeDataStr == null || !matchHex(writeDataStr) || writeDataStr.length() % 4 != 0){
            showToast(R.string.please_input_right_write_data);
            return ;
        }
        byte[] writeDataBytes = Tools.HexString2Bytes(writeDataStr) ;
        byte[] epc = Tools.HexString2Bytes(epcStr);
        Reader.READER_ERR er ;
        LogUtil.e("membank = " + membank + ", startAddr = " + startAddr  + ", access = "  +  accessPassword);
        if (checkBoxFilter.isChecked()){
            //fbank: 1 epc,2 tid ,3 user, 一般使用EPC过滤即选择对应的EPC号的标签进行读写
            er = mUhfrManager.writeTagDataByFilter((char)membank,startAddr,writeDataBytes,writeDataBytes.length,accessPassword,(short)1000,epc,1,2,true);
        }else{
            er = mUhfrManager.writeTagData((char)membank,startAddr,writeDataBytes,writeDataBytes.length,accessPassword,(short)1000);
        }
        if(er== Reader.READER_ERR.MT_OK_ERR ){
            //写入成功
            showToast(R.string.write_success);
        }else{
            //写入失败
            showToast(R.string.write_fail);
        }

    }

    /****
     * 锁定操作
     */
    @OnClick(R.id.button_lock)
    void lock() {

    }

    /****
     * 清空读数据窗口
     */
    @OnClick(R.id.button_clean)
    void clean() {
        editTextReadData.setText("");
    }


    //检查读写参数
    private void checkParam(boolean isWrite) {
        if (isEPCNULL) {
            showToast(R.string.please_inventory);
            return ;
        }
        String startAddrStr = editTextStartAddr.getText().toString().trim() ;
        String lenStr = editTextLen.getText().toString().trim();
        String accessStr = editTextAccessPassword.getText().toString().trim() ;
        //起始地址不能为空
        if (startAddrStr == null || startAddrStr.length() == 0) {
            showToast(R.string.start_address_not_null);
            return;
        }
        //访问不能为空
        if (accessStr == null || accessStr.length() == 0) {
            showToast(R.string.access_password_not_null);
            return;
        }
        if(!isWrite){
            //长度不能为空
            if (lenStr == null || lenStr.length() == 0) {
                showToast(R.string.len_not_null);
                return;
            }
            len = Integer.valueOf(lenStr);
        }
        //检验访问密码是否为4字节十六进制数据
        if (!matchHex(accessStr) || accessStr.length() != 8) {
            showToast(R.string.please_input_right_access_password);
        }
        startAddr = Integer.valueOf(startAddrStr);
        accessPassword = Tools.HexString2Bytes(accessStr);
    }

    /**
     * 校验输入的数据是否为十六进制
     * @param data
     * @return
     */
    private boolean matchHex(String data) {
        boolean flag = false ;

        String regEx = "-?[0-9a-fA-F]+" ;
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(data);
        flag = matcher.matches();
        return flag;
    }
}