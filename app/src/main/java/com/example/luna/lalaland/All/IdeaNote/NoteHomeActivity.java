package com.example.luna.lalaland.All.IdeaNote;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.example.luna.lalaland.All.AccountManagement.SharedPrefManager;
import com.example.luna.lalaland.All.AccountManagement.SignInActivity;
import com.example.luna.lalaland.All.AccountManagement.User;
import com.example.luna.lalaland.All.Blockchain.MyWalletActivity;
import com.example.luna.lalaland.All.Blockchain.WalletLoginActivity;
import com.example.luna.lalaland.All.BuySongs_Kakaopay.BuySongsActivity;
import com.example.luna.lalaland.All.MusicVideo.WatchMVActivity;
import com.example.luna.lalaland.All.Trending.TrendingActivity;
import com.example.luna.lalaland.All.Intro.HomeScreenActivity;
import com.example.luna.lalaland.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
*
* 아이디어 노트 목록을 볼 수 있는 액티비티
* -STT(speech-to-text) 기능을 이용하여 음성을 문자로 변환하여 메모하는 기능 및 음성 메모 기능 및 기본적인 텍스트 메모 기능을 구현할 예정
*
*
* */
public class NoteHomeActivity extends AppCompatActivity implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener, NoteAdapter.NoteAdapterListner{

    private final static String TAG = NoteHomeActivity.class.getSimpleName();
    private final static int REQUEST_STT = 1;
    private final static int REQUEST_VOICENOTE = 2;
    private final static int REQUEST_TEXTNOTE = 3;

    private final static int MODIFY_TEXTNOTE = 4;
    private final static int MODIFY_VOICENOTE =5;

    //플로팅 버튼 관련
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton btn_newMemo, fab_text, fab_voice, fab_stt; //btn_newMemo버튼을 누르면 텍스트, 음성녹음, stt 메모 버튼이 보이게 됨

    //툴바
    private Toolbar tb_toolBar;

    //내비게이션 뷰
    DrawerLayout drawer;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    //리사이클러뷰
    RecyclerView recyclerview_note;
    List<NoteItem> noteItemList;
    NoteAdapter noteAdapter;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    //노트를 구성하는 요소
    String title, content, fileName, audioUri;
    Boolean isVoiceNote;

    /*로그인한 유저 정보를 담고 있는 sharedPreference와 유저 정보를 담을 변수*/
    SharedPreferences pref;
    String email, username, age, gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_home);

        //로그인 여부를 판별, 로그인 안 되어있으면 앱을 이용할 수 없다
        if(!SharedPrefManager.getmInstance(this).isLoggedIn()) {
            Log.d(TAG, "로그인 되어 있지 않음");
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(this, SignInActivity.class));
        }

        Log.d(TAG, "로그인 되어 있음");

        final User user = SharedPrefManager.getmInstance(this).getUser(); // 현재 로그인 된 유저정보
        username = user.getUsername(); //유저네임
        email = user.getEmail();

        initViews(); //객체 초기화
        ItemTouchHelper(); //노트 슬라이드->삭제
        restoreState(); //저장된 메모 불러오기
    }//onCreate


    @Override
    public void onNoteItemSelected(NoteItem noteItem) {

        title = noteItem.title;
        content = noteItem.content;
        isVoiceNote = noteItem.isVoiceNote;

        Log.d(TAG, "오디오 메모인가? "+isVoiceNote);
        if(isVoiceNote) { //음성메모라면 파일네임도 받아옴
            fileName = noteItem.fileName;
            Log.d(TAG, "오디오 메모 파일명 확인: "+fileName);
        }
        Log.d(TAG, "메모 제목: "+title+"/메모 내용: "+content);

        //오디오 메모는 저장한 음성메시지를 들을 수 있는 액티비티로 이동시키고,
        //텍스트+stt 메모는 그냥 저장한 메모를 볼 수 있는 액티비티로 이동하면 된다
        if(isVoiceNote) {
            Intent voiceNote = new Intent(getApplicationContext(), CheckVoiceNoteActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            voiceNote.putExtra("fileName", fileName);
            voiceNote.putExtra("title", title);
            voiceNote.putExtra("content", content);
            startActivityForResult(voiceNote, MODIFY_VOICENOTE);
        } else {
            Intent textNote = new Intent(NoteHomeActivity.this, CheckTextNoteActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            textNote.putExtra("title", title);
            textNote.putExtra("content", content);
            startActivityForResult(textNote, MODIFY_VOICENOTE);
        }

    }//onNoteItemSelected


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){

            switch (requestCode){

                case REQUEST_TEXTNOTE:
                    title = data.getStringExtra("title");
                    content = data.getStringExtra("content");
                    Log.d(TAG, "텍스트 메모 내용 확인: "+title+"////"+content);

                    NoteItem textNote = new NoteItem(title, content, false);
                    noteItemList.add(textNote);
                    noteAdapter.notifyDataSetChanged();
                    break;

                case REQUEST_VOICENOTE:
                    title = data.getStringExtra("title");
                    content = data.getStringExtra("content");
                    audioUri = data.getStringExtra("audioUri");
                    Log.d(TAG,  "메모 w/오디오 내용 확인: "+title+"////"+content);
                    Log.d(TAG, "오디오 uri: "+audioUri);

                    NoteItem voiceNote;

                    //오디오파일이 있을 때
                    if(audioUri != null) {
                        Log.d(TAG, "오디오 uri: "+audioUri);
                        voiceNote = new NoteItem(audioUri, title, content, true);
                        noteItemList.add(voiceNote);
                        noteAdapter.notifyDataSetChanged();
                    }

                    //오디오파일이 없을 떄
                    if(audioUri == null) {
                        voiceNote = new NoteItem(title, content, false);
                        noteItemList.add(voiceNote);
                        noteAdapter.notifyDataSetChanged();
                    }
                    break;

                case REQUEST_STT:
                    title = data.getStringExtra("title");
                    content = data.getStringExtra("content");
                    Log.d(TAG, "stt 메모 내용 확인: "+title+"////"+content);

                        NoteItem sttNote = new NoteItem(title, content, false);
                        noteItemList.add(sttNote);
                        noteAdapter.notifyDataSetChanged();
                    break;


                //이미 작성된 메모를 클릭했을 때 startActivityForResult의 리퀘스트 코드
                //TODO 메모 수정기능 구현하는 건 중요하지 않은 것 같아서 미뤄둠 2018-10-26
                case MODIFY_TEXTNOTE:
                    break;

                case MODIFY_VOICENOTE:
                    break;
            }//switch
        } else {
            Log.d(TAG, "메모 오류 발생");
        }
    }//onActivityResult

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_newMemo:
                anim();
                break;
            case R.id.fab_text: //텍스트 메모 작성
                Intent textIntent = new Intent(getApplicationContext(), NewTextNoteActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(textIntent, REQUEST_TEXTNOTE);
                anim();
                break;
            case R.id.fab_voice: //보이스 메모 작성
                anim();
                Intent voiceIntent = new Intent(getApplicationContext(), NewVoiceNoteActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(voiceIntent, REQUEST_VOICENOTE);
                break;
            case R.id.fab_stt: //speech-to-text 음성->텍스트 변환 노트 작성
                anim();
                Intent sttIntent = new Intent(getApplicationContext(), NewSTTActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(sttIntent, REQUEST_STT);
                break;
        }//switch
    }//onClick


    //리사이클러뷰--밀어서 삭제하기 기능 구현
    private void ItemTouchHelper(){
        //슬라이드 -> 삭제
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder ViewHolder, RecyclerView.ViewHolder target) {
                return false;
            }//onMove

            @Override
            public void onSwiped(RecyclerView.ViewHolder ViewHolder, int swipeDir) {

                noteItemList.remove(ViewHolder.getAdapterPosition());
                noteAdapter.notifyItemRemoved(ViewHolder.getAdapterPosition());

            }//onSwiped
        };//simpleItemTouchCallback

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerview_note);
    }

    //sharedPreferences에 메모 저장
    //TODO Sqlite에 저장하는 것이 맞지만 일정상 일단 sharedpreferences에 저장하고 추후 여유가 되면 수정
    public void saveState() {

        SharedPreferences pref_memo = getSharedPreferences("note", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor_memo = pref_memo.edit();

        JSONArray jsonArray_memo = new JSONArray();

        //데이터의 크기만큼 jsonobject를 만든다
        for (int i = 0; i < noteItemList.size(); i++) {
            JSONObject jsonObject_memo = new JSONObject();


            String key_audio = "audio" + i;
            String key_title = "title" + i;
            String key_note = "content" + i;

            try {
                jsonObject_memo.put(key_title, noteItemList.get(i).getTitle());
                Log.e("제목 검사", key_title);
                jsonObject_memo.put(key_note, noteItemList.get(i).getContent());
                Log.e("내용 검사", key_note);

                String audioUri = noteItemList.get(i).getFileName();
                jsonObject_memo.put(key_audio, noteItemList.get(i).getFileName());
                jsonArray_memo.put(jsonObject_memo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }//for

        editor_memo.putString("noteDetail", jsonArray_memo.toString());
        editor_memo.commit();
        //테스트 메모 저장 세팅 끝
    }//saveState()

    public void restoreState() {
        //메모 꺼내오기
        SharedPreferences pref_memo = getSharedPreferences("note", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor_memo = pref_memo.edit();

        try {
            JSONArray jsonArray_memo = new JSONArray(pref_memo.getString("noteDetail", ""));
            Log.e("jsonArray복원", "jsonArray" + jsonArray_memo);
            if (jsonArray_memo.length() != 0) {
                editor_memo.clear();
                editor_memo.commit();
            }

            for (int i = 0; i < jsonArray_memo.length(); i++) {
                JSONObject jsonObject_memo = jsonArray_memo.getJSONObject(i);
                Log.e("데이터 검사", "" + jsonArray_memo.getJSONObject(i)); //여기까진 옴

                String audio;
                String key_audio = "audio" + i;

                if (!jsonObject_memo.isNull(key_audio)) { //오디오 키값이 없으면
                    audio = jsonObject_memo.getString(key_audio); //넣어준다

                }

                if (jsonObject_memo.has(key_audio)) {

                    Log.e("몇번올까?", "누가올까?" + jsonObject_memo);
                    String key_title = "title" + i;
                    String key_note = "content" + i;

                    String title = jsonObject_memo.getString(key_title);
                    String note = jsonObject_memo.getString(key_note);
                    audio = jsonObject_memo.getString(key_audio);

                    try {
                        noteAdapter.addItem(audio, title, note);
                        // memoRcvAdapter.addItem(new Item_memo(audio, title, note)); ...아 이것때문에 몇시간 날림 ㅡㅡ..

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("오냐마냐2", "kkkk");
                    String key_title = "title" + i;
                    String key_note = "content" + i;

                    String title = jsonObject_memo.getString(key_title);
                    String note = jsonObject_memo.getString(key_note);
                    noteAdapter.addItem(new NoteItem(title, note, false));

                }
            }//for
        } catch (JSONException e) {
            e.printStackTrace();
        }
        noteAdapter.notifyDataSetChanged();
        //메모 끝
    }//restoreState

    //객체 초기화 및 버튼-클릭 리스너 연결
    private void initViews() {
        tb_toolBar = findViewById(R.id.tb_toolBar);
        //툴바 커스텀
        setSupportActionBar(tb_toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //이걸 추가해주지 않으면 툴바에 앱명 lalaland가 나옴
        tb_toolBar.setTitle("Idea Note");

        //리사이클러뷰
        recyclerview_note = findViewById(R.id.recyclerview_note);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1); //리스트가 1행 2열로 배치되게 만들어줌
        recyclerview_note.setLayoutManager(staggeredGridLayoutManager);
        noteItemList = new ArrayList<>();
        noteAdapter = new NoteAdapter(this, noteItemList, this); //리사이클러뷰 어댑터
        //broadcastAdapter= new BroadcastAdapter(this, R.layout.broadcast_list, broadcastList); //굳이 이렇게 안 해도 되지만 참고위해 남김
        recyclerview_note.setAdapter(noteAdapter); //어댑터를 리사이클러뷰에 세팅

        //플로팅 버튼
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        btn_newMemo = (FloatingActionButton) findViewById(R.id.btn_newMemo);
        fab_text = (FloatingActionButton) findViewById(R.id.fab_text);
        fab_voice = (FloatingActionButton) findViewById(R.id.fab_voice);
        fab_stt = (FloatingActionButton) findViewById(R.id.fab_stt);

        btn_newMemo.setOnClickListener(this);
        fab_text.setOnClickListener(this);
        fab_voice.setOnClickListener(this);
        fab_stt.setOnClickListener(this);

        //내비게이션 뷰
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, tb_toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nav_header = navigationView.getHeaderView(0); //네비게이션 헤더 뷰
        TextView tv_navHeader = nav_header.findViewById(R.id.tv_navHeader);
        tv_navHeader.setText(username);
    }//initViews


    //floating button을 누르면 세 가지 버튼을 보여주는 애니메이션 처리
    public void anim() {

        if (isFabOpen) {
            fab_text.startAnimation(fab_close);
            fab_voice.startAnimation(fab_close);
            fab_stt.startAnimation(fab_close);
            fab_text.setClickable(false);
            fab_voice.setClickable(false);
            fab_stt.setClickable(false);
            isFabOpen = false;
        } else {
            fab_text.startAnimation(fab_open);
            fab_voice.startAnimation(fab_open);
            fab_stt.startAnimation(fab_open);
            fab_text.setClickable(true);
            fab_voice.setClickable(true);
            fab_stt.setClickable(true);
            isFabOpen = true;
        }//if
    }//anim

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_homeScreen:
                startActivity(new Intent(NoteHomeActivity.this, HomeScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;


            case R.id.menu_buyMusic:
                startActivity(new Intent(NoteHomeActivity.this, BuySongsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_musicVideo:
                startActivity(new Intent(NoteHomeActivity.this, WatchMVActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_lunToken:
                startActivity(new Intent(NoteHomeActivity.this, WalletLoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_chart:
                startActivity(new Intent(NoteHomeActivity.this, TrendingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_logout:
                /*sharedpreference에 저장된 현재 유저정보를 지움*/
                SharedPrefManager.getmInstance(getApplicationContext()).logout(email);
                finish();
                break;
        }//switch
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }//onNavigationItemSelected

    @Override //뒤로가기 버튼
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        saveState();//메모 저장
    }

}//NoteHomeActivity
