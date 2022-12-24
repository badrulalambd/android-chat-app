package com.badrulacademy.mychat;

public class FirebaseModel {
    //Variable name should be same name as variable of
    // SetProfile to save/store data on Cloud Firestore.
    //Otherwise it won't work
    String name, image, uid, status;

    //Constructor....
    public FirebaseModel() {
    }

    public FirebaseModel(String name, String image, String uid, String status) {
        this.name = name;
        this.image = image;
        this.uid = uid;
        this.status = status;
    }

    //Getter and setter..
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
