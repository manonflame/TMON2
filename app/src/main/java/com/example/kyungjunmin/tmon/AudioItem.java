package com.example.kyungjunmin.tmon;

import android.database.Cursor;
import android.provider.MediaStore;

/**
 * Created by KyungJunMin on 2017. 7. 18..
 */

public class AudioItem {
    public long mId; // 오디오 고유 ID
    public long mAlbumId; // 오디오 앨범아트 ID
    public String mTitle; // 타이틀 정보
    public String mArtist; // 아티스트 정보
    public String mAlbum; // 앨범 정보
    public long mDuration; // 재생시간
    public String mDataPath; // 실제 데이터위치

    public void bindCursor(Cursor cursor) {
        mId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID));
        mAlbumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
        mTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
        mArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
        mAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
        mDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));
        mDataPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
    }
}
