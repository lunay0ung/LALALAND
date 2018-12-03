package com.example.luna.lalaland.All.MusicVideo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.luna.lalaland.R;
/*
* 최신곡의 앨범 자켓 등을 이용해 이미지 리스트를 만든 후
* 각 이미지를 카메라에 인식시키면
* 해당 앨범/이미지와 관련된 뮤직 비디오를 AR로 볼 수 있는 액티비티
*
* */
public class WatchMVActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_mv);

        initViews(); /*레이아웃 객체 초기화*/



    }//onCreate

    /*레이아웃 객체 초기화*/
    private void initViews() {

    }//defaultViews

}//WatchMVActivity
