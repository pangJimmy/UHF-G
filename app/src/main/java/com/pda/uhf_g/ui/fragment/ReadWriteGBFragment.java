package com.pda.uhf_g.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.pda.uhf_g.R;
import com.pda.uhf_g.ui.base.BaseFragment;
import com.pda.uhf_g.util.UtilSound;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReadWriteGBFragment extends BaseFragment {

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

    private boolean isRunning = false ;
    private boolean isStart = false ;
    int count = 0 ;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    } ;



    private Context mainActivity;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = getActivity() ;
        //初始化声音池
        UtilSound.initSoundPool(mainActivity);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read_write_gb, container, false);
        ButterKnife.bind(this, view);
        return view;
    }
}
