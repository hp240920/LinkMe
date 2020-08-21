package com.linkme.LinkMe;

public class Row {

    Message message;
    Integer profile;
    int img_download;
    int img_save;

    public Row(Message message, Integer profile, int img_download, int img_save){
        this.img_download = img_download;
        this.img_save = img_save;
        this.profile = profile;
        this.message = message;
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

    public Message getName() {
        return message;
    }

    public void setName(Message message) {
        this.message = message;
    }

    public Integer getProfile() {
        return profile;
    }

    public void setProfile(Integer profile) {
        this.profile = profile;
    }

}
