package com.pda.uhf_g.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;


import com.pda.uhf_g.R;

import java.util.HashMap;
import java.util.Map;


public class UtilSound {

    public static SoundPool sp;
    public static Map<Integer, Integer> suondMap;
    public static Context context;

    //init sound pool
    public static void initSoundPool(Context context) {
        UtilSound.context = context;
        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
        suondMap = new HashMap<Integer, Integer>();
        suondMap.put(1, sp.load(context, R.raw.barcodebeep, 1));
        suondMap.put(2,sp.load(context,R.raw.beep,1));
        suondMap.put(3,sp.load(context,R.raw.beeps,1));
    }

    private static long time = 0;
    //play sound
    public static void play(int sound, int number) {
        if (System.currentTimeMillis() - time > 30) {
            AudioManager am = (AudioManager) UtilSound.context.getSystemService(UtilSound.context.AUDIO_SERVICE);
            float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            float volumnRatio = audioCurrentVolume / audioMaxVolume;

            sp.play(3, 1, 1, 0, 0, 2f);//0.5-2.0 speed
            time = System.currentTimeMillis() ;
        }

    }


}
