package com.example.kyungjunmin.tmon;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by KyungJunMin on 2017. 7. 17..
 */

public class AudioAdapter extends CursorRecyclerViewAdapter{


    public AudioAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        AudioItem audioItem = AudioItem.bindCursor(cursor);
        ((AudioViewHolder) viewHolder).setAudioItem(audioItem, cursor.getPosition());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_audio_card, parent, false);
        return new AudioViewHolder(v);
    }


    public static class AudioItem {

        public long mId; // 오디오 고유 ID
        public long mAlbumId; // 오디오 앨범아트 ID
        public String mTitle; // 타이틀 정보
        public String mArtist; // 아티스트 정보
        public String mAlbum; // 앨범 정보
        public long mDuration; // 재생시간
        public String mDataPath; // 실제 데이터위치

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



    }


    class AudioViewHolder extends RecyclerView.ViewHolder {
        private final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        private ImageView mImgAlbumArt;
        private TextView mTxtTitle;
        private TextView mTxtSubTitle;
        private TextView mTxtDuration;
        private AudioItem mItem;
        private int mPosition;

        private AudioViewHolder(View view) {
            super(view);
            mImgAlbumArt = (ImageView) view.findViewById(R.id.img_albumart);
            mTxtTitle = (TextView) view.findViewById(R.id.txt_title);
            mTxtSubTitle = (TextView) view.findViewById(R.id.artist_name);

            //여기서는 사용 안함
            //mTxtDuration = (TextView) view.findViewById(R.id.txt_duration);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ..
                }
            });
        }

        public void setAudioItem(AudioItem item, int position) {
            mItem = item;
            mPosition = position;
            mTxtTitle.setText(item.mTitle);
            mTxtSubTitle.setText(item.mArtist + "(" + item.mAlbum + ")");

            //여기서 듀레이션은 안씀
            //mTxtDuration.setText(DateFormat.format("mm:ss", item.mDuration));
            Uri albumArtUri = ContentUris.withAppendedId(artworkUri, item.mAlbumId);
            Picasso.with(itemView.getContext()).load(albumArtUri).error(R.mipmap.ic_empty_albumart).into(mImgAlbumArt);
        }
    }
}
