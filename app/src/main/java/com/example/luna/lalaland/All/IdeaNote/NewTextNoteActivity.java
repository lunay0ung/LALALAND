package com.example.luna.lalaland.All.IdeaNote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.luna.lalaland.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewTextNoteActivity extends AppCompatActivity {

    private final static String TAG = NewTextNoteActivity.class.getSimpleName();

    Button btn_save;
    EditText et_title, et_content;
    String title, content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_note_new);

        btn_save = findViewById(R.id.btn_save);
        et_title = findViewById(R.id.et_title);
        et_content = findViewById(R.id.et_content);

        btn_save.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                title = et_title.getText().toString();
                content = et_content.getText().toString();

                //제목이나 내용 둘 중 하나는 있어야 저장됨
                if(TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
                    Toast.makeText(NewTextNoteActivity.this, "저장할 내용이 없습니다.", Toast.LENGTH_SHORT).show();

                        //제목은 없고 내용만 있다면
                } else if (TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)) {
                        Log.d(TAG, "클릭1");
                        //메모를 저장할 때의 일시를 제목으로 만듦
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        String title = dateFormat.format(date);

                }

                        Intent intent = new Intent(getApplicationContext(), NoteHomeActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("content", content);
                        setResult(RESULT_OK, intent);
                        Log.d("텍스트 메모", title + content);
                        finish();


            }//onClick
        });//setOnClickListener

    }//onCreate


}//NewTextNoteActivity
