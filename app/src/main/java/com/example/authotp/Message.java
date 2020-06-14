package com.example.authotp;

import androidx.annotation.NonNull;

public class Message {
    private String from;
    private String to;
    private boolean check;
    private String file1;
    private String file2;
    private String key;

    public String getFile1() {
        return file1;
    }

    public void setFile1(String file1) {
        this.file1 = file1;
    }

    public String getFile2() {
        return file2;
    }

    public void setFile2(String file2) {
        this.file2 = file2;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Message(){

    }

    public Message(String from, String to, boolean check){
        this.from = from;
        this.to = to;
        this.check = check;
    }

    public Message(String from, String to, boolean check,String file1, String file2,String key){
        this.from = from;
        this.to = to;
        this.check = check;
        this.file1 = file1;
        this.file2 = file2;
        this.key = key;
    }

    @NonNull
    @Override
    public String toString() {

        return "From "+ this.from + " To "+ this.to;
    }
}
