package com.pda.uhf_g.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedUtil {


    private final SharedPreferences mSharedPreferences;

    public SharedUtil(Context context) {
        mSharedPreferences = context.getSharedPreferences("UHF", Context.MODE_PRIVATE);
    }


    public void saveWorkFreq(int workFreq) {
        SharedPreferences.Editor editor = mSharedPreferences.edit() ;
        editor.putInt("workFreq", workFreq);
        editor.commit() ;
    }


    public int getWorkFreq() {
        return mSharedPreferences.getInt("workFreq", 1);
    }


    public void savePower(int power) {
        SharedPreferences.Editor editor = mSharedPreferences.edit() ;
        editor.putInt("power", power);
        editor.commit() ;
    }


    public int getPower() {
        return mSharedPreferences.getInt("power",33);
    }


    public void saveSession(int session) {
        SharedPreferences.Editor editor = mSharedPreferences.edit() ;
        editor.putInt("session", session);
        editor.commit() ;
    }


    public int getSession() {
        return mSharedPreferences.getInt("session",0);
    }


    public void saveQvalue(int qvalue) {
        SharedPreferences.Editor editor = mSharedPreferences.edit() ;
        editor.putInt("qvalue", qvalue);
        editor.commit() ;
    }


    public int getQvalue() {
        return mSharedPreferences.getInt("qvalue",0);
    }


    public void saveTarget(int target) {
        SharedPreferences.Editor editor = mSharedPreferences.edit() ;
        editor.putInt("target", target);
        editor.commit() ;
    }


    public int getTarget() {

        return mSharedPreferences.getInt("target",0);
    }
}
