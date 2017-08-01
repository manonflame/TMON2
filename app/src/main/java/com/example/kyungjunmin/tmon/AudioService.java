package com.example.kyungjunmin.tmon;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
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
    private boolean isFirst = true;

    private boolean nowPlaying;

    Realm realm;


    SharedPreferences pref;


    public static final String CMD_PLAY = "action.service.PLAY";


    public class AudioServiceBinder extends Binder {
        AudioService getService() {
            Log.d("AudioServieBinder","binder() - return AudioService.this");

            return AudioService.this;
        }
    }




    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("AudioService","onCreate()");
        //램연결
        realm = Realm.getDefaultInstance();
        RealmResults<AudioItem>  results;

        pref = PreferenceManager.getDefaultSharedPreferences(this);


        SHUFFLE = pref.getBoolean("SHUFFLE", false);

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


                    //마지막 재생중이 었던 음악의 아이디를 쉐어드 프리퍼런스에서 들고와서
                    //쿼리로 긁은 다음에 현재 오디오에 맞춰주자(아이디, 아이템, 포지션)
                    mCurrentId = pref.getLong("TheLastId",-1);
                    mCurrentPosition = pref.getInt("TheLastPosition",-1);
                    mAudioItem = realm.where(AudioItem.class).equalTo("mId", mCurrentId).findFirst();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Log.d("SERVICE onCreate","쉐어드PF의 오디오 리스트 값이 null입니다");
                //쉐어드 프리퍼런스 상태가 없다면 그냥 임의 정렬함
                //램에서 오디오 아이디 긁어와야함
                results = realm.where(AudioItem.class).findAll().sort("INDEX", Sort.ASCENDING);
                for(int i = 0; i < results.size() ; i++){
                    Log.d("서비스 초기에 아이디 받음",i+"번째"+results.get(i).getmTitle());
                    mAudioIds.add(results.get(i).getmId());
                }
                long seed = System.nanoTime();
                Collections.shuffle(mAudioIds, new Random(seed));

                //마지막 재생중이 없다면 맨 위 것을 뽑아서
                //쿼리로 긁은 다음에 현재 오디오에 맞춰주자(아이디, 아이템, 포지션)
                mCurrentId = mAudioIds.get(0);
                mCurrentPosition = 0;
                mAudioItem = realm.where(AudioItem.class).equalTo("mId", mCurrentId).findFirst();
            }

        }else{
            //램에서 오디오 아이디 긁어와야함 ******(순차상태)
            results = realm.where(AudioItem.class).findAll().sort("INDEX", Sort.ASCENDING);
            for(int i = 0; i < results.size() ; i++){
                Log.d("서비스 초기에 아이디 받음",i+"번째"+results.get(i).getmTitle());
                mAudioIds.add(results.get(i).getmId());
            }

            //그리고 맨 위에 애를 현재 재생으로 맞춰주자(아이디, 아이템, 포지션)
            mCurrentId = mAudioIds.get(0);
            mCurrentPosition = 0;
            mAudioItem = realm.where(AudioItem.class).equalTo("mId", mCurrentId).findFirst();
        }


        for(int i = 0; i<mAudioIds.size(); i++){
            Log.d("Services mAudioIds check", ""+mAudioIds.get(i));
        }

        mMediaPlayer = new MediaPlayer();


        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;
                mp.start();
                nowPlaying = true;
                sendBroadcast(new Intent(BroadcastActions.PREPARED));
                Log.d("SERVICE-재생준비중인 음악","포지션 : " + mCurrentPosition + ",, 타이틀 : "+mAudioItem.getmTitle());
                Log.d("SERVICE-setOnPreparedListener","PREPARED 인텐트 전송");

                if(isFirst){
                    isFirst=false;
                    pause();
                }

            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //완료시
                //다음곡
                forward();
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
                Log.d("SERVICE-setOnCompletionListener","PLAY_STATE_CHANGED 인텐트 전송");

            }
        });


        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                //에러 발생시
                isPrepared = false;
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
                Log.d("SERVICE-setOnErrorListener","PLAY_STATE_CHANGED 인텐트 전송");

                return false;
            }
        });
        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {

            }
        });

        try {
            mMediaPlayer.setDataSource(mAudioItem.getmDataPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
      //  mMediaPlayer.pause();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        String action = intent.getAction();
        Log.d("SERVICE","onStartCommand");
        return START_STICKY;
    }

    public void toggleShuffle(){
        if(SHUFFLE){
            Log.d("TOGGLE SHUFFLE","셔플 끔");
            SHUFFLE = false;
        }else{
            Log.d("TOGGLE SHUFFLE","셔플 컴");
            SHUFFLE = true;
        }
    }

    public ArrayList<Long> getmAudioIds(){
        return mAudioIds;
    }

    public void toggleRepeat(){
        if(REPEAT){
            Log.d("TOGGLE REPEAT","반복 끔");
            REPEAT = false;
        }else{
            Log.d("TOGGLE REPEAT","반복 켬");
            REPEAT = true;
        }
    }

    public int getmCurrentPosition(){
        return mCurrentPosition;
    }
    public void setmCurrentPosition(int i){ mCurrentPosition = i; }

    public int getmPastPosition(){
        return mPastPosition;
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d("AudioService- onBind","return SERVICE BINDER()"+intent.toString());
//        this.startService(new Intent(this, AudioService.class));
        return mBinder;
    }



    public void setSHUFFLE(boolean toggle){
        SHUFFLE = toggle;
    }

    public boolean getSHUFFLE(){ return SHUFFLE; }


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
            Log.d("prepare",mAudioItem.getmTitle()+"  postion : "+mCurrentPosition);
        } catch (Exception e) {
            Log.d("prepare","에러발생");
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

    public void setPlayList(ArrayList<Long> audioIds){

        if (!mAudioIds.equals(audioIds)) {
            mAudioIds.clear();
            mAudioIds.addAll(audioIds);
        }
    }

    public void play(int position) {
        queryAudioItem(position);
        pause();
        Log.d("SERVICE-play(position)","stop전");
        stop();
        Log.d("SERVICE-play(position)","stop후");
        prepare();
    }

    public MediaPlayer getmMediaPlayer(){
        return mMediaPlayer;
    }

    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    public AudioItem getAudioItem(){
        return mAudioItem;
    }

    public void play() {
        if (isPrepared) {
            mMediaPlayer.start();
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
            Log.d("SERVICE-play","PLAY_STATE_CHANGED 인텐트 전송");
        }
    }

    public void pause() {
        if (isPrepared) {
            mMediaPlayer.pause();
            sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
            Log.d("SERVICE-pause","PLAY_STATE_CHANGED 인텐트 전송");
        }
    }

    public void forward() {
        //순차 재생
        Log.d("SERVICE","forward() 다음 포지션으로 이동시킨다" + mCurrentPosition +"에서 " + (mCurrentPosition+1) + "로");
        if (mAudioIds.size() - 1 > mCurrentPosition) {
            // 다음 포지션으로 이동.
            play(mCurrentPosition+1);
        } else {
            // 처음 포지션으로 이동.
            play(0);
        }
    }



    public void rewind() {
        if (mCurrentPosition > 0) {
            // 이전 포지션으로 이동.
            play(mCurrentPosition-1);
        } else {
            // 마지막 포지션으로 이동.
            play(mAudioIds.size()-1);
        }
    }

    public void changePosition(int fromPosition, int toPosition){

        Log.d("changePosition","============================");
        Log.d("fromPosition", ""+mAudioIds.get(fromPosition));
        Log.d("toPosition",""+mAudioIds.get(toPosition));
        Log.d("changePosition","============================");
        long tempId = mAudioIds.get(fromPosition);
        mAudioIds.set(fromPosition, mAudioIds.get(toPosition));
        mAudioIds.set(toPosition, tempId);
        mCurrentPosition = toPosition;
        Log.d("fromPosition", ""+mAudioIds.get(fromPosition));
        Log.d("toPosition",""+mAudioIds.get(toPosition));

        Log.d("changePosition","============================");
    }


}
