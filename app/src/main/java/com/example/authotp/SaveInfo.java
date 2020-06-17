package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.StringTokenizer;

public class SaveInfo extends AppCompatActivity {

    String key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_info);
        Intent intent = getIntent();
        key = intent.getStringExtra("key");
    }

    public void onSave(View v){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference dbref = firebaseDatabase.getReference().child("Message");

        Query query = dbref.orderByChild("key").equalTo(key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot messageSnapshot : dataSnapshot.getChildren()){
                    if(messageSnapshot.exists()){
                        String file1 = messageSnapshot.getValue(Message.class).getFile1();
                        //String file2 = messageSnapshot.getValue(Message.class).getFile2();
                        //String phoneNumber = messageSnapshot.getValue(Message.class).getFrom();
                        if(file1 != null){
                            saveToContact(file1);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void saveToContact(String file1) {
        System.out.println(file1);
        String[] information = file1.split(", ");
        String name = information[0];
        String phoneNumber = information[1];
        String email = information[2];
        String website = information[3];
        String insta = information[4];
        String snap = information[5];
        String gitHub = information[6];
        String linkedin = information[7];
        String notes = "Website: " + website + "\nInstagram: "
                + insta + "\nSnapChat: " + snap + "\nGithub: " + gitHub + "\nLinkedIn: " + linkedin;

        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL,email);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE,phoneNumber);
        intent.putExtra(ContactsContract.Intents.Insert.NAME,name);
        intent.putExtra(ContactsContract.Intents.Insert.NOTES, notes);
        startActivity(intent);
    }

    public void onDownload(View v){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference dbref = firebaseDatabase.getReference().child("Message");

        Query query = dbref.orderByChild("key").equalTo(key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot messageSnapshot : dataSnapshot.getChildren()){
                    if(messageSnapshot.exists()){
                        //String file1 = messageSnapshot.getValue(Message.class).getFile1();
                        String file2 = messageSnapshot.getValue(Message.class).getFile2();
                        //String phoneNumber = messageSnapshot.getValue(Message.class).getFrom();
                        if(file2!=null){
                            downloadFile(file2);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void downloadFile(String file2) {

        File filepath = new File(Environment.getExternalStorageDirectory() + "/MYAPP");
        if(!filepath.exists()){
            filepath.mkdir();
        }
        // StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(file2);

        DownloadManager downloadManager = (DownloadManager) getApplicationContext().
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(file2);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir( "/MYAPP/", uri.getLastPathSegment());

        downloadManager.enqueue(request);

        /*
        DownloadManager downloadManager1 = (DownloadManager) getApplicationContext().
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri1 = Uri.parse(file2);
        DownloadManager.Request request1 = new DownloadManager.Request(uri1);

        request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request1.setDestinationInExternalFilesDir(getApplicationContext(), DIRECTORY_DOWNLOADS, "Try1.pdf");

        downloadManager1.enqueue(request1);

         */
    }


}