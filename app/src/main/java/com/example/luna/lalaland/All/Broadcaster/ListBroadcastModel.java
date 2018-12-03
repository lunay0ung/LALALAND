package com.example.luna.lalaland.All.Broadcaster;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by LUNA on 2018-09-27.
 */

public class ListBroadcastModel {

    @SerializedName("broadcastList")
    private List<Broadcast> broadcastList;
    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;
    @SerializedName("viewers")
    private int viewers;


    public ListBroadcastModel(List<Broadcast> broadcastList, int status, String message){
        this.broadcastList = broadcastList;
        this.status = status;
        this.message = message;
    }//

    public List<Broadcast> getBroadcastList(){
        return broadcastList;
    }

    public void setBroadcastList(List<Broadcast> broadcastList) {
        this.broadcastList = broadcastList;
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

    public int getViewers() {
        return viewers;
    }

    public void setViewers(int viewers) {
        this.viewers = viewers;
    }
}//ListBroadcastModel
