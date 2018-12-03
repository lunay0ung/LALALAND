package com.example.luna.lalaland.All.Utils;

import android.util.Log;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by LUNA on 2018-10-18.
 */

public class ConnectToMongoDB {

    //채팅 데이터를 저장하기 위해 몽고디비 연동
    private String MongoDB_IP = "13.124.23.131";
    private int MongoDB_PORT = 27017;
    private String DATABASE = "lalaland"; //데이터베이스 이름
    private String COLLECTION_NAME;
    private String TAG;

    public ConnectToMongoDB(String TAG, String COLLECTION_NAME) {
        this.TAG = TAG;
        this.COLLECTION_NAME = COLLECTION_NAME;
    }



    public MongoCollection<Document> getCollection() {

        MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
        optionsBuilder.connectTimeout(300000);

        MongoClient mongoClient = new MongoClient(new ServerAddress(MongoDB_IP, MongoDB_PORT));
        Log.d(TAG, "몽고디비-클라이언트 연결");


        MongoDatabase database = mongoClient.getDatabase(DATABASE);
        Log.d(TAG, "몽고디비-데이터베이스 연결");
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        Log.d(TAG, "몽고디비 연결-컬렉션 리턴");
        return collection;
    }


    public String getTAG() {
        return TAG;
    }

    public void setTAG(String TAG) {
        this.TAG = TAG;
    }

    public String getMongoDB_IP() {
        return MongoDB_IP;
    }

    public void setMongoDB_IP(String mongoDB_IP) {
        MongoDB_IP = mongoDB_IP;
    }

    public int getMongoDB_PORT() {
        return MongoDB_PORT;
    }

    public void setMongoDB_PORT(int mongoDB_PORT) {
        MongoDB_PORT = mongoDB_PORT;
    }

    public String getDATABASE() {
        return DATABASE;
    }

    public void setDATABASE(String DATABASE) {
        this.DATABASE = DATABASE;
    }

    public String getCOLLECTION_NAME() {
        return COLLECTION_NAME;
    }

    public void setCOLLECTION_NAME(String COLLECTION_NAME) {
        this.COLLECTION_NAME = COLLECTION_NAME;
    }


}//ConnectToMongoDB
