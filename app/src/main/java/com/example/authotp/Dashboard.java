package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static android.app.DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class Dashboard extends AppCompatActivity {

    //String key = "";

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
        intent.putExtra("phone", currentUser.getPhonenumber());
        startActivity(intent);
    }

    private void EditInfo() {
        //this gotta be fix... few corner cases check...
        /*
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
         */

        //SharePreHelper.setName(null);
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

    private User currentUser;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    Intent serviceIntent;

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
                assert message != null;
                boolean check = message.isCheck();
                String toNumber = message.getTo();
                //count++;
                if(!check && toNumber.equals(currentUser.getPhonenumber())) {
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

    private void updateScrollView() {
        final ScrollView scrollView = findViewById(R.id.scrollView);

        DatabaseReference dbref = firebaseDatabase.getReference().child("Message");

        Query query = dbref.orderByChild("to").equalTo(currentUser.getPhonenumber());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //LinearLayout linearLayout = findViewById(R.id.scrollViewLinearLayout);
                //linearLayout.removeAllViews();
                TableLayout tableLayout = findViewById(R.id.tableLayout);
                tableLayout.removeAllViews();
                for(DataSnapshot messageSnapshot : dataSnapshot.getChildren()){
                    if(messageSnapshot.exists()){
                        Message newMessage = messageSnapshot.getValue(Message.class);
                        writeTextView(newMessage.getFrom(), newMessage.getKey());
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void writeTextView(String phone, final String key){
        String name = "";
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            String phoneNum = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "").replace("-","");
            if(phoneNum.equals(phone)){
                name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                System.out.println("Hello There I am here! " + name);
            }
            //phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        phones.close();
        TableLayout ll = (TableLayout) findViewById(R.id.tableLayout);

        //ll.isColumnShrinkable(0);
        ll.setColumnStretchable(0, true);
        ll.setColumnStretchable(1, false);
        ll.setColumnStretchable(2, false);
        TableRow row= new TableRow(this);

        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.MATCH_PARENT,0.15f);
        row.setLayoutParams(lp);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.save_symbol);

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
        });

        Bitmap bitmapDownload = BitmapFactory.decodeResource(getResources(),R.drawable.download_img);

        int height1 = (bitmapDownload.getHeight() * 128 / bitmapDownload.getWidth());
        Bitmap scale1 = bitmapDownload.createScaledBitmap(bitmapDownload, 128,height1, true);


        ImageButton downloadBtn = new ImageButton(this);
        //downloadBtn.setImageResource(R.drawable.save_symbol);
        downloadBtn.setImageBitmap(scale1);
       // Button downloadBtn = new Button(this);
        //downloadBtn.setText("Download File");
        downloadBtn.setOnClickListener(new View.OnClickListener() {
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
        });
        TextView number = new TextView(this);



        number.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        number.setGravity(Gravity.CENTER);
        number.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.MATCH_PARENT,0.6f));
       // row.getChildAt(0).setLayoutParams();
        if(name.equals("")){
            number.setText(phone);
        }else{
            number.setText(name);
        }
        number.setTag(key);
        //checkBox.setText("hello");
        //row.addView(checkBox);
        row.addView(number);
        //row.addView(space);
        row.addView(saveButton);
        //row.addView(space);
        row.addView(downloadBtn);

        ll.addView(row, 0);
        //System.out.println(phone);
       // LinearLayout linearLayout = findViewById(R.id.scrollViewLinearLayout);
        //TextView textView = new TextView(this);
        //textView.setText(phone);
        //textView.setTag(key);
        //textView.setOnClickListener(onClickListener);
        //linearLayout.addView(textView,0);
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

             ArrayList<ContentValues> data = new ArrayList<ContentValues>();

             ContentValues row2 = new ContentValues();
             row2.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
             row2.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
             row2.put(ContactsContract.CommonDataKinds.Email.LABEL, "Instagram");
             row2.put(ContactsContract.CommonDataKinds.Email.ADDRESS, insta);
             data.add(row2);

             ContentValues row3 = new ContentValues();
             row3.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
             row3.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
             row3.put(ContactsContract.CommonDataKinds.Email.LABEL, "Snapchat");
             row3.put(ContactsContract.CommonDataKinds.Email.ADDRESS, snap);
             data.add(row3);

             ContentValues row4 = new ContentValues();
             row4.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
             row4.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
             row4.put(ContactsContract.CommonDataKinds.Email.LABEL, "GitHub");
             row4.put(ContactsContract.CommonDataKinds.Email.ADDRESS, gitHub);
             data.add(row4);

             ContentValues row5 = new ContentValues();
             row5.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
             row5.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
             row5.put(ContactsContract.CommonDataKinds.Email.LABEL, "LinkedIn");
             row5.put(ContactsContract.CommonDataKinds.Email.ADDRESS, linkedin);
             data.add(row5);

             ContentValues row6 = new ContentValues();
             row6.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
             row6.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
             row6.put(ContactsContract.CommonDataKinds.Email.LABEL, "Website");
             row6.put(ContactsContract.CommonDataKinds.Email.ADDRESS, website);
             data.add(row6);


             i.setData(contactUri);
             i.putExtra("finishActivityOnSaveCompleted", true);

             i.putExtra(ContactsContract.Intents.Insert.PHONE_ISPRIMARY,phoneNumber);
             i.putExtra(ContactsContract.Intents.Insert.EMAIL_ISPRIMARY,email);
             //i.putExtra(ContactsContract.Intents.Insert.NOTES, notes);

             i.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);

             startActivity(i);
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


             ArrayList<ContentValues> data = new ArrayList<ContentValues>();

             ContentValues row2 = new ContentValues();
             row2.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
             row2.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
             row2.put(ContactsContract.CommonDataKinds.Email.LABEL, "Instagram");
             row2.put(ContactsContract.CommonDataKinds.Email.ADDRESS, insta);
             data.add(row2);

             ContentValues row3 = new ContentValues();
             row3.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
             row3.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
             row3.put(ContactsContract.CommonDataKinds.Email.LABEL, "Snapchat");
             row3.put(ContactsContract.CommonDataKinds.Email.ADDRESS, snap);
             data.add(row3);

             ContentValues row4 = new ContentValues();
             row4.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
             row4.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
             row4.put(ContactsContract.CommonDataKinds.Email.LABEL, "GitHub");
             row4.put(ContactsContract.CommonDataKinds.Email.ADDRESS, gitHub);
             data.add(row4);

             ContentValues row5 = new ContentValues();
             row5.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
             row5.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
             row5.put(ContactsContract.CommonDataKinds.Email.LABEL, "LinkedIn");
             row5.put(ContactsContract.CommonDataKinds.Email.ADDRESS, linkedin);
             data.add(row5);

             ContentValues row6 = new ContentValues();
             row6.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
             row6.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
             row6.put(ContactsContract.CommonDataKinds.Email.LABEL, "Website");
             row6.put(ContactsContract.CommonDataKinds.Email.ADDRESS, website);
             data.add(row6);

             Intent intent_demo = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
             intent_demo.putExtra(ContactsContract.Intents.Insert.NAME, name);
             intent_demo.putExtra(ContactsContract.Intents.Insert.EMAIL, email);
             intent_demo.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
             intent_demo.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
             startActivity(intent_demo);

        }


        /*
        Intent intent123 = new Intent(this, CheckContacts.class);
        intent123.putExtra("phone_no", phoneNumber);
        startActivity(intent123);




        boolean isExisting = false;

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        while(cursor.moveToNext()){
            isExisting = true;
            long idContact = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            Intent i = new Intent(Intent.ACTION_EDIT);
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, idContact);
            i.setData(contactUri);
            i.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            i.putExtra("finishActivityOnSaveCompleted", true);
            //i.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            //i.putExtra(String.valueOf(ContactsContract.CommonDataKinds.Email.IS_PRIMARY),email);
            i.putExtra(ContactsContract.Intents.Insert.PHONE_ISPRIMARY,phoneNumber);
            //i.putExtra(ContactsContract.Intents.Insert.NAME,name);
            i.putExtra(ContactsContract.Intents.Insert.NOTES, notes);

            i.setDataAndType(contactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            startActivity(i);


         */



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


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view instanceof TextView){
                TextView tv = (TextView)view;
                String selectedUserNumber = tv.getText().toString();
                String key = tv.getTag().toString();
                //getFileFromNumber(selectedUserNumber ,key);
                Intent intent = new Intent(getApplicationContext(), SaveInfo.class);
                intent.putExtra("key", key);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), selectedUserNumber, Toast.LENGTH_SHORT).show();
            }
        }
    };

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

    private void getSharedPref(SharedPreferences sharedPreferences){
        currentUser.setName(sharedPreferences.getString("name",""));
        currentUser.setPhonenumber(sharedPreferences.getString("phone",""));
        currentUser.setEmail(sharedPreferences.getString("email", ""));
        currentUser.setWebsite(sharedPreferences.getString("website", ""));
        currentUser.setInstagram(sharedPreferences.getString("insta",""));
        currentUser.setSnapchat(sharedPreferences.getString("snap",""));
        currentUser.setGitHub(sharedPreferences.getString("github",""));
        currentUser.setLinkedIn(sharedPreferences.getString("linkedIn",""));
      //  currentUser.setFiles1(sharedPreferences.getString("file1",""));
        //currentUser.setFiles2(sharedPreferences.getString("file2",""));
    }

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
