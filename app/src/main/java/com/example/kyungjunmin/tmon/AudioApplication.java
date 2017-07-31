package com.example.kyungjunmin.tmon;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
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
        Log.d("AudioApplication", "onCreate()");


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
