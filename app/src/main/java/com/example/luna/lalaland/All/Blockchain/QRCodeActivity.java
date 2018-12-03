package com.example.luna.lalaland.All.Blockchain;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.luna.lalaland.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
* MyWalletActivity(루나코인 메뉴 홈)에서 툴바/액션바에 있는 qr code 메뉴를 누르면
* 현재 지갑의 주소를 qr코드로 보여준다
*
* */
public class QRCodeActivity extends AppCompatActivity {

    Bitmap QRCode;
    String address;

    @BindView(R.id.iv_QRCode)
    ImageView iv_QRCode;
    @BindView(R.id.tv_address)
    TextView tv_address;
    @BindView(R.id.btn_finish)
    Button btn_finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        ButterKnife.bind(this);

        //LunaCoinActivity에서 보낸 QRCode 받아오기
        Intent intent = getIntent();
        QRCode = (Bitmap)intent.getParcelableExtra("QRCode");
        address = intent.getStringExtra("address");

        //이미지뷰에 QR코드 세팅
        iv_QRCode.setImageBitmap(QRCode);

        //텍스트뷰에 지갑 주소 세팅
        tv_address.setText(address);

        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }//onClick
        });//OnClickListener
    }//onCreate

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }//finish
}//QRCodeActivity
