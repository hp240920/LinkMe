package com.example.authotp;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.authotp.Threads.GetCurrentUserThread;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyListAdapter extends BaseAdapter {

    Activity context;
    ArrayList<Row> rows;
    private static LayoutInflater inflater = null;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private static int ON_REQUEST_CONTACT = 5;
    Dialog popUpDialog;
    Thread getCurrentUser;
    GetCurrentUserThread userThread;

    public MyListAdapter(@NonNull Activity context, ArrayList<Row> rows) {
        this.context = context;
        this.rows = rows;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popUpDialog = new Dialog(context);

        userThread = new GetCurrentUserThread(context);
        getCurrentUser = new Thread(userThread);
        getCurrentUser.start();
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Nullable
    @Override
    public Row getItem(int position) {
        return rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if(itemView == null){
            itemView = inflater.inflate(R.layout.list_view, null);
        }

        TextView textView = itemView.findViewById(R.id.textView);

        String name = rows.get(position).getName().getFrom();

        ImageView profile_pic = itemView.findViewById(R.id.profile);
        profile_pic.setImageResource(rows.get(position).getProfile());
        // Set profile thread
        Set_profile profile = new Set_profile(name,profile_pic);
        Thread profile_thread = new Thread(profile);
        profile_thread.start();
        openPopUpWhenClicked(name,profile_pic); // Setting on click listener

        ImageButton imageButton_save = itemView.findViewById(R.id.btnSave);
        ImageButton imageButton_download = itemView.findViewById(R.id.btnDownload);

        imageButton_download.setImageResource(rows.get(position).getImg_download());
        imageButton_download.setTag(name);
        imageButton_download.setOnClickListener(new onClickBtn(imageButton_download));

        imageButton_save.setImageResource(rows.get(position).getImg_save());
        imageButton_save.setTag(name);
        imageButton_save.setOnClickListener(onClickSave);

        textView.setText(name);
        textView.setTag(name);
        if(!rows.get(position).getName().isNotify()){
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }

        Set_contact_names contact_names = new Set_contact_names(textView,name);
        Thread fill_contact = new Thread(contact_names);
        fill_contact.start();

        return itemView;
    }

    private View.OnClickListener onClickSave = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DatabaseReference databaseReference = firebaseDatabase.getReference().child("Message");
            Query query = databaseReference.orderByChild("from").equalTo(view.getTag().toString()).limitToLast(1);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot messageSnapshot : snapshot.getChildren()){
                        if(messageSnapshot.exists()){
                            String file1 = messageSnapshot.getValue(Message.class).getFile1();
                            if(file1 != null){
                                saveToContact(file1);
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    };

    private void saveToContact(String file1) {
        System.out.println(file1 + " , ");
        file1 = file1 + " , ";
        String[] information = file1.split(", ");
        System.out.println(information.length);
        String name = information[0];
        String phoneNumber = information[1];
        String email = information[2];
        String website = information[3];
        String insta = information[4];
        String snap = information[5];
        String gitHub = information[6];
        //if(information[7] == nullzzz)
        String linkedin = information[7];
        //System.out.println(linkedin);


        boolean isExisting = false;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

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
            context.startActivityForResult(i, ON_REQUEST_CONTACT);
        }



        if(isExisting == false){

            Intent intent_demo = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
            intent_demo.putExtra(ContactsContract.Intents.Insert.NAME, name);
            if(!email.equals("null")){
                intent_demo.putExtra(ContactsContract.Intents.Insert.EMAIL, email);
            }
            intent_demo.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
            intent_demo.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
            context.startActivityForResult(intent_demo, ON_REQUEST_CONTACT);

        }
    }

    private ArrayList<ContentValues> setLabels(String insta, String snap, String gitHub, String linkedin, String website){
        ArrayList<ContentValues> data = new ArrayList<ContentValues>();
        if(!insta.equals("null") && !insta.isEmpty()) {
            ContentValues row2 = new ContentValues();
            row2.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            row2.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
            row2.put(ContactsContract.CommonDataKinds.Website.LABEL, "Instagram");
            row2.put(ContactsContract.CommonDataKinds.Website.URL, insta);
            data.add(row2);
        }

        if(!snap.equals("null") && !snap.isEmpty()){
            ContentValues row3 = new ContentValues();
            row3.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            row3.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
            row3.put(ContactsContract.CommonDataKinds.Website.LABEL, "Snapchat");
            row3.put(ContactsContract.CommonDataKinds.Website.URL, snap);
            data.add(row3);
        }

        if(!gitHub.equals("null")){
            ContentValues row4 = new ContentValues();
            row4.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            row4.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
            row4.put(ContactsContract.CommonDataKinds.Website.LABEL, "GitHub");
            row4.put(ContactsContract.CommonDataKinds.Website.URL, gitHub);
            data.add(row4);
        }

        if(!linkedin.equals("null") && !linkedin.isEmpty()){
            ContentValues row5 = new ContentValues();
            row5.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            row5.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
            row5.put(ContactsContract.CommonDataKinds.Website.LABEL, "LinkedIn");
            row5.put(ContactsContract.CommonDataKinds.Website.URL, linkedin);
            data.add(row5);
        }

        if(!website.equals("null") && !website.isEmpty()){
            ContentValues row6 = new ContentValues();
            row6.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            row6.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM);
            row6.put(ContactsContract.CommonDataKinds.Website.LABEL, "Website");
            row6.put(ContactsContract.CommonDataKinds.Website.URL, website);
            data.add(row6);
        }
        return data;
    }


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


    private class onClickBtn implements View.OnClickListener {

        ImageButton downloadBtn;
        public onClickBtn(ImageButton button){
            this.downloadBtn = button;
        }

        @Override
        public void onClick(View view) {

            LinearLayout ll = (LinearLayout) view.getParent();
            TextView tv = (TextView) ll.getChildAt(1);
            tv.setTypeface(Typeface.DEFAULT);

            if(Permissions.check_storage_permission(context)){

                final List<String> options = new ArrayList<>();
                final List<Uri> tags = new ArrayList<>();
                final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                final DatabaseReference dbref = firebaseDatabase.getReference().child("Message");
                Query query = dbref.orderByChild("to").equalTo(userThread.getPhonenumber());
                query.addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //int limit = 0;
                        for(DataSnapshot values : snapshot.getChildren()){
                            String key = values.getKey();
                            //System.out.println("KEY: " + key + "        " + limit);
                            Message message = values.getValue(Message.class);
                            if(message.getFrom().equals(downloadBtn.getTag())){
                                Uri uri = Uri.parse(message.getFile2());
                                options.add(0, uri.getLastPathSegment());
                                tags.add(0, uri);
                                firebaseDatabase.getReference("Message").child(key).child("notify").setValue(true);
                                //limit++;
                            }
                        }
                        List<String> last_5_options = new ArrayList<>();
                        if(options.size() > 5){
                            last_5_options = options.subList(0, 5);
                        }else{
                            last_5_options = options;
                        }
                        List<Uri> last_5_tags = new ArrayList<>();
                        if(tags.size() > 5){
                            last_5_tags = tags.subList(0, 5);
                        }else {
                            last_5_tags = tags;
                        }
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, last_5_options);
                        ArrayList<Call_Row> rows = new ArrayList<>();
                        rows.add(new Call_Row(last_5_options.get(0), last_5_tags.get(0), R.drawable.ic_baseline_save_alt_24));
                        rows.add(new Call_Row(last_5_options.get(1), last_5_tags.get(1), R.drawable.ic_baseline_save_alt_24));
                        rows.add(new Call_Row(last_5_options.get(2), last_5_tags.get(2), R.drawable.ic_baseline_save_alt_24));
                        rows.add(new Call_Row(last_5_options.get(3), last_5_tags.get(3), R.drawable.ic_baseline_save_alt_24));
                        rows.add(new Call_Row(last_5_options.get(4), last_5_tags.get(4), R.drawable.ic_baseline_save_alt_24));
                        PopUpAdapter popUpAdapter = new PopUpAdapter(context, rows);
                        popUpDialog.setContentView(R.layout.popup_window);
                        //popUpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        ImageView profilepic = popUpDialog.findViewById(R.id.popUpImage);
                        profilepic.setVisibility(View.GONE);
                        final ListView lv = popUpDialog.findViewById(R.id.list_of_files);
                        lv.setAdapter(popUpAdapter);
                        popUpDialog.show();
                        /*
                        final List<Uri> finalLast_5_tags = last_5_tags;
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                String to_download = finalLast_5_tags.get(i).toString();
                                lv.getChildAt(i).setBackgroundColor(Color.parseColor("#6099cc00"));
                                downloadFile(to_download);
                            }
                        });
                         */
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    }


    private void downloadFile(String file2) {

        File filepath = new File(Environment.getExternalStorageDirectory() + "/MYAPP");
        if(!filepath.exists()){
            filepath.mkdir();
        }
        DownloadManager downloadManager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(file2);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir( "/MYAPP/", uri.getLastPathSegment());
        downloadManager.enqueue(request);
    }

    class Set_contact_names implements Runnable{

        TextView textView;
        String phone_from;
        final Handler updatePhone = new Handler();
        public Set_contact_names(TextView textView, String phone_from){
            this.textView = textView;
            this.phone_from = phone_from;
        }
        @Override
        public void run() {
            String name = "";
            try{
                Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                while (phones.moveToNext()) {
                    String phoneNum = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "").replace("-", "");
                    if (phoneNum.equals(phone_from)) {
                        name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                        final String finalName = name;
                        updatePhone.post(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(finalName);
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
    }


    class Set_profile implements Runnable{

        String phone;
        ImageView profile_pic;
        public Set_profile(String phone, ImageView profile_pic){
            this.phone = phone;
            this.profile_pic = profile_pic;
        }

        @Override
        public void run() {
            try{
                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
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
                    }catch(Exception e){
                        System.out.println(e.toString());
                    }
                }
    }

}

class PopUpAdapter extends BaseAdapter{
    Activity context;
    ArrayList<Call_Row> rows;
    private static LayoutInflater inflater = null;

    public PopUpAdapter(Activity context, ArrayList<Call_Row> rows){
        this.context = context;
        this.rows = rows;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View itemView = view;
        if(itemView == null){
            itemView = inflater.inflate(R.layout.pop_up_list, null);
        }
        TextView textView = itemView.findViewById(R.id.file_name);
        String name = rows.get(i).getName();
        textView.setText(name);
        ImageButton btn = itemView.findViewById(R.id.download_file);
        btn.setImageResource(rows.get(i).getSend_img());
        btn.setTag(rows.get(i).getUri());
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view instanceof ImageButton){
                    ImageButton btn = (ImageButton) view;
                    String uri = (String) btn.getTag().toString();
                    LinearLayout lv = (LinearLayout) btn.getParent();
                    lv.setBackgroundColor(Color.parseColor("#6099cc00"));
                    downloadFile(uri);
                }
            }
        });
        return itemView;
    }

    private void downloadFile(String file2) {

        File filepath = new File(Environment.getExternalStorageDirectory() + "/MYAPP");
        if(!filepath.exists()){
            filepath.mkdir();
        }
        DownloadManager downloadManager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(file2);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir( "/MYAPP/", uri.getLastPathSegment());
        downloadManager.enqueue(request);
    }
}
