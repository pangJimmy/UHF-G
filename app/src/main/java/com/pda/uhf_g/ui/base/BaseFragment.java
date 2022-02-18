package com.pda.uhf_g.ui.base;

import android.widget.Toast;

import androidx.fragment.app.Fragment;

/**基础fragment类**/
public class BaseFragment extends Fragment {


    /***
     * 提示弹窗
     * @param msg
     */
    public void showToast(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show(); ;
    }

}
