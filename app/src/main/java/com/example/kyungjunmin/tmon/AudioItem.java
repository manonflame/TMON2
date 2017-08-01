package com.example.kyungjunmin.tmon;

import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.SeekBar;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by KyungJunMin on 2017. 7. 18..
 */

public class AudioItem extends RealmObject{
    @PrimaryKey
    private long mId; // 오디오 고유 ID
    @Index
    private int INDEX;
    private long mAlbumId; // 오디오 앨범아트 ID
    private String mTitle; // 타이틀 정보
    private String mArtist; // 아티스트 정보
    private String mAlbum; // 앨범 정보
    private long mDuration; // 재생시간
    private String mDataPath; // 실제 데이터위치

    public static AudioItem bindCursor(Cursor cursor) {
        AudioItem audioItem = new AudioItem();
        audioItem.mId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID));
        audioItem.mAlbumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
        audioItem.mTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
        audioItem.mArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
        audioItem.mAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
        audioItem.mDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));
        audioItem.mDataPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
        return audioItem;
    }

    //getter
    public long getmId(){
        return mId;
    }
    public long getmAlbumId(){

        return mAlbumId;
    }
    public String getmTitle(){
        return mTitle;
    }
    public String getmArtist(){
        return mArtist;
    }
    public String getmAlbum(){
        return mAlbum;
    }
    public long getmDuration(){
        return mDuration;
    }
    public String getmDataPath(){
        return mDataPath;
    }
    public int getmIndex(){ return INDEX; }



    //setter
    public void setmId(long mId){
        this.mId = mId;
    }
    public void setmAlbumId(long mAlbumId){
        this.mAlbumId = mAlbumId;
    }
    public void setmTitle(String mTitle){
        this.mTitle = mTitle;
    }
    public void setmArtist(String mArtist){
        this.mArtist = mArtist;
    }
    public void setmAlbum(String mAlbum){
        this.mAlbum = mAlbum;
    }
    public void setmDuration(long mDuration){
        this.mDuration = mDuration;
    }
    public void setmDataPath(String mDataPath){
        this.mDataPath = mDataPath;
    }
    public void setmIndex(int index){ this.INDEX = index; }


}
