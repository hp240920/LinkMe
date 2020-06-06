package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Conformation extends AppCompatActivity {

    private TextView display;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conformation);

        display = findViewById(R.id.message);
        Intent intent = getIntent();
        String send_to = intent.getStringExtra("phoneNum");

        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
        String my_phone = sharedPreferences.getString("phone","");

        sendInfo(my_phone,send_to);
        display.setText("Message Successfully sent to :"+send_to + " :) .......");
    }

    private void sendInfo(String my_phone, String send_to) {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Message");


        Message message = new Message(my_phone, send_to, false);
        databaseReference.push().setValue(message).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                display.setText(e.getLocalizedMessage());
            }
        });
    }
}
