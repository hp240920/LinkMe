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

import com.google.android.gms.tasks.OnFailureListener;
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
        //Intent intent = getIntent();
        String send_to = User.lastestNumber;
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
        String my_phone  = sharedPreferences.getString("phone","");
        int notificationId = 0;
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.cancel(notificationId);
        sendInfo(my_phone,send_to);
        display.setText("Message Successfully sent to :" + send_to + " :) .......");
    }

    public void onBackPressed() {
      finish();
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