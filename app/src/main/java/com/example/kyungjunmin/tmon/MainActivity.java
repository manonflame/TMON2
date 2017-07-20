package com.example.kyungjunmin.tmon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    private final static int LOADER_ID = 0x001;
    public static final String TAG = "mTag";
    private Menu menu;

    boolean CardViewState = true;


    //슬라이딩레이아웃
    private SlidingUpPanelLayout mLayout;

    //슬라이드가 밑에 있을 때의 플레이 버튼
    public Button slidePlay;

    //슬라이드가 올라왔을 때의 옵션과 리스트 표시 버튼
    public Button slideOption;
    public Button slideList;



    //레이아웃 관련 인스턴스들
    RecyclerView.LayoutManager layoutManager;
    private myAdapter mAdapter;
    private RecyclerView mRecyclerView;

    List<AudioItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            }else{
                // 외부 저장소 읽기에 관한 권한이 있음
                // 미디어 스토어에서 아이템 받기
                LoadMusic loadMusic = new LoadMusic(this);
                list = loadMusic.getMusicList();
            }
        }else{
            //OS가 Marshmallow일 경우 권한체크를 하지 않는다
            //미디어 스토어에서 아이템 받기
            LoadMusic loadMusic = new LoadMusic(this);
            list = loadMusic.getMusicList();
        }



        //버튼들 연결
        slidePlay = (Button)findViewById(R.id.slide_play);
        slideOption = (Button)findViewById(R.id.slide_openned_option);
        slideList = (Button)findViewById(R.id.go_to_list);






        //리사이클러 뷰 초기에 띄우기
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new myAdapter(list, 1, this);
        mRecyclerView.setAdapter(mAdapter);

        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);



        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.d(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.d(TAG, "onPanelStateChanged " + newState);

                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED){
                    slidePlay.setVisibility(GONE);
                    slideOption.setVisibility(VISIBLE);
                    slideList.setVisibility(VISIBLE);
                }
                else{
                    slidePlay.setVisibility(VISIBLE);
                    slideOption.setVisibility(GONE);
                    slideList.setVisibility(GONE);
                }

            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

            }
        });




    }











    //메뉴바 관련 함수들
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
            // 외부 스토리지 읽기에 관한 권한 획득
            getAudioListFromMediaDatabase();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            //서치 버튼 눌렸을 때 처리
            System.out.println("서치 눌렀을 때 처리");
            return true;
        }

        else {
            if (id == R.id.action_viewChange) {
                //뷰 바꿈 버튼 눌렀을 때 처리
                System.out.println("뷰 바꿈 눌렀을 때 처리");

                //뷰 변경 버튼의 상태를 변경
                if (CardViewState == true) {
                    CardViewState = false;
                } else {
                    CardViewState = true;
                }

                //뷰 변경 버튼의 상태에 따라 이미지 변경
                if (CardViewState == true) {
                    Log.e("card view state", "true");

                    menu.findItem(R.id.action_viewChange).setIcon(R.drawable.ic_view_change);

                    mAdapter = new myAdapter(list, 1, this);
                    mRecyclerView.setAdapter(mAdapter);
                    layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
                    mRecyclerView.setLayoutManager(layoutManager);
                } else {
                    Log.e("card view state", "false");

                    mAdapter = new myAdapter(list, 0, this);
                    mRecyclerView.setAdapter(mAdapter);
                    menu.findItem(R.id.action_viewChange).setIcon(R.mipmap.action_cardview);
                    layoutManager = new LinearLayoutManager(this);
                    mRecyclerView.setLayoutManager(layoutManager);
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void getAudioListFromMediaDatabase() {
        getSupportLoaderManager().initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                System.out.println("onCreateLoader()");
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA
                };
                String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";
                String sortOrder = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC";
                return new CursorLoader(getApplicationContext(), uri, projection, selection, null, sortOrder);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                System.out.println("onLoadFinished()");
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                System.out.println("onLoaderReset()");
            }
        });
    }
}
