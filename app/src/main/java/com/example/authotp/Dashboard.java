package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

public class Dashboard extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dashboard_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.edit_information:
                Log.i("Selected :","edit info");
                return true;
            case R.id.about_us:
                Log.i("Selected :","about us");
                return true;
            case R.id.log_out:
                Log.i("Selected :","log out");
            default:
                return false;
        }
    }

    private User currentUser;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        setTitle("Dashboard");

        // Get all the values from Shared Prefrences;
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);


        currentUser = new User();
        getSharedPref(sharedPreferences);

        //////////////////////////////////
        currentUser.setPhonenumber("7777");
        /////////////////////////////////

        // Getting Firebase Database and Storage

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage =FirebaseStorage.getInstance();

        // update the scroll view
        updateScrollView();

        // check for Notification and start Service

        if(serviceIntent == null){
            serviceIntent = new Intent(this, com.example.authotp.Notify.class);
            serviceIntent.putExtra("inputExtra", "input");
            ContextCompat.startForegroundService(this, serviceIntent);

            check_notification();
        }



    }
    

    private void check_notification() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference().child("Message").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                System.out.println("Outside for loop");
                for(DataSnapshot user : dataSnapshot.getChildren()){
                    System.out.println("For loop");
                    if(user.exists()){
                        System.out.println("user exists");
                        if(user.getValue(Message.class).getTo().equals(currentUser.getPhonenumber())){ // insert your current number
                            System.out.println("changes made");
                            notification2();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void updateScrollView() {
        final ScrollView scrollView = findViewById(R.id.scrollView);

        DatabaseReference dbref = firebaseDatabase.getReference().child("Message");

        Query query = dbref.orderByChild("to").equalTo(currentUser.getPhonenumber());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LinearLayout linearLayout = findViewById(R.id.scrollViewLinearLayout);
                linearLayout.removeAllViews();
                for(DataSnapshot messageSnapshot : dataSnapshot.getChildren()){
                    if(messageSnapshot.exists()){
                        Message newMessage = messageSnapshot.getValue(Message.class);

                        writeTextView(newMessage.getFrom(),scrollView);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void writeTextView(String phone, ScrollView scrollView){

        System.out.println(phone);
        LinearLayout linearLayout = findViewById(R.id.scrollViewLinearLayout);
        TextView textView = new TextView(this);
        textView.setText(phone);
        linearLayout.addView(textView);
    }

    private void getSharedPref(SharedPreferences sharedPreferences){

        currentUser.setName(sharedPreferences.getString("name",""));
        currentUser.setPhonenumber(sharedPreferences.getString("phone",""));
        currentUser.setInstagram(sharedPreferences.getString("insta",""));
        currentUser.setSnapchat(sharedPreferences.getString("snap",""));
        currentUser.setGitHub(sharedPreferences.getString("github",""));
        currentUser.setLinkedIn(sharedPreferences.getString("LinkedIn",""));
        currentUser.setFiles1(sharedPreferences.getString("file1",""));
        currentUser.setFiles2(sharedPreferences.getString("file2",""));
    }

    private void notification2() {
        // Builds your notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("John's Android Studio Tutorials")
                .setContentText("A video has just arrived!");

        // Creates the intent needed to show the notification
        Intent notificationIntent = new Intent(this, Dashboard.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

        manager.notify(0, builder.build());

    }
}
