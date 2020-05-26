package com.example.authotp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Sign_Up extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign__up);
        setTitle("Your Information");
        Intent intent  = getIntent();
        String phone = intent.getStringExtra("phoneNo");
        assert phone != null;
        Log.i("Phone No: ", phone);
    }
}
