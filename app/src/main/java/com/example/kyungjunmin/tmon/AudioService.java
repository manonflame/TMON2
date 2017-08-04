package com.example.kyungjunmin.tmon;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by KyungJunMin on 2017. 7. 25..
 */

public class AudioService extends Service {
    private final IBinder mBinder = new AudioServiceBinder();
    private ArrayList<Long> mAudioIds = new ArrayList<>();
    protected MediaPlayer mMediaPlayer;
    private boolean isPrepared;
    private int mPastPosition;
    private int mCurrentPosition = -1;
    private long mCurrentId;
    private AudioItem mAudioItem;
    private boolean SHUFFLE;
    private boolean REPEAT;
    private boolean isFirst;
    Realm realm;
    SharedPreferences pref;
    private NotificationPlayer mNotificationPlayer;
    public static final String CMD_PLAY = "action.service.PLAY";


    public class AudioServiceBinder extends Binder {
        AudioService getService() {
            Log.d("AudioServieBinder","binder() - return AudioService.this");
            if(mAudioItem != null) {
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
            }
            return AudioService.this;
        }
    }




    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SERVICE","onCreate()");
        isFirst = true;
        Realm.init(this);
        realm = Realm.getDefaultInstance();
        RealmResults<AudioItem> results;
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;
                sendBroadcast(new Intent(BroadcastActions.PREPARED));
                if(isFirst){
                    isFirst=false;
                }
                else{
                    mp.start();
                    updateNotificationPlayer();
                }
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                forward();
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
                updateNotificationPlayer();
            }
        });


        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                //에러 발생시
                isPrepared = false;
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
                updateNotificationPlayer();
                return false;
            }
        });
        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {

            }
        });

        SHUFFLE = pref.getBoolean("SHUFFLE", false);
        REPEAT = pref.getBoolean("REPEAT", false);

        if(SHUFFLE){
            //셔플 상태면 그 가장 최근의 셔플 리스트를 쉐어드 프리퍼런스에 들고오고
            String json = pref.getString("TheLastShuffleList", null);

            if(json != null){
                try{
                    JSONArray jsonArray = new JSONArray((json));
                    for(int i = 0; i < jsonArray.length(); i++){
                        String eachId = jsonArray.optString(i);
                        mAudioIds.add(Long.valueOf(eachId));
                    }

                    mCurrentId = pref.getLong("TheLastId",-1);
                    mCurrentPosition = pref.getInt("TheLastPosition",-1);
                    mAudioItem = realm.where(AudioItem.class).equalTo("mId", mCurrentId).findFirst();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                results = realm.where(AudioItem.class).findAll().sort("INDEX", Sort.ASCENDING);
                for(int i = 0; i < results.size() ; i++){
                    mAudioIds.add(results.get(i).getmId());
                }
                long seed = System.nanoTime();
                Collections.shuffle(mAudioIds, new Random(seed));
                if(mAudioIds.size() != 0){ mCurrentId =  mAudioIds.get(0); }
                mCurrentPosition = 0;
                mAudioItem = realm.where(AudioItem.class).equalTo("mId", mCurrentId).findFirst();
            }

        }else{
            results = realm.where(AudioItem.class).findAll().sort("INDEX", Sort.ASCENDING);
            for(int i = 0; i < results.size() ; i++){
                Log.d("서비스 초기에 아이디 받음",i+"번째"+results.get(i).getmTitle());
                mAudioIds.add(results.get(i).getmId());
            }
            if(mAudioIds.size() != 0){ mCurrentId =  mAudioIds.get(0); }
            mCurrentPosition = 0;
            mAudioItem = realm.where(AudioItem.class).equalTo("mId", mCurrentId).findFirst();
        }


        for(int i = 0; i<mAudioIds.size(); i++){
            Log.d("Services mAudioIds check", ""+mAudioIds.get(i));
        }
        prepare();
        mNotificationPlayer = new NotificationPlayer(this);
    }


    private void updateNotificationPlayer() {
        Log.i("updateNotificationPlayer","in Service");
        if (mNotificationPlayer != null) {
            mNotificationPlayer.updateNotificationPlayer();
        }
    }

    private void removeNotificationPlayer() {
        if (mNotificationPlayer != null) {
            mNotificationPlayer.removeNotificationPlayer();
        }
    }

    public long getmCurrentId(){
        return mCurrentId;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if (intent != null) {
            String action = intent.getAction();
            if (CommandActions.TOGGLE_PLAY.equals(action)) {
                if (isPlaying()) {
                    pause();
                } else {
                    play();
                }
            } else if (CommandActions.REWIND.equals(action)) {
                rewind();
            } else if (CommandActions.FORWARD.equals(action)) {
                forward();
            } else if (CommandActions.CLOSE.equals(action)) {
                pause();
                removeNotificationPlayer();
                stopSelf();
                return START_REDELIVER_INTENT;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        removeNotificationPlayer();
        realm.close();

        super.onDestroy();
    }



    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    public ArrayList<Long> getmAudioIds(){
        return mAudioIds;
    }
    public int getmCurrentPosition(){
        return mCurrentPosition;
    }
    public void setmCurrentPosition(int i){ mCurrentPosition = i; }
    public int getmPastPosition(){
        return mPastPosition;
    }
    public boolean getSHUFFLE(){ return SHUFFLE; }
    public boolean getREPEAT(){return REPEAT; }
    public MediaPlayer getmMediaPlayer(){return mMediaPlayer;}
    public AudioItem getAudioItem(){
        return mAudioItem;
    }



    public void setSHUFFLE(boolean toggle){
        SHUFFLE = toggle;
    }
    public void setREPEAT(boolean toggle){  REPEAT = toggle;    }

    public void setPlayList(ArrayList<Long> audioIds){

        if (!mAudioIds.equals(audioIds)) {
            mAudioIds.clear();
            mAudioIds.addAll(audioIds);
        }
    }

    //재생할 아이템을 램에서 긁어옴
    private void queryAudioItem(int position) {
        //포지션으로 램에서 긁어오고 현재 재생곡에 넣음
        mPastPosition = mCurrentPosition;
        mCurrentPosition = position;
        final long audioId = mAudioIds.get(position);
        mCurrentId = audioId;
        mAudioItem = realm.where(AudioItem.class).equalTo("mId", audioId).findFirst();
    }



    private void prepare() {
        try {
            mMediaPlayer.setDataSource(mAudioItem.getmDataPath());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
    }



    public void addPlayList(Long mId){
        mAudioIds.add(mId);
    }


    public void play(int position) {
        queryAudioItem(position);
        stop();
        prepare();
    }




    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }


    public void play() {
        if (isPrepared) {
            mMediaPlayer.start();
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
            updateNotificationPlayer();
        }
    }

    public void pause() {
        if (isPrepared) {
            mMediaPlayer.pause();
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
            updateNotificationPlayer();
        }
    }

    public void forward() {
        if(REPEAT){
            //반복 재생
            play(mCurrentPosition);
        }
        else{
            //순차 재생
            if (mAudioIds.size() - 1 > mCurrentPosition) {
                // 다음 포지션으로 이동.
                play(mCurrentPosition+1);
            } else {
                // 처음 포지션으로 이동.
                play(0);
        }}
    }

    public void rewind() {
        if(REPEAT){
            //반복 재생
            play(mCurrentPosition);
        }
        else{
            //순차 재생
            if (mCurrentPosition > 0) {
                // 이전 포지션으로 이동.
                play(mCurrentPosition-1);
            } else {
                // 마지막 포지션으로 이동.
                play(mAudioIds.size()-1);
            }
        }
    }


}