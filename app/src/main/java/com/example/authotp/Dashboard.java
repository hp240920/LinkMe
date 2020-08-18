package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.authotp.Threads.GetCurrentUserThread;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private static final int REQUEST_CODE = 3055;
    private static final int REQUEST_CONTACT = 3066;
    private static final int REQUEST_PHONE = 3067;
    private static final int REQUEST_STORAGE = 3068;
    //String key = "";
    ProgressDialog loadImage;
    private static int ON_REQUEST_CONTACT = 5;
    private long total_messages;
    private long counter = 0;
    Thread getCurrentUser;
    GetCurrentUserThread userThread;
    boolean isFirstTimeRun = true;

    private  DrawerLayout drawerLayout;
    private NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;

    /*
        @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dashboard_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

     */



    //private User currentUser;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    Intent serviceIntent;
    Dialog popUpDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        setTitle("Dashboard");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Permissions.check_phone_permission(Dashboard.this)){
                requestPermissions(new String[] {
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG,
                }, REQUEST_PHONE);
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Permissions.check_contacts_permission(Dashboard.this)){
                requestPermissions(new String[] {
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                }, REQUEST_CONTACT);
            }
        }


        // Get all the values from Shared Prefrences for a background thread;


        /*
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
        currentUser = new User();
        getSharedPref(sharedPreferences);
         */

        // here we can make a method

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setNativagationDrawer();
                updateScrollView();
                pullToRefresh.setRefreshing(false);
            }
        });

        if(!isMyServiceRunning(Notify.class)){
            //System.out.println("HELLO");
            serviceIntent = new Intent(this, Notify.class);
            serviceIntent.putExtra("inputExtra", "start");
            ContextCompat.startForegroundService(this, serviceIntent);
            check_notification();
        }




        // Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        setNativagationDrawer();
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Custom popup window created for profile pic and display a list of file of a particular user.
        popUpDialog = new Dialog(this);

        // Getting Firebase Database and Storage
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage =FirebaseStorage.getInstance();

        // update the scroll view
        userThread = new GetCurrentUserThread(Dashboard.this);
        getCurrentUser = new Thread(userThread);
        getCurrentUser.start();

        //loadImage = new ProgressDialog(Dashboard.this);
        //loadImage.setTitle("Processing");
        //loadImage.setMessage("Please Wait...");
        //loadImage.show();
        updateScrollView();
        //loadImage.dismiss();

        // check if service is not started if not then start a service

        startDetector();
        //  loadrofile.start();
    }

    private void setNativagationDrawer(){
        navigationView = findViewById(R.id.navigation_bar);
        navigationView.setNavigationItemSelectedListener(Dashboard.this);

        View header = navigationView.getHeaderView(0);
        final ImageView my_profile = (ImageView) header.findViewById(R.id.nav_profile);
        my_profile.setImageResource(R.mipmap.ic_launcher_round);
        TextView my_name = (TextView) header.findViewById(R.id.nav_name);
        TextView my_phone = (TextView) header.findViewById(R.id.nav_number);
        //my_profile.setImageResource(R.drawable.default_dp);
        FirebaseStorage storage = FirebaseStorage.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
        String phone = sharedPreferences.getString("phone", "");
        String name = sharedPreferences.getString("name", "");
        if(!name.equals("")){
            my_name.setText(name);
        }
        //assert phone != null;
        if(!phone.equals("")){
            my_phone.setText(phone);
            StorageReference profileRef = storage.getReference().child("Profiles/" + Objects.requireNonNull(phone));
            profileRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                @Override
                public void onSuccess(ListResult listResult) {
                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if(uri != null){
                                    Picasso.get().load(uri).into(my_profile);
                                }
                            }
                        });
                    }
                }
            });
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        System.out.println("Clicked");
        drawerLayout.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.edit_information:
                Log.i("Selected :", "edit info");
                EditInfo();
                return true;
            case R.id.delete_files:
                Log.i("Selected :", "delete files");
                deleteFiles();
                return true;
            case R.id.call_log:
                Log.i("Selected :", "call log");
                onCallLog();
                return true;
            case R.id.search_nearby:
                Log.i("Selected :", "Search nearby");
                onSearchNearby();
                return true;

            case R.id.about_us:
                Log.i("Selected :", "about us");
                return true;
            case R.id.log_out:
                Log.i("Selected :", "log out");
                logOut();
                return true;
            default:
                return false;
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
            }
        return false;
    }

    private void onSearchNearby() {
        Intent intent = new Intent(this,SearchNearby.class);
        startActivity(intent);
    }

    private void onCallLog(){
        Intent intent = new Intent(this, call_log.class);
        startActivity(intent);
    }

    private void deleteFiles() {
        Intent intent = new Intent(this, deleteFiles.class);
        intent.putExtra("phone", userThread.getPhonenumber());
        startActivity(intent);
    }

    private void EditInfo() {


        if(!getCurrentUser.isAlive()){
            User currentUser = userThread.getCurrentUser();
            Intent intent = new Intent(this, Sign_Up.class);
            intent.putExtra("check", true);
            intent.putExtra("name", currentUser.getName());
            intent.putExtra("insta", currentUser.getInstagram());
            intent.putExtra("snap", currentUser.getSnapchat());
            intent.putExtra("email", currentUser.getEmail());
            intent.putExtra("website", currentUser.getWebsite());
            intent.putExtra("linkedin", currentUser.getLinkedIn());
            intent.putExtra("git", currentUser.getGitHub());
            intent.putExtra("phoneNo", currentUser.getPhonenumber());
            startActivity(intent);
        }
        else {
            Toast.makeText(this,"ERROR",Toast.LENGTH_SHORT).show();
        }

    }

    // removing shared pref and going to main Activity
    private void logOut() {
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        SharePreHelper.setName(null);
        if(isMyServiceRunning(Notify.class)){
            serviceIntent = new Intent(Dashboard.this, Notify.class);
            serviceIntent.putExtra("inputExtra", "stop");
            startService(serviceIntent);
        }
        PackageManager pm  = Dashboard.this.getPackageManager();
        ComponentName componentName = new ComponentName(Dashboard.this, Detector.class);
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }



    private void startDetector(){
        PackageManager pm  = Dashboard.this.getPackageManager();
        ComponentName componentName = new ComponentName(Dashboard.this, Detector.class);
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CONTACT ){
            if(grantResults.length > 1 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)){
              Intent intent = new Intent(getApplicationContext(), Dashboard.class);
              startActivity(intent);
              finish();
            }
        }
        if(requestCode == REQUEST_PHONE){
            if(grantResults.length > 1 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)){
                Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                startActivity(intent);
                finish();
            }else{
                Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
       // updateScrollView();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
                //System.out.println("KEY: " + key);
                Message message = dataSnapshot.getValue(Message.class);
                //System.out.println("Needed123!!! " + message.toString());
                assert message != null;
                boolean check = message.isCheck();
                String toNumber = message.getTo();
                //count++;
                if(!check && toNumber.equals(userThread.getPhonenumber())) {
                    notification2(message.getFrom());
                    assert key != null;
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





    ArrayList<Message> dashboardUserNumbers = new ArrayList<>();
    private void updateScrollView() {
        //System.out.println(dashboardUserNumbers.size());
        DatabaseReference dbref = firebaseDatabase.getReference().child("Message");
        while(getCurrentUser.isAlive()){};
        Query query = dbref.orderByChild("to").equalTo(userThread.getPhonenumber());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                total_messages = dataSnapshot.getChildrenCount();
                System.out.println("TOTAL MESSAGE" + total_messages);
                //Log.i("Total messages :",Integer.toString((int) total_messages));
                for(DataSnapshot messageSnapshot : dataSnapshot.getChildren()){
                    //System.out.println("hello there 123");
                    if(messageSnapshot.exists()){
                        Message newMessage = messageSnapshot.getValue(Message.class);
                        //notify = newMessage.isNotify();
                        if(dashboardUserNumbers.contains(newMessage)){
                            if(dashboardUserNumbers.get(dashboardUserNumbers.indexOf(newMessage)).getKey().compareTo(newMessage.getKey()) < 0){
                                dashboardUserNumbers.set(dashboardUserNumbers.indexOf(newMessage),newMessage);
                            }
                            continue;
                        }
                        dashboardUserNumbers.add(newMessage);
                    }
                }

                ArrayList<Row> rows = new ArrayList<>();
                Collections.sort(dashboardUserNumbers);
                //System.out.print("Hello");
                for(Message newMessage : dashboardUserNumbers){
                    //writeTextView(newMessage.getFrom(), newMessage.getKey(),newMessage.isNotify());
                    rows.add(new Row(newMessage,R.drawable.default_dp, R.drawable.ic_round_save_alt_24 ,R.drawable.ic_baseline_save_24));
                }
                MyListAdapter adapter = new MyListAdapter(Dashboard.this, rows);
                ListView list= (ListView) findViewById(R.id.listView);
                //list.addView(new TextView(this));
                list.setAdapter(adapter);
                isFirstTimeRun = false;
                for(Message newMessage : dashboardUserNumbers){
                    if(!newMessage.isNotify()){
                        unread_message(newMessage);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    final Message messageObj = snapshot.getValue(Message.class);
                    //final Message messageObj = message.getValue(Message.class);
                    // is notify false
                    if(!messageObj.isNotify()){
                      unread_message(messageObj);
                    }
                    else {
                        // read message
                        read_message(messageObj);
                    }

                }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /*
    private ArrayList<Message> sort_it(ArrayList<Message> list){

        for(int i=0; i<list.size(); i++){

        }
    }
     */

    private void read_message(Message message){
        //TableLayout   ll = findViewById(R.id.tableLayout);
        //int num_of_rows = ll.getChildCount();

        ListView listView = findViewById(R.id.listView);
        int num_of_rows = listView.getChildCount();

        for(int i=0 ; i < num_of_rows; i++){
            LinearLayout currentRow = (LinearLayout) listView.getChildAt(i);
            TextView currentTextView = (TextView)currentRow.getChildAt(1);
            String str = (String) currentTextView.getTag();
            if(str.equals(message.getFrom())){
                // Unread new message
                currentRow.setBackgroundResource(R.color.defaultBackgroundColor);
                currentTextView.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    private void unread_message(Message message){
        ListView listView = findViewById(R.id.listView);
        int num_of_rows = listView.getChildCount();

        for(int i=0 ; i < num_of_rows; i++){
            LinearLayout currentRow = (LinearLayout) listView.getChildAt(i);
            TextView currentTextView = (TextView)currentRow.getChildAt(1);
            String str = (String) currentTextView.getTag();
            if(str.equals(message.getFrom())){
                currentTextView.setTypeface(null, Typeface.BOLD);
            }
        }
    }

    private void writeTextView(final String phone, final String key, final boolean notify){

        System.out.println( "Phone "+ phone + "Notify "+ notify);
        TableLayout ll = (TableLayout) findViewById(R.id.tableLayout);
        ll.setColumnStretchable(0, true);
        final ImageView profile_pic = new ImageView(this);
        profile_pic.setImageResource(R.drawable.ic_baseline_account_circle_24);


        new Thread(){
            @Override
            public void run() {
                super.run();
                try{
                    StorageReference profileRef = firebaseStorage.getReference().child("Profiles/" + phone);
                    profileRef.listAll()
                            .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                @Override
                                public void onSuccess(ListResult listResult) {
                                    for (StorageReference item : listResult.getItems()) {
                                        item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                if(uri != null){
                                                    Picasso.get().load(uri).into(profile_pic);
                                                    counter++;
                                                }
                                            }
                                        });
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Uh-oh, an error occurred!
                                }
                            });
                    /*
                        profileRef.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("Failed");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if(uri != null){
                                Picasso.get().load(uri).into(profile_pic);
                                counter++;
                            }
                        }
                    });
                    */
                }catch(Exception e){
                    System.out.println(e.toString());
                }
            }
        }.start();

        //loadImage.dismiss();
        TableRow.LayoutParams profilePicLayoutParam = new TableRow.LayoutParams(100, 100,0.10f);
        profilePicLayoutParam.gravity = Gravity.CENTER;
        profile_pic.setLayoutParams(profilePicLayoutParam);

        openPopUpWhenClicked(phone, profile_pic);

        //ll.isColumnShrinkable(0);
        ll.setColumnStretchable(1, true);
        ll.setColumnStretchable(2, false);
        ll.setColumnStretchable(3, false);
        final TableRow row= new TableRow(this);

        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.MATCH_PARENT,0.15f);
        row.setLayoutParams(lp);
        //row.setOnClickListener(onClickRow);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.save_symbol);
        int height = (bitmap.getHeight() * 128 / bitmap.getWidth());
        Bitmap scale = Bitmap.createScaledBitmap(bitmap, 128,height, true);
        ImageButton saveButton = new ImageButton(this);
        saveButton.setImageBitmap(scale);
        //saveButton.setImageResource(R.drawable.save_symbol);
        //saveButton.setScaleType(ImageView.ScaleType.FIT_XY);
        //Button addBtn = new Button(this);
        //addBtn.setText("Save Info");
        final String[] output_name = {""};
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference dbref = firebaseDatabase.getReference().child("Message");

                Query query = dbref.orderByChild("key").equalTo(key);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot messageSnapshot : dataSnapshot.getChildren()){
                            if(messageSnapshot.exists()){
                                String file1 = messageSnapshot.getValue(Message.class).getFile1();

                                if(file1 != null){
                                    saveToContact(output_name[0], file1);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });

        Bitmap bitmapDownload = BitmapFactory.decodeResource(getResources(), R.drawable.download_img);
        int height1 = (bitmapDownload.getHeight() * 128 / bitmapDownload.getWidth());
        Bitmap scale1 = bitmapDownload.createScaledBitmap(bitmapDownload, 128,height1, true);
        final ImageButton downloadBtn = new ImageButton(this);
        downloadBtn.setImageBitmap(scale1);
        downloadBtn.setTag(phone);
        downloadBtn.setOnClickListener(new onBtnViewFiles(downloadBtn));


        final TextView number = new TextView(this);
        number.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        number.setGravity(Gravity.CENTER);
        number.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.MATCH_PARENT,0.6f));
       // number.setOnClickListener(onClickRow);


        // thread to insert name if present in contacts
        final Handler updatePhone = new Handler();
        new Thread() {
            @Override
            public void run() {
                super.run();
                String name = "";
                try{
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                    while (phones.moveToNext()) {
                        String phoneNum = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "").replace("-", "");
                        if (phoneNum.equals(phone)) {
                            name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            output_name[0] = name;
                            final TextView tvNumber = (TextView) row.getChildAt(1);
                            final String finalName = name;
                            updatePhone.post(new Runnable() {
                                @Override
                                public void run() {
                                    tvNumber.setText(finalName);
                                    if(!notify){
                                      tvNumber.setTypeface(null, Typeface.BOLD);
                                    }
                                }
                            });
                            break;
                            //System.out.println("Hello There I am here! " + name);
                        }
                        //phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    phones.close();
                }catch(Exception e){

                }
            }
        }.start();

        number.setText(phone);
        number.setTag(phone);

        row.addView(profile_pic);
        row.addView(number);
        row.addView(saveButton);
        row.addView(downloadBtn);

        if(!notify){
            row.setBackgroundResource(R.color.LightGrey);
        }


        ll.addView(row);

    }


    private class onBtnViewFiles implements View.OnClickListener {

        ImageButton downloadBtn;
        public onBtnViewFiles(ImageButton button){
            this.downloadBtn = button;
        }

        @Override
        public void onClick(View view) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(!Permissions.check_storage_permission(Dashboard.this)){
                    requestPermissions(new String[] {
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                }
            }

            if(Permissions.check_storage_permission(Dashboard.this)){
                TableRow row = (TableRow) downloadBtn.getParent();
                final List<String> options = new ArrayList<>();
                final List<Uri> tags = new ArrayList<>();
                final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                final DatabaseReference dbref = firebaseDatabase.getReference().child("Message");
                Query query = dbref.orderByChild("to").equalTo(userThread.getPhonenumber()).limitToLast(5);
                query.addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int limit = 0;
                        //String key = snapshot.getKey();
                        for(DataSnapshot values : snapshot.getChildren()){
                            String key = values.getKey();
                            System.out.println("KEY: " + key);
                            if(limit ==5){
                                break;
                            }
                            Message message = values.getValue(Message.class);
                            if(message.getFrom().equals(downloadBtn.getTag())){
                                Uri uri = Uri.parse(message.getFile2());
                                options.add(0, uri.getLastPathSegment());
                                tags.add(0, uri);
                                firebaseDatabase.getReference("Message").child(key).child("notify").setValue(true);
                                limit++;
                            }
                        }


                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Dashboard.this, android.R.layout.simple_list_item_1, options);
                        popUpDialog.setContentView(R.layout.popup_window);
                        popUpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        ImageView profilepic = popUpDialog.findViewById(R.id.popUpImage);
                        profilepic.setVisibility(View.GONE);
                        final ListView lv = popUpDialog.findViewById(R.id.list_of_files);
                        lv.setAdapter(arrayAdapter);
                        popUpDialog.show();
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                String to_download = tags.get(i).toString();
                                lv.getChildAt(i).setBackgroundColor(Color.parseColor("#6099cc00"));
                                downloadFile(to_download);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    };

    /*
     View.OnClickListener onClickRow = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TextView textview = (TextView) view;
            String number = textview.getTag().toString();
            File filepath = new File(Environment.getExternalStorageDirectory() + "/MYAPP");
            Uri uriToLoad = Uri.fromFile(filepath);
            // Choose a directory using the system's file picker.
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

            // Provide read access to files and sub-directories in the user-selected
            // directory.
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker when it loads.
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);
            startActivity(intent);

        }
    };

     */


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void permission() {
        if (ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.READ_PHONE_STATE) +
                ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.READ_CALL_LOG) +
                ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.READ_CONTACTS) +
                ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.WRITE_CONTACTS) +
                ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.READ_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_CALL_LOG) || shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS) || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder build = new AlertDialog.Builder(Dashboard.this);
                build.setTitle("Grant Permissions");
                build.setMessage("These permissions are required to run this app");
                build.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(new String[] {
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_CALL_LOG,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_CONTACTS,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                    }
                });
                build.setNegativeButton("Cancel", null);
                AlertDialog alert = build.create();
                alert.show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "App won't function properly", Toast.LENGTH_SHORT).show();
        }
    }

    /*
        @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
                if (grantResults.length > 0 &&
                        grantResults[0] + grantResults[1] + grantResults[2] + grantResults[3] + grantResults[4] + grantResults[5] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
        }
        if(requestCode == REQUEST_CODE + 1){
            if (grantResults.length > 0 &&
                    grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Dashboard.this.recreate();
            }
            return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }


     */

    private void openPopUpWhenClicked(final String phone, ImageView profile_pic){

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpDialog.setContentView(R.layout.popup_window);
                popUpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                StorageReference profileRef = firebaseStorage.getReference().child("Profiles/" + phone);
                profileRef.listAll()
                        .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                            @Override
                            public void onSuccess(ListResult listResult) {
                                for (StorageReference item : listResult.getItems()) {
                                    item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            if(uri != null){
                                                final ImageView popUpImg = popUpDialog.findViewById(R.id.popUpImage);
                                                Picasso.get().load(uri).into(popUpImg);
                                                popUpDialog.show();
                                            }
                                        }
                                    });
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("Failed");
                            }
                        });
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

    }

    private void saveToContact(String tag, String file1) {
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


        boolean isExisting = false;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        ArrayList<ContentValues> data = setLabels(insta,snap,gitHub,linkedin,website);

         if(cursor.moveToFirst()) {
             isExisting = true;
             long idContact = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
             Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, idContact);
             Intent i = new Intent(Intent.ACTION_EDIT,contactUri);
             i.setData(contactUri);
             i.putExtra("finishActivityOnSaveCompleted", true);

             i.putExtra(ContactsContract.Intents.Insert.PHONE_ISPRIMARY, phoneNumber);
             if(!email.equals("null")){
                 i.putExtra(ContactsContract.Intents.Insert.EMAIL_ISPRIMARY,email);
             }
             i.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
             startActivityForResult(i, ON_REQUEST_CONTACT);
         }



         if(isExisting == false){
             /*
               Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
            intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL,email);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE,phoneNumber);
            intent.putExtra(ContactsContract.Intents.Insert.NAME,name);
            intent.putExtra(ContactsContract.Intents.Insert.NOTES, notes);
             //intent.putExtra(ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM, notes);
             //intent.putExtra(ContactsContract.Intents.Insert)
              */

             Intent intent_demo = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
             intent_demo.putExtra(ContactsContract.Intents.Insert.NAME, name);
             if(!email.equals("null")){
                 intent_demo.putExtra(ContactsContract.Intents.Insert.EMAIL, email);
             }
             intent_demo.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
             intent_demo.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
             startActivityForResult(intent_demo, ON_REQUEST_CONTACT);

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ON_REQUEST_CONTACT && resultCode == RESULT_OK){
            Intent refresh = new Intent(this, Dashboard.class);
            startActivity(refresh);//Start the same Activity
            finish();
        }

        //finish Activity.
        /*
        if(requestCode == ON_REQUEST_CONTACT){
        if (ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.READ_CONTACTS) +
                ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,
            }, REQUEST_CODE + 1);
        }
            if(resultCode == RESULT_OK){
                String tag_check = data.getStringExtra("tag_check");
                Uri uriData = data.getData();
                String dataName = null;
                String dataPhone = null;
                Cursor cursor = getContentResolver().query(uriData, null, null, null, null);
                if (cursor.moveToFirst()) {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                    String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1"))
                    {
                        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
                        phones.moveToFirst();
                        String cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        dataPhone = cNumber.replace(" ", "").replace("-", "");
                        String nameContact = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                        dataName = nameContact;

                    }
                }

                if (dataName != null && dataPhone != null) {
                  TableLayout   ll = findViewById(R.id.tableLayout);

                    int num_of_rows = ll.getChildCount();

                    for(int i=0 ; i < num_of_rows; i++){
                        TableRow currentRow = (TableRow)ll.getChildAt(i);
                        TextView currentTextView = (TextView)currentRow.getChildAt(1);
                        String str = (String) currentTextView.getTag();
                        if(currentTextView.getTag().equals(dataPhone)){
                            currentTextView.setText(dataName);
                        }
                    }
                }
            }else if(resultCode == RESULT_CANCELED){
                Toast.makeText(getApplicationContext(), "Abort!", Toast.LENGTH_SHORT).show();
            }
        }
        */

    }

    /*
                int id = cursor.getColumnIndex(ContactsContract.Contacts._ID);

                ContentResolver cr = getContentResolver();
                Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                if (phones.moveToFirst()) {
                    dataPhone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
      */

    @Override
    protected void onResume() {
        super.onResume();
        //updateScrollView();
    }

    private ArrayList<ContentValues> setLabels(String insta, String snap, String gitHub, String linkedin, String website){
        ArrayList<ContentValues> data = new ArrayList<ContentValues>();
        if(!insta.equals("null")) {
            insta = "Instagram: " + insta;
            ContentValues row2 = new ContentValues();
            row2.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            row2.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
            row2.put(ContactsContract.CommonDataKinds.Website.LABEL, "Instagram");
            row2.put(ContactsContract.CommonDataKinds.Website.URL, insta);
            data.add(row2);
        }

        if(!snap.equals("null")){
            snap = "SnapChat: " + snap;
            ContentValues row3 = new ContentValues();
            row3.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            row3.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
            row3.put(ContactsContract.CommonDataKinds.Website.LABEL, "Snapchat");
            row3.put(ContactsContract.CommonDataKinds.Website.URL, snap);
            data.add(row3);
        }

        if(!gitHub.equals("null")){
            gitHub = "GitHub: " + gitHub;
            ContentValues row4 = new ContentValues();
            row4.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            row4.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
            row4.put(ContactsContract.CommonDataKinds.Website.LABEL, "GitHub");
            row4.put(ContactsContract.CommonDataKinds.Website.URL, gitHub);
            data.add(row4);
        }

        if(!linkedin.equals("null")){
            linkedin = "LinkedIn: " + linkedin;
            ContentValues row5 = new ContentValues();
            row5.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            row5.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
            row5.put(ContactsContract.CommonDataKinds.Website.LABEL, "LinkedIn");
            row5.put(ContactsContract.CommonDataKinds.Website.URL, linkedin);
            data.add(row5);
        }

        if(!website.equals("null")){
            website = "Website: " + website;
            ContentValues row6 = new ContentValues();
            row6.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            row6.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
            row6.put(ContactsContract.CommonDataKinds.Website.LABEL, "Website");
            row6.put(ContactsContract.CommonDataKinds.Website.URL, website);
            data.add(row6);
        }
        return data;
    }






        /*


        ArrayList < ContentProviderOperation > ops = new ArrayList< ContentProviderOperation >();

        ops.add(ContentProviderOperation.
                newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());



          Cursor mCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,  "NUMBER = " + phoneNumber,null, null);
        mCursor.moveToFirst();
        int lookupKeyIndex;
        int idIndex;
        String currentLookupKey;
        long currentId;
        Uri selectedContactUri;

        // Gets the lookup key column index
        lookupKeyIndex = mCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
        // Gets the lookup key value
        currentLookupKey = mCursor.getString(lookupKeyIndex);
        // Gets the _ID column index
        idIndex = mCursor.getColumnIndex(ContactsContract.Contacts._ID);
        currentId = mCursor.getLong(idIndex);
        selectedContactUri =
                ContactsContract.Contacts.getLookupUri(currentId, currentLookupKey);

        Intent editIntent = new Intent(Intent.ACTION_EDIT);
        editIntent.putExtra(ContactsContract.Intents.Insert.EMAIL,email);
       // editIntent.putExtra(ContactsContract.Intents.Insert.PHONE,phoneNumber);
       // editIntent.putExtra(ContactsContract.Intents.Insert.NAME,name);
        editIntent.putExtra(ContactsContract.Intents.Insert.NOTES, notes);
        editIntent.setDataAndType(selectedContactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        startActivity(editIntent);
         */


/*
        if(isExisting == false){
            Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
            intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL,email);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE,phoneNumber);
            intent.putExtra(ContactsContract.Intents.Insert.NAME,name);
            intent.putExtra(ContactsContract.Intents.Insert.NOTES, notes);
            startActivity(intent);
        }

 */

    private void downloadfiles(String file1, String phoneNumber) {

        File filepath = new File(Environment.getExternalStorageDirectory() + "/MYAPP");
        if(!filepath.exists()){
            filepath.mkdir();
        }
       // StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(file2);

        DownloadManager downloadManager = (DownloadManager) getApplicationContext().
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(file1);
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

    /*
     private void getSharedPref(SharedPreferences sharedPreferences){
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
    }
     */


    private void notification2(String num) {

        if(Detector.getNameOfContact(this,num) != null){
            num = Detector.getNameOfContact(this,num);
        }
        // Builds your notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("LinkMe Incoming")
                .setAutoCancel(true)
                .setContentText("Message from " + num);

        // Creates the intent needed to show the notification
        Intent notificationIntent = new Intent(this, Dashboard.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Rec_Message";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Message Received",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        manager.notify(1, builder.build());
    }

}
