package com.example.kyungjunmin.tmon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by KyungJunMin on 2017. 7. 25..
 */

public class AudioServiceInterface {
    private ServiceConnection mServiceConnection;
    private AudioService mService;

    public AudioServiceInterface(Context context) {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("onServiceConnected","SUCCCESS TO CONNECT SERVICE"+service.toString());
                mService = ((AudioService.AudioServiceBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("onServiceDisconnected","FAIL TO MAKE SERVICE");
                mServiceConnection = null;
                mService = null;
            }
        };
    }
    public AudioService getmService(){
        return mService;
    }

    public ServiceConnection getmServiceConnection(){
        return mServiceConnection;
    };
    public void addPlayList(Long mId){
        if (mService != null) {
            Log.d("addPlayList in INTERFACE","NOT null" );
            mService.addPlayList(mId);
        }
        else{
            Log.d("addPlayList in INTERFACE","null" );
        }
    }

    public ArrayList<Long> getmAudioIds(){
        if (mService != null) {
            return mService.getmAudioIds();
        }
        return null;
    }

    public boolean checkNull(){
        if (mService != null) {
            return false;
        }else{
            return true;
        }
    }

    public void setSHUFFLE(boolean toggle){
        if (mService != null) {
            mService.setSHUFFLE(toggle);
        }
    }

    public boolean getSHUFFLE(){
        if(mService ==null){
            Log.e("mService getSHUFFLE","mService is NULL");
        }
        return mService.getSHUFFLE();
    }


    public void setPlayList(ArrayList<Long> audioIds) {
        if (mService != null) {
            mService.setPlayList(audioIds);
        }
    }

    public void play(int position) {
        if (mService != null) {
            mService.play(position);
        }
    }

    public void play() {
        if (mService != null) {
            mService.play();
        }
    }

    public void pause() {
        if (mService != null) {
            mService.play();
        }
    }

    public void togglePlay() {
        if (isPlaying()) {
            mService.pause();
        } else {
            Log.d("서비스 인터페이스 토글플레이 ", "플레이");
            mService.play();
        }
    }

    public boolean isPlaying() {
        if (mService != null) {
            return mService.isPlaying();
        }
        return false;
    }

    public AudioItem getAudioItem() {
        if (mService != null) {
            return mService.getAudioItem();
        }
        return null;
    }


    public MediaPlayer getmMediaPlayer(){
        if (mService != null) {
            return mService.getmMediaPlayer();
        }
        return null;
    }



    public void forward() {
        if (mService != null) {
            mService.forward();
        }
    }

    public void rewind() {
        if (mService != null) {
            mService.rewind();
        }
    }

    public void changePosition(int fromPosition, int toPosition){
        if(mService != null){
            mService.changePosition(fromPosition,toPosition);
        }
    }

    public int getmCurrentPosition(){
        if(mService != null){
            return mService.getmCurrentPosition();
        }
        else{
            Log.d("mService","null");
            return -1;
        }
    }

    public int getmPastPosition(){
        if(mService != null){
            return mService.getmPastPosition();
        }
        else{
            Log.d("mService","null");
            return -1;
        }
    }

    public void toggleShuffle(){
        if(mService != null){
            mService.toggleShuffle();
        }
    }

    public void toggleRepeat(){
        if(mService != null){
            mService.toggleRepeat();
        }
    }

    public void setmCurrentPosition(int i)
    {
        if(mService != null){
            mService.setmCurrentPosition(i);
        }
    }

}
