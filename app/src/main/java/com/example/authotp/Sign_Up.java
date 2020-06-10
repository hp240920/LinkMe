package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Sign_Up extends AppCompatActivity {

    EditText name, phoneNo, insta, snap, linkedin, github;
    TextView fileName;
    Button btnSelect , btnSignUp;
    final int REQUEST_CODE = 9;
    final int INTENT_CODE_SELECTFILE = 86;
    private Uri pdfUri;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String displayName = null;
    boolean editInfo = false;
    String oldUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign__up);
        setTitle("Your Information");
        name = findViewById(R.id.fullName);
        phoneNo = findViewById(R.id.phoneNo);
        insta = findViewById(R.id.insta);
        snap = findViewById(R.id.snap);
        linkedin = findViewById(R.id.linkedin);
        github = findViewById(R.id.github);
        fileName = findViewById(R.id.fileName);
        btnSelect = findViewById(R.id.btnSelectFile);
        btnSignUp = findViewById(R.id.btnSignUp);
        Intent intent  = getIntent();
        String phone = intent.getStringExtra("phoneNo");
        String userName, userInsta, userSnap, userLinkedin, userGit = null;
        if(intent.getBooleanExtra("check", false)){
            editInfo = true;
            userName = intent.getStringExtra("name");
            name.setText(userName);
            userInsta = intent.getStringExtra("insta");
            insta.setText(userInsta);
            userSnap = intent.getStringExtra("snap");
            snap.setText(userSnap);
            userLinkedin = intent.getStringExtra("linkedin");
            linkedin.setText(userLinkedin);
            userGit = intent.getStringExtra("git");
            github.setText(userGit);
        }
        //linkedin.setText("het");
        phoneNo.setText(phone);
        phoneNo.setEnabled(false);
        assert phone != null;
        Log.i("Phone No: ", phone);

        // Firebase connection
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
    }


    public void onClickbtnSelect(View v){
        if (ContextCompat.checkSelfPermission(Sign_Up.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            selectPdf();
        } else {
            ActivityCompat.requestPermissions(Sign_Up.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    private void selectPdf() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,INTENT_CODE_SELECTFILE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // check whether user has selected a pdf
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_CODE_SELECTFILE && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData(); // getting the uri of selected file
            assert pdfUri != null;
            String uriString = pdfUri.toString();
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();

            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = this.getContentResolver().query(pdfUri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
            }
            fileName.setText(" Selected File: "+ displayName);
        }
        else{
            Toast.makeText(this,"Please select a file",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            selectPdf();
        }

        else{
            Toast.makeText(this,"Please provide the permissions",Toast.LENGTH_LONG).show();
        }
    }


    //private User myUser;

    public void onclickbtnSignUp(View v){
        if(editInfo){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            Query query = ref.child("User").orderByChild("phonenumber").equalTo(phoneNo.getText().toString());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot user: dataSnapshot.getChildren()) {
                        //oldUri = "Objects.requireNonNull(user.getValue(User.class)).getFiles1();";
                        user.getRef().removeValue();
                        editInfo = false;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("Error: ", "onCancelled", databaseError.toException());
                }
            });
        }
        Uri myFileUri = uploadFile();

        //  addNotification();
        addNotification();
        User myUser = createUser();

        if (pdfUri != null) {
            if(myFileUri != null){
                uploadFiletoDatabase(pdfUri, myUser,myFileUri);
            }
        } else {
            if(myFileUri != null){
                uploadFiletoDatabase(null, myUser, myFileUri); // ONLY uploading the info file
            }
        }
    }

    private void movetoDashboard(User myUser){

        createSharedPref(myUser);
        Intent intent = new Intent(Sign_Up.this,Dashboard.class);
        startActivity(intent);
    }

    private void addNotification() {
        // Builds your notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("John's Android Studio Tutorials")
                .setContentText("A video has just arrived!");

        // Creates the intent needed to show the notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("notification_id", 0);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
        Toast.makeText(this,"Toast in Notification",Toast.LENGTH_LONG).show();
    }


    private void createSharedPref(User myUser) {
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);

        sharedPreferences.edit().putString("name",myUser.getName()).apply();
        sharedPreferences.edit().putString("phone",myUser.getPhonenumber()).apply();
        SharePreHelper.setName(myUser.getPhonenumber());
        sharedPreferences.edit().putString("insta",myUser.getInstagram()).apply();
        sharedPreferences.edit().putString("snap",myUser.getSnapchat()).apply();
        sharedPreferences.edit().putString("github",myUser.getGitHub()).apply();
        sharedPreferences.edit().putString("linkedIn",myUser.getLinkedIn()).apply();

        sharedPreferences.edit().putString("file1",myUser.getFiles1()).apply();
        sharedPreferences.edit().putString("file2",myUser.getFiles2()).apply();
    }

    private User createUser(){

        String username = name.getText().toString();
        String userPhone = phoneNo.getText().toString();
        String userInsta = insta.getText().toString();
        String userSnap = snap.getText().toString();
        String userGit = github.getText().toString();
        String userLinkedIn = linkedin.getText().toString();
        String userFile1 ="";
        String userFile2 = "";
        //ArrayList<User> arrFiles = new ArrayList<>();

        User newUser = new User(username,userInsta,userSnap,userGit,userLinkedIn, userFile1,userFile2,userPhone);
        return newUser;
    }

    private Uri uploadFile() {
        if (ContextCompat.checkSelfPermission(Sign_Up.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            String details = "";
            details += "Name :" + name.getText().toString();
            details += "\nPhone Number :" + phoneNo.getText().toString();
            details += "\nInstagram :" + insta.getText().toString();
            details += "\nSnapchat :" + snap.getText().toString();
            details += "\nLinkedIn :" + linkedin.getText().toString();
            details += "\nGitHub :" + github.getText().toString();

            return createPDF(details);
        } else {
            ActivityCompat.requestPermissions(Sign_Up.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
        return null;
    }

    private Uri createPDF(String text){
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300,600,1).create();
        PdfDocument.Page myPage = document.startPage(pageInfo);

        Paint myPaint = new Paint();

        Log.i("String ", text);

        int x = 10;
        int y = 20;

        for(String line : text.split("\n")){
            myPage.getCanvas().drawText(line,x,y,myPaint);
            y += myPaint.descent() - myPaint.ascent() + 10;
        }


        document.finishPage(myPage);
        String NameofFile = "/"+ name.getText().toString() + "MYAPP.pdf";
        String filePath = Environment.getExternalStorageDirectory().getPath() + NameofFile;
        File myFile = new File(filePath);

        Uri myFileUri = Uri.fromFile(myFile); // Uri of selected file


        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(myFile);
            document.writeTo(fileOutputStream);
            //uploadFiletoDatabase(myFileUri);

        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(Sign_Up.this,"Error in Creating File",Toast.LENGTH_SHORT).show();
        }

        document.close();
        return myFileUri;
    }


    private void uploadFiletoDatabase(final Uri pdfUriFile, final User myUser, final Uri myFileUri){

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading files....");
        progressDialog.setProgress(0);
        progressDialog.show();

        //may be we need a for loop to delete all the files, but wont work
        //so to replace old files with new files, we need to delete the folder or the old files...

        final String phone = phoneNo.getText().toString() + "/";

  final StorageReference desertRef = storage.getReference().child("files/" + phone);

        desertRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                //final int[] count = {0};
                for(StorageReference item : listResult.getItems()){
                    String path = item.toString().substring(item.toString().lastIndexOf('/') + 1);
                    StorageReference desertRef1 = storage.getReference().child("files/" + phone + path);
                    desertRef1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // File deleted successfully
                            Log.i("Success", "onSuccess: deleted file");
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Uh-oh, an error occurred!
                                    Log.i("Failure", "onFailure: did not delete file");
                                }
                            });
                    Log.i("Item: ", path);
                    //Toast.makeText(Sign_Up.this, "Please", Toast.LENGTH_LONG).show();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Fail: ", "item.getName()");
                    }
                });




        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("User");
        final DatabaseReference keyref = databaseReference.push();
        final String key = keyref.getKey();
        keyref.setValue(myUser);


        final StorageReference storageReference2 = storage.getReference().child("files/" + phone + myFileUri.getLastPathSegment().toString());
        UploadTask uploadTask2 = storageReference2.putFile(myFileUri);

        uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"File2 Successfully Uploaded",Toast.LENGTH_SHORT).show();
                storageReference2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        myUser.setFiles2(uri.toString());
                        databaseReference.child(key).child("files2").setValue(uri.toString());
                        if(pdfUriFile == null){
                            movetoDashboard(myUser);
                        }
                        System.out.println(uri.toString());
                    }
                });

            }

        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Sign_Up.this, "File2 not uploaded!!", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        // tacking the progress of our upload
                        int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setProgress(currentProgress);
                    }
                });


        if(pdfUriFile != null) {
            editInfo = false;

            final ProgressDialog progressDialog1 = new ProgressDialog(this);
            progressDialog1.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog1.setTitle("Uploading files....");
            progressDialog1.setProgress(0);
            progressDialog1.show();

            final StorageReference storageReference = storage.getReference().child("files/" + phone + displayName);
            UploadTask uploadTask = storageReference.putFile(pdfUriFile);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   progressDialog1.dismiss();
                   Toast.makeText(getApplicationContext(),"File1 successfully Uploaded",Toast.LENGTH_SHORT).show();
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            myUser.setFiles1(uri.toString());
                            databaseReference.child(key).child("files1").setValue(uri.toString());
                            System.out.println(uri.toString());
                            movetoDashboard(myUser);
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Sign_Up.this, "File1 not uploaded !!", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            // tacking the progress of our upload
                            int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog1.setProgress(currentProgress);
                        }
                    });
        }

        if(pdfUriFile == null && false){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            Query query = ref.child("User").orderByChild("phonenumber").equalTo(phoneNo.getText().toString());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot user: dataSnapshot.getChildren()) {
                        user.getRef().child("files1").setValue(oldUri);
                        editInfo = false;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("Error: ", "onCancelled", databaseError.toException());
                }
            });
        }
    }
}