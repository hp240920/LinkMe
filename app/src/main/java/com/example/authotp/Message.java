package com.example.authotp;

import androidx.annotation.NonNull;

public class Message {
    private String from;
    private String to;
    private boolean check;

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

    @NonNull
    @Override
    public String toString() {

        return "From "+ this.from + " To "+ this.to;
    }
}
