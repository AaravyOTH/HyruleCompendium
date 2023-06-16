package com.example.hyrulecompendium;

public class Users {
    String userId, name, profile;

    public Users(String userId, String name, String profile){
        this.userId = userId;
        this.name = name;
        this.profile = profile;
    }

    public Users(){

    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getProfile() {
        return profile;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
