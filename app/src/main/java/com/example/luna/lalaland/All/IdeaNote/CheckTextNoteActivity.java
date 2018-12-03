package com.example.luna.lalaland.All.IdeaNote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.example.luna.lalaland.R;

import butterknife.BindView;
import butterknife.ButterKnife;
/*
* stt+텍스트 메모를 확인할 때 이동하는 액티비티
* 저장된 제목과 내용을 가져와서 보여주면 된다.
* */

public class CheckTextNoteActivity extends AppCompatActivity {

    private final static String TAG = CheckTextNoteActivity.class.getSimpleName();
    String title, content;

    @BindView(R.id.btn_save)
    Button btn_save;
    @BindView(R.id.et_title)
    EditText et_title;
    @BindView(R.id.et_content)
    EditText et_content;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_text_note);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        content = intent.getStringExtra("content");
        Log.d(TAG, "텍스트 메모 제대로 가져왔는지 "+title+content);

//        et_content = findViewById(R.id.et_content);

        //가져온 메모 붙여주기
        if(!TextUtils.isEmpty(title)) {
            et_title.setText(title);
        }

        if(!TextUtils.isEmpty(content)){
            et_content.setText(content);
        }


    }//onCreate

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}//CheckTextNoteActivity
