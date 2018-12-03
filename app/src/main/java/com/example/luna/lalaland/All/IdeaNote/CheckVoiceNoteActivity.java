package com.example.luna.lalaland.All.IdeaNote;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.luna.lalaland.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
* 음성메모를 확인할 때 이동하는 액티비티
* TODO 일정관리+우선순위 상 메모 수정기능은 구현하지 않는다 2018-10-26
* */
public class CheckVoiceNoteActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.btn_save)
    Button btn_save;
    @BindView(R.id.et_title)
    EditText et_title;
    @BindView(R.id.et_content)
    EditText et_content;
    @BindView(R.id.recFileFormatLayout)
    RelativeLayout recFileFormatLayout;
    @BindView(R.id.btn_playNotReady)
    Button btn_playNotReady;
    @BindView(R.id.btn_playReady)
    Button btn_playReady;
    @BindView(R.id.mPlayProgressBar)
    SeekBar mPlayProgressBar;
    @BindView(R.id.btn_delete)
    Button btn_delete;
    @BindView(R.id.tv_maxPoint)
    TextView tv_maxPoint;


    //미디어 플레이어 관련
    private static final int REC_STOP = 0;
    private static final int RECORDING = 1;
    private static final int PLAY_STOP = 0;
    private static final int PLAYING = 1;
    private static final int PLAY_PAUSE = 2;


    private int mRecState = REC_STOP;
    private int mPlayerState = PLAY_STOP;

    private MediaPlayer mPlayer = null;
    private int mCurRecTimeMs = 0;
    private int mCurProgressTimeDisplay = 0;

    private static String RECORDED_FILE;
    File file;

    private final static String TAG = CheckVoiceNoteActivity.class.getSimpleName();
    String title, content, fileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_voice_note);


        ButterKnife.bind(this);

        //메모수정기능은 보류한 상태이므로 음성메모 삭제기능도 보류
        btn_delete.setVisibility(View.GONE);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        content = intent.getStringExtra("content");
        fileName = intent.getStringExtra("fileName");

        Log.d(TAG, "오디오 메모 제대로 가져왔는지 "+"제목: "+title+"내용: "+content+"파일명: "+fileName);

        if(fileName == null){ //뭔가 문제가 생겨서 음성파일을 못 가져왔다면 플레이버튼의 색상을 통해 알 수 있다(가져왔을 경우 검정색, 아닐 경우 투명)
            btn_playNotReady.setVisibility(View.VISIBLE);
            btn_playReady.setVisibility(View.GONE);
        }

        //받아온 자료 세팅
        et_title.setText(title);
        et_content.setText(content);
        RECORDED_FILE = fileName;
        file = new File(RECORDED_FILE);

        btn_save.setOnClickListener(this);
        btn_playReady.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
    }//onCreate

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_playReady:
                Log.d(TAG, "오디오 플레이버튼 클릭");
                mBtnStartPlayOnClick();

                break;

                //TODO 일정관리상 + 현재 해야할일의 우선순위 상 메모를 수정하여 저장하는 기능은 구현하지 않는다 2018-10-26
            case R.id.btn_save:
                Toast.makeText(this, "준비 중!", Toast.LENGTH_SHORT).show();
                break;

            //TODO 메모 수정기능이 없으므로 삭제기능도 보류
            case R.id.btn_delete:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CheckVoiceNoteActivity.this);

                //제목 세팅
                alertDialogBuilder.setTitle("<녹음 파일 삭제여부 확인>");

                //알림창 내용 세팅
                alertDialogBuilder
                        .setMessage("파일을 정말 삭제하시겠어요?")
                        .setCancelable(false) //뒤로 버튼 클릭 시 취소가능설정 여부
                        .setPositiveButton("삭제 할래요", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                File file = new File(RECORDED_FILE);
                                Log.e("",""+file);
                                // 녹음 파일을 삭제한다
                                if( file.exists())
                                    Log.e("파일삭제",""+file.exists());
                                {


                                    file.delete();
                                    Log.e("파일있음?", ""+file.exists()); //exist에 대해 false, 즉 삭제 됨.

                                    btn_playNotReady.setVisibility(View.VISIBLE);
                                    btn_playReady.setVisibility(View.GONE);

                                    Toast.makeText(CheckVoiceNoteActivity.this, "음성 메모가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                    tv_maxPoint.setText("00:00");

                                    RECORDED_FILE= null; //여기서 null을 넣어도 소용없음, 어차피 저장하는 case는 delete위에 있잖아
                                    //  Log.e("파일삭제하면 뭐가들었나", RECORDED_FILE); //->nullpointerException
                                    //모든 뷰 객체를 삭제한다
//                                    mPlayProgressBar.setVisibility(View.GONE);
//                                    btn_playReady.setVisibility(View.GONE);
//                                    btn_delete.setVisibility(View.GONE);
//                                    mTvPlayMaxPoint.setVisibility(View.GONE);
                                }
                            }
                        })
                        .setNegativeButton("삭제 안 해요", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //알림창을 벗어난다
                                dialog.cancel();
                            }
                        });
                //다이얼로그 생성
                AlertDialog alertDialog = alertDialogBuilder.create();

                //다이얼로그 보여주기
                alertDialog.show();

                break;
        }//switch
    }//onClick

    //재생시작 버튼 클릭
    private void mBtnStartPlayOnClick()
    {
        if (mPlayerState == PLAY_STOP)
        {
            mPlayerState = PLAYING;
            startPlay();
            updateUI();
        } else if (mPlayerState == PLAYING)
        {
            mPlayerState = PLAY_STOP;
            stopPlay();
            updateUI();
        }
    }//mBtnStartPlayOnClick


    //재생시작
    private void startPlay()
    {
        //미디어 플레이어 생성
        if(mPlayer == null) {
            mPlayer = new MediaPlayer();
        } else {
            mPlayer.reset();
        }

        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this::onCompletion);
        //Register a callback to be invoked when the end of a media source has been reached during playback.

        try {
            File file2play = new File(RECORDED_FILE);
            //  String file2playStr = file2play.getAbsolutePath();
            //  Log.e("파일주소 확인", file2playStr);
            FileInputStream fis = new FileInputStream(file2play);
            FileDescriptor fd = fis.getFD();
            mPlayer.setDataSource(fd);
            mPlayer.prepare();

            int point = mPlayer.getDuration();
            mPlayProgressBar.setMax(point);

            int maxMinPoint = point / 1000 / 60;
            int maxSecPoint = (point / 1000) % 60;
            String maxMinPointStr = "";
            String maxSecPointStr = "";

            if (maxMinPoint < 10)
                maxMinPointStr = "0" + maxMinPoint + ":";
            else
                maxMinPointStr = maxMinPoint + ":";

            if (maxSecPoint < 10)
                maxSecPointStr = "0" + maxSecPoint;
            else
                maxSecPointStr = String.valueOf(maxSecPoint);

            tv_maxPoint.setText(maxMinPointStr + maxSecPointStr);

        } catch (Exception e) {
            Log.v("ProgressRecorder", "미디어 플레이어 Prepare Error ==========> " + e);
        }

        if (mPlayerState == PLAYING) {
            mPlayProgressBar.setProgress(0);

            try {
                // SeekBar의 상태를 0.1초마다 체크
                mProgressHandler2.sendEmptyMessageDelayed(0, 100);


                mPlayer.start();
            } catch (Exception e) {
                Toast.makeText(this, "error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }//startPlay



    //재생중지
    private void stopPlay()
    {
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        mPlayProgressBar.setProgress(0);

        // 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
    }//stopPlay


    public void onCompletion(MediaPlayer mp)
    {
        mPlayerState = PLAY_STOP; // 재생이 종료됨

        // 재생이 종료되면 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);

    }//onCompletion


    //UI업데이트
    private void updateUI()
    {
        if (mPlayerState == PLAY_STOP) {
            mPlayProgressBar.setProgress(0);
        } else if (mPlayerState == PLAYING);

    }//updateUI

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, 0);
    }

    // 재생시 SeekBar 처리
    Handler mProgressHandler2 = new Handler() {
        public void handleMessage(Message msg) {
            //Message; extends Object implements Parcelable
            if (mPlayer == null)
                return;

            try {
                if (mPlayer.isPlaying()) {
                    mPlayProgressBar.setProgress(mPlayer.getCurrentPosition());
                    mProgressHandler2.sendEmptyMessageDelayed(0, 100);

                } else {
                    mPlayer.release();
                    mPlayer = null;
                    updateUI();
                }
            } catch (IllegalStateException e) {
            } catch (Exception e) {
            }
        }//handleMessage
    };//handler
}//CheckVoiceNoteActivity
