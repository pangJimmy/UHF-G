package com.pda.uhf_g.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    @BindView(R.id.textView_test)
    TextView textView ;
    @BindView(R.id.button_test)
    Button buttonTest ;

    private boolean isRunning = false ;
    private boolean isStart = false ;
    int count = 0 ;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    textView.setText("" + count);
                    //在这时播放声音是错误的
                    play() ;
                    break;
            }
        }
    } ;

    long time = 0L;
    public void play() {
        if (System.currentTimeMillis() - time > 40) {
            time = System.currentTimeMillis() ;
            UtilSound.play(1, 0);
        }

    }


    private Runnable task = new Runnable() {
        @Override
        public void run() {
            if (isStart) {
                while (isRunning) {
                    count++ ;
                    handler.sendEmptyMessage(1);

                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    } ;
    private Context mainActivity;


    @OnClick(R.id.button_test)
    void clickTest() {
        if (!isStart) {
            isStart = true ;
            isRunning = true ;
            new Thread(task).start();
        }else{
            isStart = false ;
            isRunning = false ;
        }
    }

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
