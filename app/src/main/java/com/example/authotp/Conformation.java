package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Conformation extends BroadcastReceiver {

    String my_phone = "";
    String send_to = "";
    String uri = "";
    //String uri = "";


    @Override
    public void onReceive(final Context context, Intent intent) {
        my_phone = intent.getStringExtra("myPhone");
        send_to = intent.getStringExtra("sendTo");
        uri = intent.getStringExtra("uri");
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("User");
        //final String[] uri = {""};
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot user: dataSnapshot.getChildren()){
                        if(user.getValue(User.class).getPhonenumber().equals(my_phone)){
                            // getting info based on User class
                            String info = user.getValue(User.class).toString();
                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReference = firebaseDatabase.getReference().child("Message");

                            DatabaseReference keyref = databaseReference.push();
                            String key = keyref.getKey();

                            System.out.println(uri);
                            Message message = new Message(my_phone, send_to, false, false, info, uri, key);
                            databaseReference.child(key).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context,"SENT",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //display.setText("Message successfully sent to :" + send_to + " :) .......");
    }


/*
    private void sendInfo(String my_phone, String send_to, String uri1, String uri2) {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Message");

        DatabaseReference keyref = databaseReference.push();
        String key = keyref.getKey();

        Message message = new Message(my_phone, send_to, false, uri1, uri2, key);
        databaseReference.child(key).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(),"SENT",Toast.LENGTH_SHORT).show();
            }
        });
    }
 */
}