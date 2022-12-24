package com.badrulacademy.mychat;

public class UserProfile {

    public String userName, userUID;

    //Constructor
    public UserProfile() {
    }

    public UserProfile(String userName, String userUID) {
        this.userName = userName;
        this.userUID = userUID;
    }

    //Getter and Setter
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }
}
