package com.example.luna.lalaland.All.Blockchain;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.luna.lalaland.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
* 지갑을 생성하거나 지갑에 로그인할 수 있는 액티비티
*
* */
public class WalletLoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "지갑 생성 액티비티";

    @BindView(R.id.iv_bitcoin)
    ImageView iv_bitcoin;
    @BindView(R.id.iv_QRCode)
    ImageView iv_QRCode;
    @BindView(R.id.et_address)
    EditText et_address;
    @BindView(R.id.et_password)
    EditText et_password;
    @BindView(R.id.btn_createWallet)
    Button btn_createWallet;
    @BindView(R.id.btn_login)
    Button btn_login;
    @BindView(R.id.tv_result)
    TextView tv_result;

    Boolean canUseStorage = false;

    //QR Code 스캐너 객체
    private IntentIntegrator qrScan;

    //지갑 생성 및 로그인에 필요한 정보
    String address;
    String password;
    Wallet wallet;
    String fileName;

    //web3j
    Web3j web3j;
    Credentials credentials;

    //기타
    SharedPreferences walletInfoShared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_login);

        ButterKnife.bind(this);

        wallet = new Wallet();
        walletInfoShared = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //블록체인 이미지에 애니메이션 효과 주기
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate);
        iv_bitcoin.startAnimation(animation);

        //큐알코드 스캐너 초기화
        qrScan = new IntentIntegrator(this);

        //QRCode 버튼을 누르면 QRCode를 통해 지갑 주소를 가져올 수 있다
        iv_QRCode.setOnClickListener(this);

        //지갑 생성 버튼이나 로그인버튼을 누르면 저장소 읽기/쓰기 권한 허용을 요청해야함
        btn_createWallet.setOnClickListener(this);
        btn_login.setOnClickListener(this);

        //개발 편의상 일단 주소 넣어둠-원래는 QR코드로 찍거나 직접 입력할 수 있음
        address = "0x09432F73964B1d6CD1d057817BD20d29c0c8DF98";
        et_address.setText(address);
    }//onCreate

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_QRCode:
                    //기존에 뭔가 써있었다면 지워준다
                    et_address.getText().clear();
                    qrScan.setPrompt("Scanning...");
                    qrScan.initiateScan();
                break;

            case R.id.btn_createWallet:
                    requestPermission();
                    if(canUseStorage)

                    address = et_address.getText().toString();
                    Log.d(TAG, "주소는: "+address);
                    if(address!=null)
                    //지갑 생성
                        try {
                            password = et_password.getText().toString();
                            fileName = wallet.createWallet(password);
                            tv_result.setText(fileName);
                            Log.d(TAG, "지갑 생성 성공: "+fileName);
                            //지갑을 생성했으면 fileName을 sharedPreference에 저장하고, 로그인할 때 등 사용하도록
                            SharedPreferences.Editor editor = walletInfoShared.edit();
                            editor.putString("walletInfoShared", fileName);
                            editor.apply();

                        } catch (Exception e) {
                            Toast.makeText(this, "지갑 생성 과정에서 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "지갑 생성 오류: "+e.toString());
                        }

                break;

            case R.id.btn_login:
                    requestPermission();

                if(canUseStorage)

                    //TODO 지갑 생성/로그인 구현하려했으나 한계를 느껴서 보류 / 중단. 아래 TODO참고
                    //로그인
//                    if(walletInfoShared.getString("walletInfoShared", "") != null) {
//                        fileName = walletInfoShared.getString("walletInfoShared", "");
//                        Log.d(TAG, "로그인 시도: " + fileName);
//                        Log.d(TAG, "로그인 시도: " + address);

//                    if(walletInfoShared.getString("password", "") != null) {
//                        password = walletInfoShared.getString("password", "");
//                        et_password.setText(password);
//                    }

                        try {
                            address = et_address.getText().toString();
                            password = et_password.getText().toString();
                            //TODO 지갑 생성/ 로그인 구현을 해보려고 했으나 ...구현해보려다 보니 공부가 더 많이 필요했음. 일단은 아래와 같이 fileName을 하드코딩해서 구현하는 쪽으로 2018/11/01
                            fileName = "UTC--2018-10-22T06-23-54.389Z--09432f73964b1d6cd1d057817bd20d29c0c8df98.json";
                            Credentials credentials = wallet.loadCredentials(password, fileName);
                            tv_result.setText(credentials.getAddress() + "Loaded successfully");
                            Log.d(TAG, "지갑 로그인 성공");

                            //비밀번호 저장해
                            //지갑을 생성했으면 fileName을 sharedPreference에 저장하고, 로그인할 때 등 사용하도록
                            SharedPreferences.Editor editor = walletInfoShared.edit();
                            editor.putString("password", password);
                            editor.apply();

                            //로그인에 성공했으면 이제 토큰 잔액확인/송금 가능한 화면으로 이동할 수 있다
                            Intent homeIntent = new Intent(WalletLoginActivity.this,
                                    MyWalletActivity.class);
                            homeIntent.putExtra("address", credentials.getAddress());
                            homeIntent.putExtra("password", password);
                            homeIntent.putExtra("fileName", fileName);
                            startActivity(homeIntent);

                        } catch (Exception e) {
                            tv_result.setText(e.toString());
                            Log.d(TAG, "지갑 로그인 결과: " + e.toString());
                        }
                 //   }
                break;
        }//switch
    }//onClick




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            //qrcode 가 없으면
            if (result.getContents() == null) {
                Toast.makeText(WalletLoginActivity.this, "fail", Toast.LENGTH_SHORT).show();
            } else {
                //qrcode 결과가 있으면
                Toast.makeText(WalletLoginActivity.this, "success", Toast.LENGTH_SHORT).show();
                address = result.getContents();
                et_address.setText(address);
                Log.d(TAG, result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }//else
    }//onActivityResult

    //저장소 읽기/쓰기 권한 요청
    private void requestPermission(){
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            canUseStorage = true;
                            Log.d(TAG, "모든 접근권한 승인: "+canUseStorage);
                        }

                        //접근 권한 거부 된 것 있는지 확인
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // 그렇다면 이 기능을 사용할 수 없다는 메시지와 함께 직접 [설정]메뉴에 가서 권한상태를 바꿀 수 있도록 유도
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).onSameThread()
                .check();

    }//requestPermission



    //유저가 오디오 접근 권한을 지속적으로 거부했을 때 띄워줄 메시지
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WalletLoginActivity.this);
        builder.setTitle("저장공간 읽기 및 쓰기 접근 권한이 필요합니다.");
        builder.setMessage("저장공간 접근을 허용하지 않으면 이 기능을 사용할 수 없습니다. [설정]메뉴에 가서 권한 설정을 변경하실 수 있습니다.");
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

    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }
}//WalletLoginActivity
