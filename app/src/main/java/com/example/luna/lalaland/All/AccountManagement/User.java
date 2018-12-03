package com.example.luna.lalaland.All.AccountManagement;

/**
 * Created by LUNA on 2018-11-06.
 */

public class User {

    String username, email, gender, age;

    public User() {
    }

    public User(String username, String email, String gender, String age){
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}//User
