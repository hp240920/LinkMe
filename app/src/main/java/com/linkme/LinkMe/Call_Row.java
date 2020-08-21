package com.linkme.LinkMe;

import android.net.Uri;

public class Call_Row {
    String name;
    String phone;
    Uri uri;
    int send_img;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Call_Row(String name, String phone, int img){
        this.name = name;
        this.phone = phone;
        this.send_img = img;
    }

    public Call_Row(String name, Uri uri, int img){
        this.name = name;
        this.uri = uri;
        this.send_img = img;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSend_img() {
        return send_img;
    }

    public void setSend_img(int send_img) {
        this.send_img = send_img;
    }
}
