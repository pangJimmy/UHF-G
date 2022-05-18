package com.pda.uhf_g.ui.fragment;

import android.os.Bundle;

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
    private String epcStr = null ;
    private int membank = 3;
    private int startAddr ;
    private int len ;
    private byte[] accessPassword ;
    private byte[] killPassword ;

    private boolean isEPCNULL = true ;


    private final int UNLOCK = 0 ;//
    private final int LOCK = 1 ;//
    private final int PERM_LOCK = 0 ;//


    Reader.Lock_Obj lock_obj = null;//lock bank
    Reader.Lock_Type lock_type = null;//lock bank
    int lockTypeInt ;//lock type

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
//        mainActivity.mUhfrManager = UHFRManager.getInstance();

        initView();
        return view;
    }

    private void initView() {
        //EPC列表
        if (mainActivity.listEPC != null && mainActivity.listEPC.size() > 0) {
            spinnerEPC.setAdapter(new ArrayAdapter<String>(mainActivity, android.R.layout.simple_spinner_dropdown_item, mainActivity.listEPC));
            isEPCNULL = false ;
        }else{
            epcStr = null ;
            isEPCNULL = true ;
        }
        spinnerEPC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                epcStr = mainActivity.listEPC.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                epcStr = mainActivity.listEPC.get(0) ;
            }
        });

        //
        radioGroupMembank.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //"RESERVED", "EPC" , "TID", "USER"
                switch (checkedId) {
                    case R.id.radioButton_epc:
                        membank = 1 ;

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

        //
        spinnerLockData.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        lock_obj = Reader.Lock_Obj.LOCK_OBJECT_ACCESS_PASSWD;
                        break;
                    case 1:
                        lock_obj = Reader.Lock_Obj.LOCK_OBJECT_KILL_PASSWORD;
                        break;
                    case 2:
                        lock_obj = Reader.Lock_Obj.LOCK_OBJECT_BANK1;//epc
                        break;
                    case 3:
                        lock_obj = Reader.Lock_Obj.LOCK_OBJECT_BANK2;//TID
                        break;
                    case 4:
                        lock_obj = Reader.Lock_Obj.LOCK_OBJECT_BANK3;//USER
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                lock_obj = Reader.Lock_Obj.LOCK_OBJECT_ACCESS_PASSWD;
            }
        });

        //
        spinnerLockType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        lockTypeInt = UNLOCK;
                        break;
                    case 1:
                        lockTypeInt = LOCK;
                        break;
                    case 2:
                        lockTypeInt = PERM_LOCK;
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                lock_obj = Reader.Lock_Obj.LOCK_OBJECT_ACCESS_PASSWD;
            }
        });


    }


    @OnClick(R.id.button_read)
    void readData() {
        if(!checkParam(false)){
            return ;
        }
        byte[] readData = new byte[len * 2];
        byte[] epc = Tools.HexString2Bytes(epcStr);
        Reader.READER_ERR er = Reader.READER_ERR.MT_OK_ERR;
        LogUtil.e("membank = " + membank + ", startAddr = " + startAddr  + ",len =  " + len + ", access = "  +  accessPassword);

        if (checkBoxFilter.isChecked()) {
            //fbank: 1 epc,2 tid ,3 user, 一般使用EPC过滤即选择对应的EPC号的标签进行读写
            readData = mainActivity.mUhfrManager.getTagDataByFilter(membank, startAddr, len, accessPassword, (short) 1000, epc, 1, 2, true);
        }else{
            er = mainActivity.mUhfrManager.getTagData(membank, startAddr, len, readData, accessPassword, (short) 1000);
        }

        if(er== Reader.READER_ERR.MT_OK_ERR && readData!=null){
            editTextReadData.append("Read data:" + Tools.Bytes2HexString(readData, readData.length) + "\n");
        }else{
            showToast(R.string.read_fail);
        }
    }


    @OnClick(R.id.button_write)
    void write() {
        if(!checkParam(true)){
            return ;
        }
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
            er = mainActivity.mUhfrManager.writeTagDataByFilter((char)membank,startAddr,writeDataBytes,writeDataBytes.length,accessPassword,(short)1000,epc,1,2,true);
        }else{
            er = mainActivity.mUhfrManager.writeTagData((char)membank,startAddr,writeDataBytes,writeDataBytes.length,accessPassword,(short)1000);
        }
        if(er== Reader.READER_ERR.MT_OK_ERR ){
            //写入成功
            showToast(R.string.write_success);
        }else{
            //写入失败
            showToast(R.string.write_fail);
        }

    }

    /**
     * 修改EPC
     */
    @OnClick(R.id.button_modify)
    void modifyEPC() {
        if (isEPCNULL) {
            showToast(R.string.please_inventory);
            return ;
        }
        String newEPC = editTextNewEPC.getText().toString().trim() ;
        String accessStr = editTextAccessPassword.getText().toString().trim() ;
        //访问不能为空
        if (accessStr == null || accessStr.length() == 0) {
            showToast(R.string.access_password_not_null);
            return;
        }
        //检验访问密码是否为4字节十六进制数据
        if (!matchHex(accessStr) || accessStr.length() != 8) {
            showToast(R.string.please_input_right_access_password);
            return ;
        }
        //检验新epc是否为4的整数倍长度十六进制数据
        if (!matchHex(newEPC) || newEPC.length()% 4 != 0) {
            showToast(R.string.please_input_right_epc);
            return ;
        }
        accessPassword = Tools.HexString2Bytes(accessStr);

        //EPC区：CRC+PC+EPC号,写入新的EPC需要修改PC+EPC，所以起始地址为1,SDK内部已经计算好PC
//        String pcStr = ComputedPc.getPc(ComputedPc.getEPCLength(editTextNewEPC));
//        String writeData = pcStr + newEPC ;
        byte[] writeDataBytes = Tools.HexString2Bytes(newEPC );
        byte[] epc = Tools.HexString2Bytes(epcStr);
        Reader.READER_ERR er ;
        if (checkBoxFilter.isChecked()){
            //fbank: 1 epc,2 tid ,3 user, 一般使用EPC过滤即选择对应的EPC号的标签进行读写, 起始地址为1
            er = mainActivity.mUhfrManager.writeTagEPCByFilter(writeDataBytes,accessPassword,(short)1000,epc,1,2,true);
        }else{
            er = mainActivity.mUhfrManager.writeTagEPC(writeDataBytes,accessPassword,(short)1000);
        }
        if(er== Reader.READER_ERR.MT_OK_ERR ){
            //写入成功
            showToast(R.string.modify_success);
        }else{
            //写入失败
            showToast(R.string.modify_fail);
        }


    }

    /****
     * 锁定操作
     */
    @OnClick(R.id.button_lock)
    void lock() {
        if (isEPCNULL) {
            showToast(R.string.please_inventory);
            return ;
        }
        String accessStr = editTextAccessPassword.getText().toString().trim() ;
        //访问密码不能为空
        if (accessStr == null || accessStr.length() == 0) {
            showToast(R.string.access_password_not_null);
            return;
        }
        //检验访问密码是否为4字节十六进制数据
        if (!matchHex(accessStr) || accessStr.length() != 8) {
            showToast(R.string.please_input_right_access_password);
            return;
        }
        byte[] epc = Tools.HexString2Bytes(epcStr);
        accessPassword = Tools.HexString2Bytes(accessStr);
        getLockType();
        Reader.READER_ERR er;

        if (checkBoxFilter.isChecked())
            //fbank: 1 epc,2 tid ,3 user, 一般使用EPC过滤即选择对应的EPC号的标签进行读写
            er  = mainActivity.mUhfrManager.lockTagByFilter(lock_obj,lock_type,accessPassword,(short)1000,epc,1,2,true);
        else
            er  = mainActivity.mUhfrManager.lockTag(lock_obj,lock_type,accessPassword,(short)1000);
        if (er== Reader.READER_ERR.MT_OK_ERR) {
            showToast("Lock Success!");
//			editTips.append("Lock Success!" + "\n");
        } else {
            showToast("Lock Fail!");
//			editTips.append("Lock Fail!" + "\n");
        }
    }



    private void getLockType() {
        if(lock_obj== Reader.Lock_Obj.LOCK_OBJECT_ACCESS_PASSWD)
        {

            if(lockTypeInt==UNLOCK)
                lock_type= Reader.Lock_Type.ACCESS_PASSWD_UNLOCK;
            else if(lockTypeInt==LOCK)
                lock_type= Reader.Lock_Type.ACCESS_PASSWD_LOCK;
            else if(lockTypeInt==PERM_LOCK)
                lock_type= Reader.Lock_Type.ACCESS_PASSWD_PERM_LOCK;

        }
        else if(lock_obj== Reader.Lock_Obj.LOCK_OBJECT_KILL_PASSWORD)
        {

            if(lockTypeInt==UNLOCK)
                lock_type= Reader.Lock_Type.KILL_PASSWORD_UNLOCK;
            else if(lockTypeInt==LOCK)
                lock_type= Reader.Lock_Type.KILL_PASSWORD_LOCK;
            else if(lockTypeInt==PERM_LOCK)
                lock_type= Reader.Lock_Type.KILL_PASSWORD_PERM_LOCK;
        }
        else if(lock_obj == Reader.Lock_Obj./*LOCK_OBJECT_EPC*/LOCK_OBJECT_BANK2)
        {

            if(lockTypeInt==UNLOCK)
                lock_type= Reader.Lock_Type.BANK2_UNLOCK;
            else if(lockTypeInt==LOCK)
                lock_type= Reader.Lock_Type.BANK2_LOCK;
            else if(lockTypeInt==PERM_LOCK)
                lock_type= Reader.Lock_Type.BANK2_PERM_LOCK;
        }
        else if(lock_obj == Reader.Lock_Obj.LOCK_OBJECT_BANK1)
        {

            if(lockTypeInt==UNLOCK)
                lock_type= Reader.Lock_Type.BANK1_UNLOCK;
            else if(lockTypeInt==LOCK)
                lock_type= Reader.Lock_Type.BANK1_LOCK;
            else if(lockTypeInt==PERM_LOCK)
                lock_type= Reader.Lock_Type.BANK1_PERM_LOCK;
        }
        else if(lock_obj== Reader.Lock_Obj.LOCK_OBJECT_BANK3)
        {

            if(lockTypeInt==UNLOCK)
                lock_type= Reader.Lock_Type.BANK3_UNLOCK;
            else if(lockTypeInt==LOCK)
                lock_type= Reader.Lock_Type.BANK3_LOCK;
            else if(lockTypeInt==PERM_LOCK)
                lock_type= Reader.Lock_Type.BANK3_PERM_LOCK;
        }

    }



    /***
     * 销毁标签
     */
    @OnClick(R.id.button_kill)
    void kill() {
        if (isEPCNULL) {
            showToast(R.string.please_inventory);
            return ;
        }
        String killStr = editTextKillPassword.getText().toString().trim() ;
        //访问密码不能为空
        if (killStr == null || killStr.length() == 0) {
            showToast(R.string.access_password_not_null);
            return;
        }
        //检验密码是否为4字节十六进制数据
        if (!matchHex(killStr) || killStr.length() != 8) {
            showToast(R.string.please_input_right_access_password);
        }
        byte[] epc = Tools.HexString2Bytes(epcStr);
        killPassword = Tools.HexString2Bytes(killStr);
        Reader.READER_ERR er ;
        //kill tag
        if (checkBoxFilter.isChecked())
            er= mainActivity.mUhfrManager.killTagByFilter(killPassword,(short) 1000,epc,1,2,true);
        else
            er = mainActivity.mUhfrManager.killTag(killPassword,(short) 1000);
        if(er == Reader.READER_ERR.MT_OK_ERR) {
            showToast(R.string.kill_success);
            //editTips.append(selectEPC + getResources().getString(R.string.kill) + getResources().getString(R.string.success)+"\n");
        }else{
            showToast(R.string.kill_fail);
            //Log.e("kill fail",er.toString());
            //editTips.append(selectEPC + getResources().getString(R.string.kill) + getResources().getString(R.string.fail)+"\n");

        }
    }

    /****
     * 清空读数据窗口
     */
    @OnClick(R.id.button_clean)
    void clean() {
        editTextReadData.setText("");
    }


    //检查读写参数
    private boolean checkParam(boolean isWrite) {
        if (isEPCNULL) {
            showToast(R.string.please_inventory);
            return false;
        }
        String startAddrStr = editTextStartAddr.getText().toString().trim() ;
        String lenStr = editTextLen.getText().toString().trim();
        String accessStr = editTextAccessPassword.getText().toString().trim() ;
        //起始地址不能为空
        if (startAddrStr == null || startAddrStr.length() == 0) {
            showToast(R.string.start_address_not_null);
            return false;
        }
        //访问不能为空
        if (accessStr == null || accessStr.length() == 0) {
            showToast(R.string.access_password_not_null);
            return false;
        }
        if(!isWrite){
            //长度不能为空
            if (lenStr == null || lenStr.length() == 0) {
                showToast(R.string.len_not_null);
                return false;
            }
            len = Integer.valueOf(lenStr);
        }
        //检验访问密码是否为4字节十六进制数据
        if (!matchHex(accessStr) || accessStr.length() != 8) {
            showToast(R.string.please_input_right_access_password);
            return false;
        }
        startAddr = Integer.valueOf(startAddrStr);
        accessPassword = Tools.HexString2Bytes(accessStr);
        return true ;
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