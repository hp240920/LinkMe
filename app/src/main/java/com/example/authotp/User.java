package com.example.authotp;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class User {

    String Name;
    String Instagram;
    String Snapchat;
    String GitHub;
    String LinkedIn;
    String Files1;
    String Files2;
    String phonenumber;
    ArrayList<User> arrfiles = new ArrayList<>();

    public String getFiles1() {
        return Files1;
    }

    public void setFiles1(String files1) {
        Files1 = files1;
    }

    public String getFiles2() {
        return Files2;
    }

    public void setFiles2(String files2) {
        Files2 = files2;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public ArrayList<User> getArrfiles() {
        return arrfiles;
    }

    public void setArrfiles(ArrayList<User> arrfiles) {
        this.arrfiles = arrfiles;
    }


    public ArrayList<User> getString() {
        return arrfiles;
    }

    public void setString(ArrayList<User> string) {
        this.arrfiles = string;
    }

    public User(){

    }
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getInstagram() {
        return Instagram;
    }

    public void setInstagram(String instagram) {
        Instagram = instagram;
    }

    public String getSnapchat() {
        return Snapchat;
    }

    public void setSnapchat(String snapchat) {
        Snapchat = snapchat;
    }

    public String getGitHub() {
        return GitHub;
    }

    public void setGitHub(String gitHub) {
        GitHub = gitHub;
    }

    public String getLinkedIn() {
        return LinkedIn;
    }

    public void setLinkedIn(String linkedIn) {
        LinkedIn = linkedIn;
    }


    public User(String name, String Instagram, String Snapchat, String Github, String LinkedIn, String Files1 ,String File2,  ArrayList<User> array, String phonenumber){
        this.Name = name;
        this.Instagram = Instagram;
        this.Snapchat = Snapchat;
        this.GitHub = Github;
        this.LinkedIn = LinkedIn;
        this.Files1 = Files1;
        this.Files2 = File2;
        this.arrfiles = array;
        this.phonenumber = phonenumber;
    }
    public User(String name, String Instagram, String Snapchat, String Github, String LinkedIn, String File1, String File2, String phonenumber){
        this.Name = name;
        this.Instagram = Instagram;
        this.Snapchat = Snapchat;
        this.GitHub = Github;
        this.LinkedIn = LinkedIn;
        this.Files1 = File1;
        this.Files2 = File2;
        this.phonenumber = phonenumber;
    }
}