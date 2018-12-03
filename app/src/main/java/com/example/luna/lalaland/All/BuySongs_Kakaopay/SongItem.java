package com.example.luna.lalaland.All.BuySongs_Kakaopay;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Created by LUNA on 2018-10-23.
 */

/*
* BuySongsActivity에서 보여줄 음원 리스트를 위한 클래스
* 앨범 이미지, 음원 제목, 아티스트명, 원화 표시 이미지, 가격을 포함
* */

public class SongItem {

    int albumImage;
    Drawable drawable_albumImage;
    String title;
    String artist;
    ImageView wonMark;
    int price;
    int orderId;


    public SongItem (int albumImage, String title, String artist, int price) {
        this.albumImage = albumImage;
        this.title = title;
        this.artist = artist;
        this.price = price;
    }//

    public SongItem (int albumImage, String title, String artist, int price, int orderId) {
        this.albumImage = albumImage;
        this.title = title;
        this.artist = artist;
        this.price = price;
        this.orderId = orderId;
    }//

    public SongItem (int albumImage, String title, String artist, ImageView wonMark, int price) {
       this.albumImage = albumImage;
       this.title = title;
       this.artist = artist;
       this.wonMark = wonMark;
       this.price = price;
    }//

    public int getAlbumImage() {
        return albumImage;
    }

    public void setAlbumImage(int albumImage) {
        this.albumImage = albumImage;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public ImageView getWonMark() {
        return wonMark;
    }

    public void setWonMark(ImageView wonMark) {
        this.wonMark = wonMark;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}//SongItem
