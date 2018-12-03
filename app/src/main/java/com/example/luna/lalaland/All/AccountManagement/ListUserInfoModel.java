package com.example.luna.lalaland.All.AccountManagement;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by LUNA on 2018-11-06.
 */

public class ListUserInfoModel {
    @SerializedName("userInfo")
    private List<User> userInfoList;


    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;

    @SerializedName("email")
    private String email;
    @SerializedName("username")
    private String username;
    @SerializedName("gender")
    private String gender;
    @SerializedName("age")
    private String age;


    public ListUserInfoModel(List<User> userInfoList, int status, String message){
        this.userInfoList = userInfoList;
        this.status = status;
        this.message = message;
    }//


    public List<User> getUserInfoList() {
        return userInfoList;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setUserInfoList(List<User> userInfoList) {
        this.userInfoList = userInfoList;
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
}//ListUserInfoModel
