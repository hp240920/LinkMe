package com.linkme.LinkMe;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class User implements Serializable {


    public static String lastestNumber= "";

    private String Name;
    private String email;
    private String website;
    private String Instagram;
    private String Snapchat;
    private String GitHub;
    private String LinkedIn;
    //private String Files1;  // User selected file
    //private String Files2;
    private String phonenumber;
    //ArrayList<User> arrfiles = new ArrayList<>();
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    /*
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
     */


    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
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


    public User(String name, String Instagram, String Snapchat, String Github, String LinkedIn, String File1, String File2, String phonenumber, String email, String website){
        this.Name = name;
        this.Instagram = Instagram;
        this.Snapchat = Snapchat;
        this.GitHub = Github;
        this.LinkedIn = LinkedIn;
        //this.Files1 = File1;
        //this.Files2 = File2;
        this.phonenumber = phonenumber;
        this.website = website;
        this.email = email;
    }

    public User(String name, String phonenumber){
        this.Name = name;
        this.phonenumber = phonenumber;
    }

    public String getCalllogUserString(User user) {
        String output = user.getPhonenumber();
        if(user.getName() != null){
            output += "  " + user.getName();
        }
        return output;
    }

    @NonNull
    public String toString(){
        return this.Name + ", " + this.phonenumber + ", " + this.email + ", " + this.website
                + ", " + this.Instagram + ", " + this.Snapchat + ", " + this.GitHub + ", " + this.LinkedIn;
    }
}
