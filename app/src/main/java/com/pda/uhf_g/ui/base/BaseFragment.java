package com.pda.uhf_g.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**基础fragment类**/
public class BaseFragment extends Fragment {


    public boolean isNavigationViewInit = false ;//记录是否已经初始化过一次视图
    private View lastView = null;//记录上次创建的view

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (lastView == null) {
            lastView = super.onCreateView(inflater, container, savedInstanceState) ;
        }
        return lastView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //初始化过视图则不再进行
        if(!isNavigationViewInit){
            super.onViewCreated(view, savedInstanceState) ;
            isNavigationViewInit = true ;
        }
    }

    /***
     * 提示弹窗
     * @param msg
     */
    public void showToast(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show(); ;
    }

    /***
     * 提示弹窗
     * @param resID
     */
    public void showToast(int resID){
        Toast.makeText(getActivity(), resID, Toast.LENGTH_SHORT).show(); ;
    }

}
