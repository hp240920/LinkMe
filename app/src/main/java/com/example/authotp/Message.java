package com.example.authotp;

import androidx.annotation.NonNull;

public class Message {
    String from;
    String to;

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

    public Message(String from, String to){
        this.from = from;
        this.to = to;
    }

    @NonNull
    @Override
    public String toString() {

        return "From "+ this.from + " To "+ this.to;
    }
}
