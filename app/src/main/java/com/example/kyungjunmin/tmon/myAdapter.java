
package com.example.kyungjunmin.tmon;

import android.content.ContentUris;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by KyungJunMin on 2017. 7. 18..
 */

public class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder>{

    Realm realm;
    private RealmResults<AudioItem> mDataset;
    Context context;
    private final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
    private List<AudioItem> albumList;
    public int viewType;
    private RealmQuery<AudioItem> query;
    private RealmResults<AudioItem> results;
    int nextIndex;
    public  AudioServiceInterface mInterface;

    public myAdapter(List<AudioItem> items , int viewType, Context context, AudioServiceInterface mInterface){
        Log.e("MY ADAPTER : onCreateViewHolder","MYADAPTER CONSTRUCTOR");
        this.albumList = items;
        this.viewType = viewType;
        this.context =  context;
        this.mInterface = mInterface;
        mDataset = null;


    }

    public void setAlbumList(List<AudioItem> list){
        this.albumList = list;
    }

    public void setmDataset(RealmResults<AudioItem> mDataset) {
        this.mDataset = mDataset;
    }
    /**
     * 레이아웃을 만들어서 Holer에 저장
     * @param viewGroup
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        realm = Realm.getDefaultInstance();

        View v;
        if(viewType ==1){
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_audio_card, viewGroup, false);
        }
        else {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_audio, viewGroup, false);
        }
        //마지막 인덱스 받기
        if(realm.isEmpty()){
            //마지막 인덱스가 비었다면
            nextIndex = 0;
        }else{
            //마지막 인덱스가 비지 않았다면
            nextIndex = realm.where(AudioItem.class).max("INDEX").intValue()+1;
        }
        return new ViewHolder(v);
    }

    /**
     * listView getView 를 대체
     * 넘겨 받은 데이터를 화면에 출력하는 역할
     *
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        final AudioItem myItem = albumList.get(position);
        viewHolder.MusicTitle.setText(myItem.getmTitle());
        viewHolder.ArtisttName.setText(myItem.getmArtist());
        final Uri albumArtUrl = ContentUris.withAppendedId(artworkUri, myItem.getmAlbumId());
        Picasso.with(viewHolder.itemView.getContext()).load(albumArtUrl).error(R.mipmap.ic_empty_albumart).into(viewHolder.img);

        final long mId = myItem.getmId();

        //버튼 클릭 이벤트
        viewHolder.optionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                PopupMenu libPopUp=new PopupMenu(context, v);
                libPopUp.getMenuInflater().inflate(R.menu.lib_popup, libPopUp.getMenu());
                libPopUp.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.lib_add:

                                //아이디로 검색해보고
                                AudioItem existanceChecker = realm.where(AudioItem.class).equalTo("mId", mId).findFirst();
                                if(existanceChecker == null){
                                    //없으면 추가함
                                    //램디비에 추가하면됨
                                    final AudioItem newItem = new AudioItem();
                                    newItem.setmIndex(nextIndex);
                                    nextIndex++;
                                    newItem.setmId(myItem.getmId());
                                    newItem.setmAlbumId(myItem.getmAlbumId());
                                    newItem.setmTitle(myItem.getmTitle());
                                    newItem.setmArtist(myItem.getmArtist());
                                    newItem.setmAlbum(myItem.getmAlbum());
                                    newItem.setmDuration(myItem.getmDuration());
                                    newItem.setmDataPath(myItem.getmDataPath());
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realm.copyToRealmOrUpdate(newItem);
                                            realm.copyToRealmOrUpdate(newItem);
                                        }
                                    });
                                    mInterface.addPlayList(mId);
                                    break;
                                }
                                else{
                                    //있으면 토스트*******
                                    break;
                                }

                            case R.id.lib_del:
                                //라이브러리 목록에서 삭제하면 됨
                                Log.d("libMenuClick","라이브러리 목록에서 삭제하면 됨");
//
//                                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mId);
//                                context.getContentResolver().delete(uri, null, null);
//                                notifyItemRemoved(position);

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
    public int getItemViewType(int position){

        if(this.viewType == 0){
            return 0;
        }
        else{
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    /**
     * 뷰 재활용을 위한 viewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{

        //버튼 선언
        public Button optionButton;
        public ImageView img;
        public TextView MusicTitle;
        public TextView ArtisttName;

        public ViewHolder(View itemView){
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.img_albumart);
            MusicTitle = (TextView) itemView.findViewById(R.id.txt_title);
            ArtisttName = (TextView) itemView.findViewById(R.id.artist_name);
            optionButton = (Button) itemView.findViewById(R.id.popUpButton);
        }
    }
}