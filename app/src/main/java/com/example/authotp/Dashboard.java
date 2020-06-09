package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

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
                EditInfo();
                return true;
            case R.id.about_us:
                Log.i("Selected :","about us");
                return true;
            case R.id.log_out:
                Log.i("Selected :","log out");
                logOut();
                return true;
            default:
                return false;
        }
    }

    private void EditInfo() {
        //this gotta be fix... few corner cases check...
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        //SharePreHelper.setName(null);
        Intent intent = new Intent(this, Sign_Up.class);
        intent.putExtra("phoneNo", currentUser.getPhonenumber());
        startActivity(intent);
    }

    // removing shared pref and going to main Activity
    private void logOut() {
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        SharePreHelper.setName(null);
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
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

        if (ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Dashboard.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.READ_CALL_LOG},
                    1);
        }

        // Get all the values from Shared Prefrences;
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);


        currentUser = new User();
        getSharedPref(sharedPreferences);


        // Getting Firebase Database and Storage

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage =FirebaseStorage.getInstance();

        // update the scroll view
        updateScrollView();

        // check for Notification and start Service

        if(serviceIntent == null){
            serviceIntent = new Intent(this, com.example.authotp.Notify.class);
            serviceIntent.putExtra("inputExtra", "AuthOTP");
            ContextCompat.startForegroundService(this, serviceIntent);
            check_notification();
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    private void check_notification() {
        System.out.println("Needed!!!");
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference().child("Message").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                Message message = dataSnapshot.getValue(Message.class);
                //System.out.println("Needed123!!! " + message.toString());
                boolean check = message.isCheck();
                String toNumber = message.getTo();
                //count++;
                if(!check && toNumber.equals(currentUser.getPhonenumber())) {
                    notification2(message.getFrom());
                    database.getReference("Message").child(key).child("check").setValue(true);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
               // Toast.makeText(Dashboard.this, "Hello There", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
        textView.setOnClickListener(onClickListener);
        linearLayout.addView(textView,0);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view instanceof TextView){
                TextView tv = (TextView)view;
                String selectedUserNumber = tv.getText().toString();
                getFileFromNumber(selectedUserNumber);
                Toast.makeText(getApplicationContext(), selectedUserNumber, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void getFileFromNumber(String selectedUserNumber) {

        DatabaseReference dbref = firebaseDatabase.getReference().child("User");

        Query query = dbref.orderByChild("phonenumber").equalTo(selectedUserNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    if(userSnapshot.exists()){
                        String file1 = userSnapshot.getValue(User.class).getFiles1();
                        String file2 = userSnapshot.getValue(User.class).getFiles2();
                        System.out.println("  File 1 "+ file1 + "  File 2 "+ file2);
                        downloadfiles(file1,file2);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void downloadfiles(String file1, String file2) {

       // StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(file2);

        DownloadManager downloadManager = (DownloadManager) getApplicationContext().
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(file1);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(getApplicationContext(), DIRECTORY_DOWNLOADS, "Try123.pdf");

        downloadManager.enqueue(request);

        DownloadManager downloadManager1 = (DownloadManager) getApplicationContext().
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri1 = Uri.parse(file2);
        DownloadManager.Request request1 = new DownloadManager.Request(uri1);

        request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request1.setDestinationInExternalFilesDir(getApplicationContext(), DIRECTORY_DOWNLOADS, "Try1.pdf");

        downloadManager1.enqueue(request1);
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

    private void notification2(String num) {
        // Builds your notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("John's Android Studio Tutorials")
                .setContentText("Message from " + num);

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
