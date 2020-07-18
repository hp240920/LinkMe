package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.authotp.Threads.GetCurrentUserThread;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.Table;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class Dashboard extends AppCompatActivity {

    //String key = "";
    ProgressDialog loadImage;
    private static int ON_REQUEST_CONTACT = 5;
    private long total_messages;
    private long counter = 0;
    Thread getCurrentUser;
    GetCurrentUserThread userThread;

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
            case R.id.delete_files:
                Log.i("Selected :","delete files");
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
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

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

        if (ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(Dashboard.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Dashboard.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.READ_CALL_LOG},
                    1);
        }

        // Get all the values from Shared Prefrences for a background thread;
         userThread = new GetCurrentUserThread(Dashboard.this);
         getCurrentUser = new Thread(userThread);
         getCurrentUser.start();

        /*
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
        currentUser = new User();
        getSharedPref(sharedPreferences);
         */



        // Custom popup window created for profile pic and display a list of file of a particular user.
        popUpDialog = new Dialog(this);


        // Getting Firebase Database and Storage
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage =FirebaseStorage.getInstance();

        // update the scroll view

        //loadImage = new ProgressDialog(Dashboard.this);
        //loadImage.setTitle("Processing");
        //loadImage.setMessage("Please Wait...");
        //loadImage.show();
        updateScrollView();
        //loadImage.dismiss();

        // check if service is not started if not then start a service
        if(!isMyServiceRunning(Notify.class)){
            serviceIntent = new Intent(this, com.example.authotp.Notify.class);
            serviceIntent.putExtra("inputExtra", "AuthOTP");
            ContextCompat.startForegroundService(this, serviceIntent);
            //check_notification();
        }
      //  loadProfile.start();
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



    final ArrayList<String> dashboardUserNumbers = new ArrayList<>();
    private void updateScrollView() {
        DatabaseReference dbref = firebaseDatabase.getReference().child("Message");
        Query query = dbref.orderByChild("to").equalTo(userThread.getPhonenumber());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //System.out.println("hello there 123");
                total_messages = dataSnapshot.getChildrenCount();
                //Log.i("Total messages :",Integer.toString((int) total_messages));
                for(DataSnapshot messageSnapshot : dataSnapshot.getChildren()){
                    //System.out.println("hello there 123");
                    if(messageSnapshot.exists()){
                        Message newMessage = messageSnapshot.getValue(Message.class);
                        if(dashboardUserNumbers.contains(newMessage.getFrom())){
                            continue;
                        }
                        dashboardUserNumbers.add(newMessage.getFrom());
                        writeTextView(newMessage.getFrom(), newMessage.getKey());
                    }
                }


            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void writeTextView (final String phone, final String key){

        TableLayout ll = (TableLayout) findViewById(R.id.tableLayout);
        ll.setColumnStretchable(0, true);
        final ImageView profile_pic = new ImageView(this);
        profile_pic.setImageResource(R.drawable.ic_baseline_account_circle_24);


        new Thread(){
            @Override
            public void run() {
                super.run();
                StorageReference profileRef = firebaseStorage.getReference().child("Profiles/" +phone+"/" + "profile.jpg");
                profileRef.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Failed");
                    }
                }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profile_pic);
                        //loadImage.dismiss();
                        counter++;

                    }
                });
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

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.save_symbol);
        int height = (bitmap.getHeight() * 128 / bitmap.getWidth());
        Bitmap scale = Bitmap.createScaledBitmap(bitmap, 128,height, true);
        ImageButton saveButton = new ImageButton(this);
        saveButton.setImageBitmap(scale);
        //saveButton.setImageResource(R.drawable.save_symbol);
        //saveButton.setScaleType(ImageView.ScaleType.FIT_XY);
        //Button addBtn = new Button(this);
        //addBtn.setText("Save Info");
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
        });

        Bitmap bitmapDownload = BitmapFactory.decodeResource(getResources(), R.drawable.download_img);
        int height1 = (bitmapDownload.getHeight() * 128 / bitmapDownload.getWidth());
        Bitmap scale1 = bitmapDownload.createScaledBitmap(bitmapDownload, 128,height1, true);
        final ImageButton downloadBtn = new ImageButton(this);
        downloadBtn.setImageBitmap(scale1);
        downloadBtn.setTag(phone);


       // Button downloadBtn = new Button(this);
        //downloadBtn.setText("Download File");
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Display alert box with various options

                final List<String> options = new ArrayList<>();
                final List<Uri> tags = new ArrayList<>();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference dbref = firebaseDatabase.getReference().child("Message");
                Query query = dbref.orderByChild("to").equalTo(userThread.getPhonenumber());
                query.addValueEventListener(new ValueEventListener(){

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int limit = 0;
                        for(DataSnapshot values : snapshot.getChildren()){
                            if(limit ==5){
                                break;
                            }
                            Message message = values.getValue(Message.class);
                            if(message.getFrom().equals(downloadBtn.getTag())){
                                Uri uri = Uri.parse(message.getFile2());
                                options.add(uri.getLastPathSegment());
                                tags.add(uri);
                                limit++;
                            }
                        }


                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Dashboard.this, android.R.layout.simple_list_item_1, options );
                        popUpDialog.setContentView(R.layout.popup_window);
                        popUpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        ImageView profilepic = popUpDialog.findViewById(R.id.popUpImage);
                        profilepic.setVisibility(View.GONE);
                        ListView lv = popUpDialog.findViewById(R.id.list_of_files);
                        lv.setAdapter(arrayAdapter);
                        popUpDialog.show();
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                String to_download = tags.get(i).toString();
                                downloadFile(to_download);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });


        final TextView number = new TextView(this);
        number.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        number.setGravity(Gravity.CENTER);
        number.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.MATCH_PARENT,0.6f));

        // thread to insert name if present in contacts
        new Thread() {
            @Override
            public void run() {
                super.run();
                String name = "";
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                while (phones.moveToNext()) {
                    String phoneNum = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "").replace("-", "");
                    if (phoneNum.equals(phone)) {
                        name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        TextView tvNumber = (TextView) row.getChildAt(1);
                        tvNumber.setText(name);
                        break;
                        //System.out.println("Hello There I am here! " + name);
                    }
                    //phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                phones.close();


            }
        }.start();

        number.setText(phone);
        number.setTag(key);
        //checkBox.setText("hello");
        //row.addView(checkBox);
        row.addView(profile_pic);

        row.addView(number);
        //row.addView(space);
        row.addView(saveButton);
        //row.addView(space);
        row.addView(downloadBtn);
        ll.addView(row, 0);

    }

    private void openPopUpWhenClicked(final String phone, ImageView profile_pic){

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpDialog.setContentView(R.layout.popup_window);
                popUpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                StorageReference profileRef = firebaseStorage.getReference().child("Profiles/" +phone+"/" + "profile.jpg");
                profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        final ImageView popUpImg = popUpDialog.findViewById(R.id.popUpImage);
                        Picasso.get().load(uri).into(popUpImg);
                        popUpDialog.show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
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




        boolean isExisting = false;

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

         if(cursor.moveToFirst()) {
             isExisting = true;
             long idContact = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
             Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, idContact);
             Intent i = new Intent(Intent.ACTION_EDIT,contactUri);

             ArrayList<ContentValues> data = setLabels(insta,snap,gitHub,linkedin,website);

             i.setData(contactUri);
             i.putExtra("finishActivityOnSaveCompleted", true);

             i.putExtra(ContactsContract.Intents.Insert.PHONE_ISPRIMARY,phoneNumber);
             i.putExtra(ContactsContract.Intents.Insert.EMAIL_ISPRIMARY,email);
             //i.putExtra(ContactsContract.Intents.Insert.NOTES, notes);

             i.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);

             startActivityForResult(i,ON_REQUEST_CONTACT);
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


             ArrayList<ContentValues> data = setLabels(insta,snap,gitHub,linkedin,website);

             Intent intent_demo = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
             intent_demo.putExtra(ContactsContract.Intents.Insert.NAME, name);
             intent_demo.putExtra(ContactsContract.Intents.Insert.EMAIL, email);
             intent_demo.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
             intent_demo.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
             startActivityForResult(intent_demo,ON_REQUEST_CONTACT);

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ON_REQUEST_CONTACT){
            updateScrollView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateScrollView();
    }

    private ArrayList<ContentValues> setLabels(String insta, String snap, String gitHub, String linkedin, String website){
        ArrayList<ContentValues> data = new ArrayList<ContentValues>();

        ContentValues row2 = new ContentValues();
        row2.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
        row2.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
        row2.put(ContactsContract.CommonDataKinds.Website.LABEL, "Instagram");
        row2.put(ContactsContract.CommonDataKinds.Website.URL, insta);
        data.add(row2);

        ContentValues row3 = new ContentValues();
        row3.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
        row3.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
        row3.put(ContactsContract.CommonDataKinds.Website.LABEL, "Snapchat");
        row3.put(ContactsContract.CommonDataKinds.Website.URL, snap);
        data.add(row3);

        ContentValues row4 = new ContentValues();
        row4.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
        row4.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
        row4.put(ContactsContract.CommonDataKinds.Website.LABEL, "GitHub");
        row4.put(ContactsContract.CommonDataKinds.Website.URL, gitHub);
        data.add(row4);

        ContentValues row5 = new ContentValues();
        row5.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
        row5.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
        row5.put(ContactsContract.CommonDataKinds.Website.LABEL, "LinkedIn");
        row5.put(ContactsContract.CommonDataKinds.Website.URL, linkedin);
        data.add(row5);

        ContentValues row6 = new ContentValues();
        row6.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
        row6.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
        row6.put(ContactsContract.CommonDataKinds.Website.LABEL, "Website");
        row6.put(ContactsContract.CommonDataKinds.Website.URL, website);
        data.add(row6);

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



    private void getFileFromNumber(String selectedUserNumber, String key) {

        DatabaseReference dbref = firebaseDatabase.getReference().child("Message");

        Query query = dbref.orderByChild("key").equalTo(key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot messageSnapshot : dataSnapshot.getChildren()){
                    if(messageSnapshot.exists()){
                        String file1 = messageSnapshot.getValue(Message.class).getFile1();
                        String file2 = messageSnapshot.getValue(Message.class).getFile2();
                        String phoneNumber = messageSnapshot.getValue(Message.class).getFrom();
                        if(file1!=null){
                            downloadfiles(file1,phoneNumber);
                        }
                        if(file2!=null){
                            downloadfiles(file2,phoneNumber);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

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
        // Builds your notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("AuthOPT Incoming")
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
