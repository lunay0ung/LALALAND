package com.example.luna.lalaland.All.Blockchain;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.luna.lalaland.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
* 유저가 가진 LunToken을 다른 지갑 주소로 송금할 수 있는 액티비티
*
* 1.
* 송금할 지갑 주소는 직접 입력하거나 QRCode로 가져올 수 있다.
* 2.
* 유저가 1lut를 보내면 1이라는 integer값을 받은 후 토큰의 decimal(18)을 이용해서 다시 원래 BigInteger형태로 변환해주는 것이 필요.
* */
public class SendTokenActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "토큰송금/SendTokenActivity";

    @BindView(R.id.iv_QRCode)
    ImageView iv_QRCode;
    @BindView(R.id.et_address)
    EditText et_address;
    @BindView(R.id.et_amount)
    EditText et_amount;
    @BindView(R.id.btn_cancel)
    Button btn_cancel;
    @BindView(R.id.btn_send)
    Button btn_send;

    //송금할 주소와 금액
    String addressToSend;
    int amountToSend;

    //QR Code 스캐너 객체
    private IntentIntegrator qrScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_token);

        ButterKnife.bind(this);

        //큐알코드 스캐너 초기화
        qrScan = new IntentIntegrator(this);

        //QRCode 버튼을 누르면 QRCode를 통해 지갑 주소를 가져올 수 있다
        iv_QRCode.setOnClickListener(this);

        //취소 및 송금
        btn_cancel.setOnClickListener(this);
        btn_send.setOnClickListener(this);

    }//onCreate

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.iv_QRCode:
                Log.d(TAG, "큐알코드");
                //기존에 뭔가 써있었다면 지워준다
                et_address.getText().clear();
                qrScan.setPrompt("Scanning...");
                qrScan.initiateScan();
                break;

            case R.id.btn_send:

                String amount = et_amount.getText().toString();
                if (!TextUtils.isEmpty(amount)) {
                    amountToSend = Integer.parseInt(amount);
                    Log.d(TAG, "받아온 금액: "+amountToSend);
                    //받아온 결과 값을 resultIntent 에 담아서 MyWalletActivity 로 전달하고 현재 Activity 는 종료.
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("addressToSend", addressToSend);
                    resultIntent.putExtra("amountToSend", amountToSend);
                    setResult(RESULT_OK,resultIntent);
                    finish();
                } else {
                    Toast.makeText(this, "송금할 금액을 정확히 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_cancel: //송금 취소
                finish();
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
                Toast.makeText(SendTokenActivity.this, "fail", Toast.LENGTH_SHORT).show();
            } else {
                //qrcode 결과가 있으면
                Toast.makeText(SendTokenActivity.this, "success", Toast.LENGTH_SHORT).show();
                addressToSend = result.getContents(); //받아온 주소
                et_address.setText(addressToSend);
                Log.d(TAG, "받아온 주소: "+result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }//else
    }//onActivityResult

    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }
}//SendTokenActivity
