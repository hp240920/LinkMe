package com.example.authotp.Threads;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.authotp.Dashboard;
import com.example.authotp.User;

public class GetCurrentUserThread implements Runnable {

    private Context context;
    User currentUser;


    public GetCurrentUserThread(Context context){
        this.context = context;
    }
    @Override
    public void run() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
         currentUser = new User();
        currentUser = getSharedPref(sharedPreferences);

    }

    private User getSharedPref(SharedPreferences sharedPreferences){
        currentUser.setName(sharedPreferences.getString("name",""));
        currentUser.setPhonenumber(sharedPreferences.getString("phone",""));
        currentUser.setEmail(sharedPreferences.getString("email", ""));
        currentUser.setWebsite(sharedPreferences.getString("website", ""));
        currentUser.setInstagram(sharedPreferences.getString("insta",""));
        currentUser.setSnapchat(sharedPreferences.getString("snap",""));
        currentUser.setGitHub(sharedPreferences.getString("github",""));
        currentUser.setLinkedIn(sharedPreferences.getString("linkedIn",""));
        //currentUser.setFiles1(sharedPreferences.getString("file1",""));
        //currentUser.setFiles2(sharedPreferences.getString("file2",""));
        return currentUser;
    }

    public User getCurrentUser(){
        return currentUser;
    }

    public String getPhonenumber(){
        return currentUser.getPhonenumber();
    }

}
