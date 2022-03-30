package com.pda.uhf_g.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pda.uhf_g.R;
import com.pda.uhf_g.ui.base.BaseFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HelpFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, null);
        return view;
    }
}
