package com.example.kyungjunmin.tmon;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
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

/**
 * Created by KyungJunMin on 2017. 7. 18..
 */

public class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {


    Context context;
    private final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
    private List<AudioItem> albumList;
    public int viewType;


    public myAdapter(List<AudioItem> items , int viewType, Context context){
        Log.d("myAdapter생성자 ","지금 들어온 뷰타입 - " + viewType);
        this.albumList = items;
        this.viewType = viewType;
        this.context =  context;
    }

    /**
     * 레이아웃을 만들어서 Holer에 저장
     * @param viewGroup
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {


        View v;
        if(viewType ==1){
            Log.d("onCreateViewHolder()","listitem_audio_card 연결");
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_audio_card, viewGroup, false);
        }
        else {
            Log.d("onCreateViewHolder()","listitem_audio 연결");
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_audio, viewGroup, false);
        }
        Log.d("onCreateViewHolder()","viewType ::: "+viewType);
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
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        AudioItem item = albumList.get(position);
        viewHolder.MusicTitle.setText(item.mTitle);
        viewHolder.ArtisttName.setText(item.mArtist);
        Uri albumArtUrl = ContentUris.withAppendedId(artworkUri, item.mAlbumId);
        Picasso.with(viewHolder.itemView.getContext()).load(albumArtUrl).error(R.mipmap.ic_empty_albumart).into(viewHolder.img);


        //버튼 클릭 이벤트
        viewHolder.optionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("버튼","압싄 버튼이 눌렸습니다");
                PopupMenu libPopUp=new PopupMenu(context, v);

                libPopUp.getMenuInflater().inflate(R.menu.lib_popup, libPopUp.getMenu());

                libPopUp.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.lib_add:
                                //램디비에 추가하면됨
                                Log.d("libMenuClick","램디비에 추가하면됨");
                                break;
                            case R.id.lib_del:
                                //라이브러리 목록에서 삭제하면 됨
                                Log.d("libMenuClick","라이브러리 목록에서 삭제하면 됨");
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
