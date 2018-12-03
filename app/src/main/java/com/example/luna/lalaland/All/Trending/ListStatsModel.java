package com.example.luna.lalaland.All.Trending;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LUNA on 2018-11-08.
 */

public class ListStatsModel {


    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;
    @SerializedName("genre")
    private String genre;
    @SerializedName("top_genre")
    private String top_genre;

    public String getTop_genre() {
        return top_genre;
    }

    public void setTop_genre(String top_genre) {
        this.top_genre = top_genre;
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

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}//ListStatsModel
