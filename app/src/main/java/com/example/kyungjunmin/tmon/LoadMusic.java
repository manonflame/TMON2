package com.example.kyungjunmin.tmon;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KyungJunMin on 2017. 7. 18..
 *///

public class LoadMusic {
    private Context context;
    private ArrayList<AudioItem> musicList;
    public LoadMusic(Context context){
        this.context = context;
        musicList = new ArrayList<AudioItem>();
        getMusicInfo();
    }


    public ArrayList<AudioItem> getMusicList() {
        return musicList;
    }


    private void getMusicInfo(){
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };

        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

        if(musicCursor != null && musicCursor.moveToFirst()){
            do {
                musicList.add(AudioItem.bindCursor(musicCursor));
            } while (musicCursor.moveToNext());
            musicCursor.close();
        }
    }

    public List<AudioItem> getSearchAudioList(String searchWord){
        List<AudioItem> retArr = new ArrayList<>();

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };
        String selection = MediaStore.Audio.Media.TITLE+" like ? ";
        String[] selectionArgs = {"%"+searchWord+"%"};
        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);

        if(musicCursor != null && musicCursor.moveToFirst()){
            do {
                retArr.add(AudioItem.bindCursor(musicCursor));
            } while (musicCursor.moveToNext());
            musicCursor.close();
        }
        Log.d("getSearchAudioList", retArr.size()+"");
        return retArr;
    }
}
