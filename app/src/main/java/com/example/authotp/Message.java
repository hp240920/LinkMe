package com.example.authotp;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Message implements Comparable {
    private String from;
    private String to;
    private boolean check;
    private boolean notify;
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

    public boolean isNotify() {
        return notify;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
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

    public Message(String from, String to, boolean check, boolean notify){
        this.from = from;
        this.to = to;
        this.check = check;
        this.notify = notify;
    }

    public Message(String from, String to, boolean check, boolean notify, String file1, String file2, String key){
        this.from = from;
        this.to = to;
        this.check = check;
        this.notify = notify;
        this.file1 = file1;
        this.file2 = file2;
        this.key = key;
    }

    @NonNull
    @Override
    public String toString() {

        return "From "+ this.from + " To "+ this.to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return from.equals(message.from) &&
                to.equals(message.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof Message){
            if(((Message) o).getKey().compareTo(this.getKey())>0){
                return 1;
            }
            else if(((Message) o).getKey().compareTo(this.getKey())==0){
                return 0;
            }
            else {
                return -1;
            }
        }
        return -1;
    }
}
