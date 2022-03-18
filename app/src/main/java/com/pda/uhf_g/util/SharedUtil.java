package com.pda.uhf_g.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedUtil {


    private final SharedPreferences mSharedPreferences;

    public SharedUtil(Context context) {
        mSharedPreferences = context.getSharedPreferences("UHF", Context.MODE_PRIVATE);
    }

    /***
     * 保存工作频段
     * @param workFreq
     */
    public void saveWorkFreq(int workFreq) {
        SharedPreferences.Editor editor = mSharedPreferences.edit() ;
        editor.putInt("workFreq", workFreq);
        editor.commit() ;
    }

    /***
     * 获取保存的工作频段
     * @return
     */
    public int getWorkFreq() {
        return mSharedPreferences.getInt("workFreq", 0);
    }

    /***
     * 保存功率
     * @param power
     */
    public void savePower(int power) {
        SharedPreferences.Editor editor = mSharedPreferences.edit() ;
        editor.putInt("power", power);
        editor.commit() ;
    }


    /***
     * 获取功率
     * @return
     */
    public int getPower() {
        return mSharedPreferences.getInt("power",33);
    }
}
