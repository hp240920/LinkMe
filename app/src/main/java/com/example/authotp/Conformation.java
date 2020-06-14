package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Conformation extends AppCompatActivity {

    private TextView display;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conformation);

        display = findViewById(R.id.message);
        Intent intent = getIntent();
       String my_phone = intent.getStringExtra("myPhone");
        String send_to = intent.getStringExtra("sendTo");
        String uri1 = intent.getStringExtra("uri1");
        String uri2 = intent.getStringExtra("uri2");
        String key = "";
        sendInfo(my_phone,send_to,uri1,uri2);
        display.setText("Message Successfully sent to :" + send_to + " :) .......");
    }

    public void onBackPressed() {
      finish();
    }

    private void sendInfo(String my_phone, String send_to, String uri1, String uri2) {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Message");

        DatabaseReference keyref = databaseReference.push();
        String key = keyref.getKey();

        Message message = new Message(my_phone, send_to, false,uri1,uri2,key);
        databaseReference.child(key).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(),"SENT",Toast.LENGTH_SHORT).show();
            }
        });
    }
}