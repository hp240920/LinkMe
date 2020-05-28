package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
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

public class Sign_Up extends AppCompatActivity {

    EditText name, phoneNo, insta, snap, linkedin, github;
    TextView fileName;
    Button btnSelect , btnSignUp;
    final int REQUEST_CODE = 9;
    final int INTENT_CODE_SELECTFILE = 86;
    private Uri pdfUri;
    FirebaseDatabase database;
    FirebaseStorage storage;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // check whether user has selected a pdf
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_CODE_SELECTFILE && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData(); // getting the uri of selected file
            fileName.setText(" Selected File: "+ data.getData().getLastPathSegment());
        }
        else{
            Toast.makeText(this,"Please select a file",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == REQUEST_CODE && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            selectPdf();
        }

        else{
            Toast.makeText(this,"Please provide the permissions",Toast.LENGTH_LONG).show();
        }
    }


    public void onclickbtnSignUp(View v){


            Uri myFileUri = uploadFile();

            User myUser = createUser();
            if (pdfUri != null) {
                // if you have selected a file to upload
                 // uploading 2 files .... 1 with info and other is the selected file
                if(myFileUri != null){

                    uploadFiletoDatabase(pdfUri,myUser,myFileUri);
                }
            } else {
                // if you have not selected a file to upload
                if(myFileUri != null){

                    uploadFiletoDatabase(null,myUser,myFileUri); // ONLY uploading the info file
                }

            }
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
        ArrayList<User> arrFiles = new ArrayList<>();

        User newUser = new User(username,userInsta,userSnap,userGit,userLinkedIn, userFile1,userFile2,arrFiles,userPhone);
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

        final String phone = phoneNo.getText().toString() + "/";
            StorageReference storageReference = storage.getReference().child("files/"+ phone + pdfUriFile.getLastPathSegment().toString());
            UploadTask uploadTask = storageReference.putFile(pdfUriFile);

            StorageReference storageReference2 = storage.getReference().child("files/"+ phone + myFileUri.getLastPathSegment().toString());
            UploadTask uploadTask2 = storageReference2.putFile(myFileUri);

        if (myFileUri != null) {
            uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    String url = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString(); // return a url of your uploaded file

                                                    HashMap<String, User> hashMap = new HashMap<>();
                                                    hashMap.put(myUser.getPhonenumber(), myUser);

                                                    database.getReference().child("User").setValue(hashMap);

                                                    DatabaseReference dbref = database.getReference("User").child(myUser.getPhonenumber()); // returns the path to root

                                                    if (pdfUriFile == null) {
                                                        dbref.child("files1").setValue(url);
                                                    }

                                                    dbref.child("files").setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {

                                                        // dbref.child("files/" + phone + pdfUriFile.getLastPathSegment().toString()).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(Sign_Up.this, "file Successfully uploaded", Toast.LENGTH_SHORT).show();
                                                                progressDialog.dismiss();
                                                                // Sign Up Complete

                                                            } else {
                                                                Toast.makeText(Sign_Up.this, "file not uploaded !!", Toast.LENGTH_SHORT).show();
                                                            }

                                                        }
                                                    });
                                                }

                                            }
            )
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Sign_Up.this, "file not uploaded !!", Toast.LENGTH_SHORT).show();
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

        }

    }

}
