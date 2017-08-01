package com.example.kyungjunmin.tmon;

import android.Manifest;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements OnCustomerListChangedListener, OnStartDragListener{
    private final static int LOADER_ID = 0x001;
    public static final String TAG = "mTag";
    private Menu menu;


    boolean CardViewState = true;

    Realm realm;
    private RealmQuery<AudioItem> query;
    private RealmResults<AudioItem> results;

    int rIndex;


    LoadMusic loadMusic;

    //서치메뉴
    MenuItem searchMenuItem;
    SearchView searchView;

    //앨범 URI
    private final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");


    //슬라이딩레이아웃
    private SlidingUpPanelLayout mLayout;

    //슬라이드가 밑에 있을 때의 플레이 버튼
    public Button slidePlay;

    //슬라이드가 올라왔을 때의 레이아웃과 옵션과 플레이리스트 표시 버튼
    public LinearLayout slideUpLayout;
    public Button slideOption;
    public Button slideList;
    public Button slideUpRewind;
    public Button slideUpForward;
    public Button slideUpPlay;
    public Button slideUpShuffle;
    public Button slideUpRepeat;
    RelativeLayout slideContent;
    LinearLayout PLDisplay;
    RecyclerView.LayoutManager PLlayoutManager;
    public ImageView slideUpAlbumArt;
    public ImageView slideAlbumArt;
    public SeekBar slideUpSeekBar;
    public TextView slideUpSeekBarDuration;
    public TextView slideUpSeekBarProgress;
    public TextView slideTitle;
    public TextView slideArtist;


    //인텐트 리시버 관련
    private BroadcastReceiver mBCReceiver;
    private IntentFilter filter;
    private IntentFilter filter2;

    //플레이리스트 관련
    //플레이리스트 상태
    private boolean PlayListOn = false;
    //플레이리스트 리사이클러뷰
    private RecyclerView playlistVeiw;
    //렘디비어댑터
    private RealmAdapter rAdapter;
    private ItemTouchHelper mItemTouchHelper;



    int WhereCurPosIs = -1;


    SharedPreferences pref;



    //셔플 여부
    boolean isShuffle = false;



    //현재 재생목록 전체 뷰에 관한 레이아웃
    LinearLayout sLayout;


    //라이브러리 레이아웃 관련 인스턴스들
    RecyclerView.LayoutManager layoutManager;
    private myAdapter mAdapter;
    private RecyclerView mRecyclerView;
    List<AudioItem> list;
    List<AudioItem> entireList;


    class MyThread extends Thread {
        @Override
        public void run() { // 쓰레드가 시작되면 콜백되는 메서드
            // 씨크바 막대기 조금씩 움직이기 (노래 끝날 때까지 반복)
            while((!AudioApplication.getInstance().getServiceInterface().checkNull()) && AudioApplication.getInstance().getServiceInterface().isPlaying()) {
                slideUpSeekBar.setProgress(AudioApplication.getInstance().getServiceInterface().getmMediaPlayer().getCurrentPosition());
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d("onCreate()","start");
        Log.d("onCreate() startService ","startService");
        startService(new Intent(this, AudioService.class));

        //미디어 스토어 권한체크
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                Log.d("권한체크","권한 없음");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            }else{
                // 외부 저장소 읽기에 관한 권한이 있음
                // 미디어 스토어에서 아이템 받기
                loadMusic = new LoadMusic(this);
                list = loadMusic.getMusicList();
                entireList = list;
            }
        }else{
            //OS가 Marshmallow일 경우 권한체크를 하지 않는다
            //미디어 스토어에서 아이템 받기
            loadMusic = new LoadMusic(this);
            list = loadMusic.getMusicList();
            entireList = list;
        }

        Log.d("권한체크","권한이고 뭐고 진행");
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        isShuffle = pref.getBoolean("SHUFFLE", false);

        Log.d("onCreate의 isShuffle : ", ""+isShuffle);




        //버튼 리스너
        Button.OnClickListener onClickListener = new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                switch(view.getId()){
                    case R.id.go_to_list :
                        Log.d("onClick","재생목록 보여주기 or 끄기");
                        playlistOnOff();
                        break;

                    case R.id.slide_up_forward:
                        Log.d("onClick", "다음곡");
                        AudioApplication.getInstance().getServiceInterface().forward();
                        break;

                    case R.id.slide_up_play:
                        Log.d("onClick", "재생");
                        AudioApplication.getInstance().getServiceInterface().togglePlay();
                        break;

                    case R.id.slide_up_rewind:
                        Log.d("onClick","이전곡");
                        AudioApplication.getInstance().getServiceInterface().rewind();
                        break;

                    case R.id.slide_up_repeat:
                        Log.d("onClick","반복");
                        AudioApplication.getInstance().getServiceInterface().toggleRepeat();
                        break;

                    case R.id.slide_play:
                        Log.d("onClick 내려온 상태","재생");
                        AudioApplication.getInstance().getServiceInterface().togglePlay();
                        break;

                    case R.id.slide_up_shuffle:
                        Log.d("onClick","셔플");
                        SharedPreferences.Editor editor = pref.edit();
                        if(isShuffle){
                            //순차재생으로
                            Log.d("onClick","교차 -> 순차");
                            isShuffle = false;
                            editor.putBoolean("SHUFFLE",false);
                            editor.commit();
                            AudioApplication.getInstance().getServiceInterface().setSHUFFLE(isShuffle);
                            slideUpShuffle.setBackgroundResource(R.mipmap.ic_shuffle_off);

                            query = realm.where(AudioItem.class);
                            results = query.findAll();
                            results = results.sort("INDEX", Sort.ASCENDING);



                            int size = results.size();
                            //ㄴ새 아이디 리스트
                            ArrayList<Long> newAudioIds = AudioApplication.getInstance().getServiceInterface().getmAudioIds();
                            for(int i = 0 ; i < size ; i++){
                                long curId = results.get(i).getmId();
                                if(curId == AudioApplication.getInstance().getServiceInterface().getAudioItem().getmId()){
                                    Log.d("순차 -> 교차","새 진행준 포지션"+i);
                                    AudioApplication.getInstance().getServiceInterface().setmCurrentPosition(i);
                                }
                                newAudioIds.set(i, curId);
                            }
                            //서비스 ids변경
                            AudioApplication.getInstance().getServiceInterface().setPlayList(newAudioIds);




                        }
                        else{
                            //교차재생으로
                            Log.d("onClick","순차 -> 교차");
                           isShuffle = true;
                            editor.putBoolean("SHUFFLE",true);
                            editor.commit();
                            AudioApplication.getInstance().getServiceInterface().setSHUFFLE(isShuffle);
                            slideUpShuffle.setBackgroundResource(R.mipmap.ic_shuffle_on);

                            ArrayList<Long> originAudioIds = AudioApplication.getInstance().getServiceInterface().getmAudioIds();
                            ArrayList<Long> newAudioIds = AudioApplication.getInstance().getServiceInterface().getmAudioIds();
                            int size = newAudioIds.size();

                            long seed = System.nanoTime();

                            int whereCurId = -1;

                            for(int i = 0 ; i < size ; i ++) {
                                Log.d("origin : " , originAudioIds.get(i)+"");
                            }

                            //재생 중인 음악이 있을 때 섞기
                            for(int i = 0 ; i < size ; i++){
                                if(originAudioIds.get(i) == AudioApplication.getInstance().getServiceInterface().getAudioItem().getmId()){
                                    whereCurId = i;
                                }
                            }

                            AudioApplication.getInstance().getServiceInterface().setmCurrentPosition(0);

                            Collections.swap(newAudioIds, 0 , whereCurId);
                            long ZeroId = newAudioIds.remove(0);
                            Collections.shuffle(newAudioIds, new Random(seed));
                            newAudioIds.add(0, ZeroId);

                            for(int i = 0 ; i < size ; i ++){
                                Log.d("newAudioIds", ""+ newAudioIds.get(i));
                            }

                            AudioApplication.getInstance().getServiceInterface().setPlayList(newAudioIds);



                            Log.d("AudioService - onDestroy()","셔플 상태 저장");
                            JSONArray jsonArray = new JSONArray();
                            for (int i = 0; i < AudioApplication.getInstance().getServiceInterface().getmAudioIds().size(); i++) {
                                jsonArray.put(AudioApplication.getInstance().getServiceInterface().getmAudioIds().get(i));
                            }
                            if (!AudioApplication.getInstance().getServiceInterface().getmAudioIds().isEmpty()) {
                                Log.d("SERVICE onDestroy", "쉐어드PF의 오디오 리스트가 null입니다.");
                                editor.putString("TheLastShuffleList", jsonArray.toString());
                            } else {
                                editor.putString("TheLastShuffleList", null);
                            }


                            //포지션하고 아이디도 저장해야함 ******
                            editor.putLong("TheLastId", AudioApplication.getInstance().getServiceInterface().getAudioItem().getmId());
                            editor.putInt("TheLastPosition", AudioApplication.getInstance().getServiceInterface().getmCurrentPosition());
                            editor.apply();




                        }
                        break;
                }
            }
        };


        //슬라이드업에 따른 레이아웃과 버튼들 연결
        slideUpLayout = (LinearLayout)findViewById(R.id.slide_up_layout);
        slidePlay = (Button)findViewById(R.id.slide_play);
        slidePlay.setOnClickListener(onClickListener);
        slideOption = (Button)findViewById(R.id.slide_openned_option);
        slideOption.setOnClickListener(onClickListener);
        slideList = (Button)findViewById(R.id.go_to_list);
        slideList.setOnClickListener(onClickListener);
        slideUpRewind = (Button)findViewById(R.id.slide_up_rewind);
        slideUpRewind.setOnClickListener(onClickListener);
        slideUpPlay = (Button)findViewById(R.id.slide_up_play);
        slideUpPlay.setOnClickListener(onClickListener);
        slideUpForward = (Button)findViewById(R.id.slide_up_forward);
        slideUpForward.setOnClickListener(onClickListener);
        slideUpRepeat = (Button)findViewById(R.id.slide_up_repeat);
        slideUpRepeat.setOnClickListener(onClickListener);
        slideUpShuffle = (Button)findViewById(R.id.slide_up_shuffle);
        slideUpShuffle.setOnClickListener(onClickListener);

        if(isShuffle){
            slideUpShuffle.setBackgroundResource(R.mipmap.ic_shuffle_on);
        }
        else{
            slideUpShuffle.setBackgroundResource(R.mipmap.ic_shuffle_off);
        }

        slideUpAlbumArt = (ImageView) findViewById(R.id.slide_up_albumart);
        slideAlbumArt = (ImageView) findViewById(R.id.slide_albumart);
        slideUpSeekBar = (SeekBar) findViewById(R.id.slide_up_seekbar);
        slideUpSeekBarDuration = (TextView)findViewById(R.id.slide_up_seekbar_duration);
        slideUpSeekBarProgress = (TextView)findViewById(R.id.slide_up_seekbar_progress);
        slideTitle = (TextView)findViewById(R.id.slide_title);
        slideArtist = (TextView)findViewById(R.id.slide_artist);


        //리사이클러 뷰 초기에 띄우기
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new myAdapter(list, 1, this);
        mRecyclerView.setAdapter(mAdapter);
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);


        //플레이리스트의 리사이클러뷰 띄우기
        playlistVeiw = (RecyclerView)findViewById(R.id.PL_recyclerView);
        slideContent = (RelativeLayout)findViewById(R.id.slide_content);
        PLDisplay = (LinearLayout)findViewById(R.id.PL_display);
        PLlayoutManager = new LinearLayoutManager(this);
        playlistVeiw.setLayoutManager(PLlayoutManager);



        //재생목록의 리사이클러뷰 비저블러티 설정 및 어댑터 연결
        //램 초기화
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(realmConfiguration);
        realm = Realm.getDefaultInstance();



        slideUpSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    AudioApplication.getInstance().getServiceInterface().getmMediaPlayer().seekTo(progress);
                }
                int m = progress / 60000;
                int s = (progress % 60000) / 1000;
                String strTime = String.format("%02d:%02d", m, s);
                slideUpSeekBarProgress.setText(strTime);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });




        //슬라이드업패널 관련
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.d(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.d(TAG, "onPanelStateChanged " + newState);

                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED){

                    slidePlay.setVisibility(GONE);
                    slideUpLayout.setVisibility(VISIBLE);

                    //켠 상태라면
                    if(PlayListOn){
                        //리사이클러 뷰 켬
                        //리사이클러뷰 다시 그리기.
                        makeRealmAdapter();

                        //아이콘 빨갛게
                        slideList.setBackgroundResource(R.mipmap.ic_list_on);
                        slideContent.setVisibility(GONE);
                        PLDisplay.setVisibility(VISIBLE);
                    }

                }
                else{
                    slidePlay.setVisibility(VISIBLE);
                    slideUpLayout.setVisibility(GONE);
                }
            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });



        //인텐트
        mBCReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                AudioItem nowPlayingItem = AudioApplication.getInstance().getServiceInterface().getAudioItem();
                Uri albumArtUrl = ContentUris.withAppendedId(artworkUri, nowPlayingItem.getmAlbumId());


                Picasso.with(slideUpAlbumArt.getContext()).load(albumArtUrl).error(R.mipmap.ic_empty_albumart).into(slideUpAlbumArt);
                Picasso.with(slideAlbumArt.getContext()).load(albumArtUrl).error(R.mipmap.ic_empty_albumart).into(slideAlbumArt);

                SharedPreferences.Editor editor = pref.edit();
                editor.putLong("TheLastId", AudioApplication.getInstance().getServiceInterface().getAudioItem().getmId());
                editor.putInt("TheLastPosition", AudioApplication.getInstance().getServiceInterface().getmCurrentPosition());
                editor.apply();


                slideTitle.setText(nowPlayingItem.getmTitle());
                slideArtist.setText(nowPlayingItem.getmArtist());

                int musicDuration = (int) nowPlayingItem.getmDuration();
                slideUpSeekBar.setMax(musicDuration);
                int m = musicDuration / 60000;
                int s = (musicDuration % 60000) / 1000;
                String strTime = String.format("%02d:%02d", m, s);
                slideUpSeekBarDuration.setText(strTime);
                Log.d("MAIN BROADCAST RECEIVER", "getMAX : "  + slideUpSeekBar.getMax());

                new MyThread().start();


                Log.d("MAIN BROADCAST RECEIVER", "현재 받은 인텐트는 "+action + "입니다");
                if(AudioApplication.getInstance().getServiceInterface().isPlaying()){
                    Log.d("MAIN BROADCAST RECEIVER", "재생 중이라 아이콘 바꿈");
                    slidePlay.setBackgroundResource(R.mipmap.ic_pause);
                    slideUpPlay.setBackgroundResource(R.mipmap.ic_pause_circle);

                }else{
                    Log.d("MAIN BROADCAST RECEIVER", "정지 중이라 아이콘 바꿈");
                    slidePlay.setBackgroundResource(R.mipmap.ic_play);
                    slideUpPlay.setBackgroundResource(R.mipmap.ic_play_circle);
                }


            }
        };
        filter = new IntentFilter(BroadcastActions.PREPARED);
        filter2 = new IntentFilter(BroadcastActions.PLAY_STATE_CHANGED);
        this.registerReceiver(mBCReceiver, filter);
        this.registerReceiver(mBCReceiver, filter2);



    }


    public void playlistOnOff(){
        if(PlayListOn){
            //켠 상태라면
            PlayListOn = false;

            //리사이클러 뷰 끔
            Log.d("onClick","리사이클러뷰(재생목록 끔");
            //아이콘 까맣게
            slideList.setBackgroundResource(R.mipmap.ic_list_off);
            slideContent.setVisibility(VISIBLE);
            PLDisplay.setVisibility(GONE);

        }
        else{
            //끈 상태라면
            PlayListOn = true;
            //리사이클러 뷰 켜고
            Log.d("onClick","리사이클러뷰(재생목록 켬");

            //리사이클러뷰 다시 그리기.
            makeRealmAdapter();

            //아이콘 빨갛게
            slideList.setBackgroundResource(R.mipmap.ic_list_on);
            slideContent.setVisibility(GONE);
            PLDisplay.setVisibility(VISIBLE);
        }
    }

    void realmRecyclerSetting() {
        //쿼리를 받아옴
        query = realm.where(AudioItem.class);
        results = query.findAll();
        mAdapter.setmDataset(results);
        rIndex = results.size();

        Log.d("realmRecyclerSetting()'s realmSize", String.valueOf(rIndex));

    }

    void makeRealmAdapter(){
        rAdapter = new RealmAdapter(this, this, this);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(rAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(playlistVeiw);
        playlistVeiw.setAdapter(rAdapter);
    }



    @Override
    protected void onPause() {
        super.onPause();
        if(realm.getDefaultInstance() != null) {
            realm.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        realm = Realm.getDefaultInstance();
        realmRecyclerSetting();
        Log.d("onResume()","END");
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
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        AudioApplication.getInstance().getServiceInterface().checkNull();
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint("검색어");
        searchMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        searchView.setSearchableInfo(searchManager.
                getSearchableInfo(getComponentName()));


        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                list = entireList;
                mAdapter.setAlbumList(list);
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });




        //서치 버튼 눌렸을 때 처리
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                //서치 버튼 눌렸을 때 처리
                searchView.setIconified(true);
                searchView.clearFocus();
                searchView.setQuery("'"+searchWord+"' 검색 결과", false);
                menu.findItem(R.id.action_viewChange).setVisible(true);
                List<AudioItem> ret = loadMusic.getSearchAudioList(searchWord);
                list = ret;
                mAdapter.setAlbumList(list);
                mAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

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
            return true;
        }

        else {
            if (id == R.id.action_viewChange) {

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

                    menu.findItem(R.id.action_viewChange).setIcon(R.mipmap.action_cardview);
                    mAdapter = new myAdapter(list, 0, this);
                    mRecyclerView.setAdapter(mAdapter);
                    layoutManager = new LinearLayoutManager(this);
                    mRecyclerView.setLayoutManager(layoutManager);
                }
                mAdapter.notifyDataSetChanged();
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

    @Override
    public void onNoteListChanged(List<AudioItem> realmDataSets) {
        //after drag and drop operation, the new list of Customers is passed in here
        //create a List of Long to hold the Ids of the
        //Customers in the List
        List<Long> listOfSortedAudioItemId = new ArrayList<Long>();

        for (AudioItem audioItem: realmDataSets){
            listOfSortedAudioItemId.add(audioItem.getmId());
        }

        Log.d("onNoteListChanged()", "enteringCheck");

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {

        mItemTouchHelper.startDrag(viewHolder);
    }


    @Override
    protected void onDestroy(){
        Log.d("MAIN ONDESTROY","START DESTROY");
        unregisterReceiver(mBCReceiver);
        super.onDestroy();
    }

}
