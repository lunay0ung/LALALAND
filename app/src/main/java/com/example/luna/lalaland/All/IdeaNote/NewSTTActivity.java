package com.example.luna.lalaland.All.IdeaNote;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.luna.lalaland.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
/*
*
*구글 STT(Speech to text) API를 이용한 액티비티
*음성->텍스트로 변환하여 메모를 작성한다.
*
* TODO
* 마이크 사용 전 권한 승인 여부를 체크하고 상황에 맞게 처리한다
* */


public class NewSTTActivity extends AppCompatActivity {

    private static final String TAG = NewSTTActivity.class.getSimpleName();
    private EditText et_speechInput;
    private ImageButton btn_mic;
    private Button btn_save, btn_delete;
    String content; //stt로 받아온 메모 내용


    SpeechRecognizer speechRecognizer;
    Intent intent;
    Boolean isAudioPermissionGranted = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stt_new);

        initViews();

        btn_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "마이크 버튼 클릭");

                //마이크 사용 권한 승인여부 확인
                requestAudioPermission();
                Log.d(TAG, "stt 전 오디오 권한 허용여부 확인: "+isAudioPermissionGranted);
                if(isAudioPermissionGranted) {
                    Log.d(TAG, "stt 시작");
                    // 새 SpeechRecognizer를 만드는 팩토리 메서드
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                    // 모든 콜백을 수신하는 리스너를 설정
                    speechRecognizer.setRecognitionListener(listener);
                    // 듣기를 시작
                    speechRecognizer.startListening(intent);
                }//if

            }//onClick
        });//setOnClickListener




        //메모 내용을 content로, 오늘 시간을 받아 '시간+ 음성녹음'이라는 내용을 제목으로 메모를 저장할 것
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                content = et_speechInput.getText().toString();

                if(TextUtils.isEmpty(content)){ //저장할 내용이 없으면 안 됨
                 Toast.makeText(NewSTTActivity.this, "메모를 입력해주세요.", Toast.LENGTH_SHORT).show();
             } else {
                 //저장
                 DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                 Date date = new Date();
                 String title = dateFormat.format(date);

                 Intent resultIntent = new Intent();
                 resultIntent.putExtra("title", title);
                 resultIntent.putExtra("content", content);
                 setResult(RESULT_OK,resultIntent);
                 finish();
                 Log.d(TAG, "stt 메모 작성 후 홈으로 보냄");
             }
            }//onClick
        });//

        //메모장에 작성한 내용을 삭제하는 버튼
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_speechInput.getText().clear();
            }
        });

    }//onCreate

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            // 사용자가 말하기 시작할 준비가되면 호출
            Toast.makeText(getApplicationContext(),"음성인식을 시작합니다.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {
            // 사용자가 말하기 시작했을 때 호출
            Log.d(TAG, "stt 말하기 시작");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // 입력받는 소리의 크기를 알려줌
            Log.d(TAG, "stt 목소리 크기: "+rmsdB);
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // 사용자가 말하기를 중지하면 호출
            Log.d(TAG, "stt 말하기 중지됨");

        }

        @Override
        public void onEndOfSpeech() {
            // 말하기가 끝나면 호출
            Log.d(TAG, "stt 종료");
        }

        @Override
        public void onError(int error) {
            // 네트워크 또는 인식 오류가 발생했을 때 호출
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "오디오 접근 권한을 허용해주세요.";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "음성이 감지되지 않아 종료되었습니다.";
                    break;
                default:
                    message = "알 수 없는 오류 발생";
                    break;
            }

            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();
        }//error

        @Override
        public void onResults(Bundle results) {
            // 인식 결과가 준비되면 호출
            // 음성인식된 결과를 ArrayList로 모아옴
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            // 이후 for문으로 textView에 setText로 음성인식된 결과를 수정
            for (int i = 0; i < matches.size(); i++) {
                et_speechInput.setText(matches.get(i));

            }//for

            btn_save.setTextColor(getResources().getColor(R.color.myWhite));

        }//onResults

        @Override
        public void onPartialResults(Bundle partialResults) {
            // 부분 인식 결과를 사용할 수 있을 때 호출

        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // 향후 이벤트를 추가하기 위해 예약
        }
    };//RecognitionListener


   private void requestAudioPermission(){
       Dexter.withActivity(this)
               .withPermission(Manifest.permission.RECORD_AUDIO)
               .withListener(new PermissionListener() {
                   @Override
                   public void onPermissionGranted(PermissionGrantedResponse response) {
                       //오디오 접근 권한 허용 됨
                       Log.d(TAG, "오디오 접근 권한 허용됨");
                       isAudioPermissionGranted = true;
                   }//onPermissionGranted

                   @Override
                   public void onPermissionDenied(PermissionDeniedResponse response) {
                       Log.d(TAG, "오디오 접근 권한 영구 거부");
                        //오디오 접근 권한을 허용하지 않겠다고 선택한 경우
                        if(response.isPermanentlyDenied()) {
                           //허용하지 않으면 이 기능을 사용할 수 없다고 다이얼로그 띄우기
                            showSettingsDialog();
                        }//
                   }//onPermissionDenied

                   @Override
                   public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                   }//onPermissionRationaleShouldBeShown
               }).check();//PermissionListener
   }//requestAudioPermission



    //유저가 오디오 접근 권한을 지속적으로 거부했을 때 띄워줄 메시지
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NewSTTActivity.this);
        builder.setTitle("오디오 접근 권한이 필요합니다.");
        builder.setMessage("오디오 접근을 허용하지 않으면 이 기능을 사용할 수 없습니다. [설정]메뉴에 가서 권한 설정을 변경하실 수 있습니다.");
        builder.setPositiveButton("[설정] 메뉴로 이동", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    //앱 권한을 설정하기 위해 세팅 메뉴로 이동
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }//openSettings


    //객체 초기화
    private void initViews(){
        et_speechInput = (EditText) findViewById(R.id.et_speechInput);
        btn_mic = (ImageButton) findViewById(R.id.btn_mic);
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_delete = (Button) findViewById(R.id.btn_delete);

        //사용자에게 음성을 요구하고 음성 인식기를 통해 전송하는 활동을 시작
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //음성 인식을위한 음성 인식기의 의도에 사용되는 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        //음성을 번역할 언어를 설정
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
    }//initViews
}//NewSTTActivity
