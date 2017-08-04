
package com.example.kyungjunmin.tmon;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
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

import io.realm.Realm;

/**
 * Created by KyungJunMin on 2017. 7. 21..
 */

public class RealmAdapter extends RecyclerView.Adapter<RealmAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {

    private ArrayList<Long> AudioIds;
    private final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
    private final OnStartDragListener mDragStartListener;
    private final OnCustomerListChangedListener mListChangedListener;
    public BroadcastReceiver mBCReceiver;
    public IntentFilter filter;
    public IntentFilter filter2;
    public  AudioServiceInterface mInterface;
    Context mContext;
    Realm realm;



    public RealmAdapter(OnStartDragListener dragLlistener,
                        OnCustomerListChangedListener listChangedListener, Context context, final AudioServiceInterface mInterface) {
        mDragStartListener = dragLlistener;
        mListChangedListener = listChangedListener;
        mContext = context;
        this.mInterface = mInterface;

        AudioIds = mInterface.getmAudioIds();
        realm = Realm.getDefaultInstance();



        mBCReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(BroadcastActions.PREPARED)){

                    int positionGottenByService = mInterface.getmCurrentPosition();
                    int positionHasBeingGottenByService = mInterface.getmPastPosition();
                    notifyItemChanged(positionGottenByService);
                    notifyItemChanged(positionHasBeingGottenByService);
                }
                if(action.equals(BroadcastActions.PLAY_STATE_CHANGED)){
                    int positionGottenByService = mInterface.getmCurrentPosition();
                    notifyItemChanged(positionGottenByService);
                }
            }
        };
        filter = new IntentFilter(BroadcastActions.PREPARED);
        filter2 = new IntentFilter(BroadcastActions.PLAY_STATE_CHANGED);
        mContext.registerReceiver(mBCReceiver, filter);
        mContext.registerReceiver(mBCReceiver, filter2);
    }



    @Override
    public int getItemCount() {
        return AudioIds.size();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
        ItemViewHolder holder = new ItemViewHolder(v);


        return holder;
    }


    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {

        final AudioItem selectedItem = realm.where(AudioItem.class).equalTo("mId", AudioIds.get(position)).findFirst();

        holder.PLMusicTitle.setText(selectedItem.getmTitle());
        holder.PLArtisttName.setText(selectedItem.getmArtist());
        Uri albumArtUrl = ContentUris.withAppendedId(artworkUri, selectedItem.getmAlbumId());
        Picasso.with(holder.itemView.getContext()).load(albumArtUrl).error(R.mipmap.ic_empty_albumart).into(holder.PLAlbmIMG);

        int positionGottenByService = mInterface.getmCurrentPosition();
        int positionHasBeingGottenByService = mInterface.getmPastPosition();
        if(position == positionGottenByService){
            if(mInterface.isPlaying()){
                holder.PLArtisttName.setTextColor(Color.RED);
            }else{
                holder.PLArtisttName.setTextColor(Color.BLUE);
            }

        }else{
            holder.PLArtisttName.setTextColor(Color.BLACK);
        }

        //리스너 구현 -- 드래그
        holder.PLDragButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });

        holder.PlayMusicByTitle.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                mInterface.play(position);
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
                                int nextPos;
                                if(mInterface.getmCurrentPosition() == position){
                                    //현재 재생중인 노래가 있다면
                                    //순차 재생 다음노래 틀고

                                    //램디에서 삭제
                                    final Realm realm;
                                    realm = Realm.getDefaultInstance();
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            selectedItem.deleteFromRealm();
                                            mInterface.getmAudioIds().remove(position);
                                        }
                                    });
                                    if (AudioIds.size() - 1 > position) {
                                        // 다음 포지션으로 이동.
                                        nextPos = position;
                                        mInterface.play(nextPos);
                                    } else {
                                        // 처음 포지션으로 이동.
                                        nextPos = 0;
                                        mInterface.play(nextPos);
                                    }
                                    //서비스의 현재 포지션 조정
                                    if(nextPos == 0){
                                        mInterface.setmCurrentPosition(0);
                                        notifyDataSetChanged();
                                    }
                                    else{
                                        mInterface.setmCurrentPosition(nextPos);
                                        notifyItemChanged(position);
                                    }
                                }
                                else{
                                    //현재 재생중인 노래가 없다면
                                    //램디에서 삭제
                                    final Realm realm;
                                    realm = Realm.getDefaultInstance();
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            selectedItem.deleteFromRealm();
                                            //서비스의 아이디 목록 최신화
                                            mInterface.getmAudioIds().remove(position);
                                            notifyDataSetChanged();
                                        }
                                    });
                                }
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
        final AudioItem selectedItem = realm.where(AudioItem.class).equalTo("mId", AudioIds.get(fromPosition)).findFirst();
        final AudioItem changedItem = realm.where(AudioItem.class).equalTo("mId", AudioIds.get(toPosition)).findFirst();
        final int beforeIndex = selectedItem.getmIndex();
        final int afterIndex = changedItem.getmIndex();

        Realm realm;
        realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                boolean isShuffle = mInterface.getSHUFFLE();
                if(isShuffle){
                    //교차 상태의 교환
                    Collections.swap(mInterface.getmAudioIds(),fromPosition,toPosition);
                }
                else {
                    //순차 상태의 교환
                    selectedItem.setmIndex(afterIndex);
                    changedItem.setmIndex(beforeIndex);
                    realm.copyToRealmOrUpdate(selectedItem);
                    realm.copyToRealmOrUpdate(changedItem);
                    Collections.swap(mInterface.getmAudioIds(),fromPosition,toPosition);
                }
            }
        });
        AudioIds = mInterface.getmAudioIds();
        if(mInterface.getmCurrentPosition() == fromPosition){
            mInterface.setmCurrentPosition(toPosition);
        }
        notifyItemMoved(fromPosition, toPosition);
        notifyItemChanged(fromPosition);
        notifyItemChanged(toPosition);
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
        public void onItemSelected() {}

        @Override
        public void onItemClear() {}
    }
}