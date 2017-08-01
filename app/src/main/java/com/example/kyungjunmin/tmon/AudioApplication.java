package com.example.kyungjunmin.tmon;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

/**
 * Created by KyungJunMin on 2017. 7. 25..
 */

public class AudioApplication extends Application {
    private static AudioApplication mInstance;
    private AudioServiceInterface mInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("AudioApplication","onCreate()");
        mInstance = this;
        mInterface = new AudioServiceInterface(getApplicationContext());

    }

    public static AudioApplication getInstance() {
        return mInstance;
    }

    public AudioServiceInterface getServiceInterface() {
        return mInterface;
    }
}
