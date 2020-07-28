package com.example.authotp;

public class Row {

    String name;
    Integer profile;
    int img_download;
    int img_save;

    public Row(String name, Integer profile, int img_download, int img_save){
        this.img_download = img_download;
        this.img_save = img_save;
        this.profile = profile;
        this.name = name;
    }

    public int getImg_download() {
        return img_download;
    }

    public void setImg_download(int img_download) {
        this.img_download = img_download;
    }

    public int getImg_save() {
        return img_save;
    }

    public void setImg_save(int img_save) {
        this.img_save = img_save;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getProfile() {
        return profile;
    }

    public void setProfile(Integer profile) {
        this.profile = profile;
    }

}
