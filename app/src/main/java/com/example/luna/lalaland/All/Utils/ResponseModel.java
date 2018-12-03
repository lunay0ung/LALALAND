package com.example.luna.lalaland.All.Utils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by LUNA on 2018-09-27.
 */

/*
* this class is used to save the response of the json from server
* */
public class ResponseModel {

    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;

    //카카오 결제를 위한 변수
    private String kakao;


    public ResponseModel(int status, String message) {
        this.status = status;
        this.message = message;
    }


    public ResponseModel() {

    }

    public String getKakao() {
        return kakao;
    }

    public void setKakao(String kakao) {
        this.kakao = kakao;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}//InsertFoodResponseModel
