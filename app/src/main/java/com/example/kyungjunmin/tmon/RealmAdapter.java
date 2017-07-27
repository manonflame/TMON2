package com.example.kyungjunmin.tmon;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by KyungJunMin on 2017. 7. 21..
 */

public class RealmAdapter extends RecyclerView.Adapter<RealmAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {


    private RealmResults<AudioItem> mDataset;
    private final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");


    private final OnStartDragListener mDragStartListener;
    private final OnCustomerListChangedListener mListChangedListener;

    public BroadcastReceiver mBCReceiver;


    //현재 서비스에 올라있는 애
    private int nowPosition;
    //서비스에 올라있는애의 재생 중인지 여부
    private boolean playing;

    //이거 없어도됌
    Context mContext;


    public RealmAdapter(RealmResults<AudioItem> myDataset, OnStartDragListener dragLlistener,
                        OnCustomerListChangedListener listChangedListener, Context context) {
        mDataset = myDataset;
        mDragStartListener = dragLlistener;
        mListChangedListener = listChangedListener;
        mContext = context;


        mBCReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //현재 진행중인 포지션을 인텐트로 변경하고
                //정지인지 플레이인지 확인
            }
        };


    }

    public void setmDataset(RealmResults<AudioItem> mDataset) {
        this.mDataset = mDataset;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("onCreateViewHolder", "Check");
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
        ItemViewHolder holder = new ItemViewHolder(v);


        return holder;
    }


    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {

        final AudioItem selectedItem = mDataset.get(holder.getAdapterPosition());
        holder.PLMusicTitle.setText(selectedItem.getmTitle());
        holder.PLArtisttName.setText(selectedItem.getmArtist());
        Uri albumArtUrl = ContentUris.withAppendedId(artworkUri, selectedItem.getmAlbumId());
        Picasso.with(holder.itemView.getContext()).load(albumArtUrl).error(R.mipmap.ic_empty_albumart).into(holder.PLAlbmIMG);


        Log.d("onBindViewHolder POSITION", holder.getAdapterPosition() + "");
        Log.d("onBindViewHolder TITLE", selectedItem.getmTitle() + "");
        Log.d("onBindViewHolder INDEX", selectedItem.getmIndex() + "");
        //리스너 구현 -- 드래그
        holder.PLDragButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                    Log.e("action down", "ation down");
                }
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
                    Log.e("action up", "ation up");
                }
                Log.d("setOnTouchListener()  :", "end of listener");
                return false;
            }
        });

        holder.PlayMusicByTitle.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Log.d("Let's Play music!! the posion is :::::", holder.getAdapterPosition()+"");
//              AudioApplication.getInstance().getServiceInterface().setPlayList(getAudioIds()); // 재생목록등록
                AudioApplication.getInstance().getServiceInterface().play(position);
            }
        });

        //리스너 구현 -- 옵션버튼
        //버튼 클릭 이벤트
        holder.optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu libPopUp = new PopupMenu(mContext, v);

                libPopUp.getMenuInflater().inflate(R.menu.playlist_popup, libPopUp.getMenu());

                libPopUp.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.list_delete:
                                //램디에서 삭제
                                final Realm realm;
                                realm = Realm.getDefaultInstance();
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        Log.d("holder.optionButton.", "삭제될 아이템의 포지션 : " + position);
                                        Log.d("holder.optionButton.", "삭제될 아이템의 어댑터의 포지션 : " + holder.getAdapterPosition());
                                        Log.d("holder.optionButton.", "삭제될 아이템의 인덱스 : " + selectedItem.getmIndex());
                                        selectedItem.deleteFromRealm();
                                        //서비스의 아이디 목록 최신화
                                        Log.d("아이템 삭제", "서비스 리스트 최신화");
                                        notifyDataSetChanged();
                                    }
                                });

                                break;

                            case R.id.music_delete:
                                //라이브러리 목록에서 삭제하면 됨
                                break;
                        }
                        return true;
                    }
                });
                libPopUp.show();
            }
        });
    }

    @Override
    public void onItemMove(final int fromPosition, final int toPosition) {
        final AudioItem selectedItem = mDataset.get(fromPosition);
        final AudioItem changedItem = mDataset.get(toPosition);
        final int beforeIndex = selectedItem.getmIndex();
        final int afterIndex = changedItem.getmIndex();

        Realm realm;
        realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                selectedItem.setmIndex(afterIndex);
                changedItem.setmIndex(beforeIndex);
                realm.copyToRealmOrUpdate(selectedItem);
                realm.copyToRealmOrUpdate(changedItem);
            }
        });
        mListChangedListener.onNoteListChanged(mDataset);
        notifyItemMoved(fromPosition, toPosition);
        notifyItemChanged(fromPosition);
        notifyItemChanged(toPosition);

        //서비스의 아이디 목록 최신화
        Log.d("아이템 드래그", "서비스 리스트 최신화");

    }




    public static class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        //버튼 선언
        //public final Button PLOptionButton;
        public final Button PLDragButton;
        public final ImageView PLAlbmIMG;
        public final TextView PLMusicTitle;
        public final TextView PLArtisttName;
        public final Button optionButton;
        public final LinearLayout PlayMusicByTitle;


        public ItemViewHolder(View v) {
            super(v);
            PLMusicTitle = (TextView) v.findViewById(R.id.PL_title);
            PLArtisttName = (TextView) v.findViewById(R.id.PL_artist);
            PLAlbmIMG = (ImageView) v.findViewById(R.id.PL_albumart);
            PLDragButton = (Button) v.findViewById(R.id.PL_drag);
            optionButton = (Button) itemView.findViewById(R.id.playlist_option);
            PlayMusicByTitle = (LinearLayout) v.findViewById(R.id.playlist_play_music);

        }


        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

        }


    }


}
