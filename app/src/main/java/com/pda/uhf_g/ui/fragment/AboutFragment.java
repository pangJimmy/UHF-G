package com.pda.uhf_g.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.handheld.uhfr.UHFRManager;
import com.pda.uhf_g.MainActivity;
import com.pda.uhf_g.R;
import com.pda.uhf_g.ui.base.BaseFragment;


public class AboutFragment extends BaseFragment {

    @BindView(R.id.textView_firmware)
    TextView textViewFirmware;

    @BindView(R.id.textView_date)
    TextView textViewDate;

    @BindView(R.id.textView_soft)
    TextView textViewSoft;
    private UHFRManager uhfrManager;
    private MainActivity mainActivity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, null);
        ButterKnife.bind(this, view);
        mainActivity = (MainActivity) getActivity();
        uhfrManager = mainActivity.mUhfrManager;

        initView();

        return view;
    }

    private void initView() {
        if (mainActivity.isConnectUHF) {
            String version = uhfrManager.getHardware() ;
            String strVer = this.getResources().getString(R.string.firmware);
            String strSoft = this.getResources().getString(R.string.soft_version);
            String strDate = this.getResources().getString(R.string.version_date);
            strSoft = String.format(strSoft, "2.2.8");
            strDate = String.format(strDate, "2022-05-30");
            if (version != null && version.length() > 0) {
                version = String.format(strVer, version);
                textViewFirmware.setText(version);
            }
            textViewSoft.setText(strSoft);
            textViewDate.setText(strDate);
        }
    }
}