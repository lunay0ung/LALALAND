package com.example.luna.lalaland.All.Trending;

/**
 * Created by LUNA on 2018-11-08.
 */

public class Genre {

    String name;
    int count;

    public Genre(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
