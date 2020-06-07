package com.example.authotp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationAction extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String send_to = intent.getStringExtra("phoneNum");
        String send_from = SharePreHelper.getName();

        //SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
        //String my_phone = sharedPreferences.getString("phone","");
        sendInfo(send_from, send_to, context);
        //display.setText("Message Successfully sent to :"+send_to + " :) .......");
    }

    private void sendInfo(String my_phone, String send_to, final Context context) {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Message");


        Message message = new Message(my_phone, send_to, false);
        databaseReference.push().setValue(message).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        })
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Sent!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
