package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

public class deleteFiles extends AppCompatActivity {


    TextView phone_folder;
    String folder_name = null;
    FirebaseStorage firebaseStorage;
    ArrayList<String> checkBoxId = new ArrayList<>();
    CheckBox file1, file2, file3, file4, file5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_files);
        phone_folder = findViewById(R.id.folder_name);
        Intent intent = getIntent();
        folder_name = intent.getStringExtra("phone");
        phone_folder.setText(folder_name);
        file1 = findViewById(R.id.sendFile1);
        //file1.setText("Hello There");
        file2 = findViewById(R.id.file2);
        file3 = findViewById(R.id.file3);
        file4 = findViewById(R.id.file4);
        file5 = findViewById(R.id.file5);
        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("files/" + folder_name);
        storageReference.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        int count = 0;
                        for (StorageReference item : listResult.getItems()) {
                            if(count >= 5){
                                break;
                            }
                            System.out.println("Item: " + item.getName());
                            if(count == 0){
                                file1.setText(item.getName());
                            }else if(count == 1){
                                file2.setText(item.getName());
                            }else if(count == 2){
                                file3.setText(item.getName());
                            }else if(count == 3){
                                file4.setText(item.getName());
                            }else{
                                file5.setText(item.getName());
                            }
                            count++;
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                    }
                });
    }

    public void onDelete(View v){
        if(file1.isChecked() && !file1.getText().toString().isEmpty()){
            checkBoxId.add(file1.getText().toString());
        }
        if(file2.isChecked() && !file2.getText().toString().isEmpty()){
            checkBoxId.add(file2.getText().toString());
        }
        if(file3.isChecked() && !file3.getText().toString().isEmpty()){
            checkBoxId.add(file3.getText().toString());
        }
        if(file4.isChecked() && !file4.getText().toString().isEmpty()){
            checkBoxId.add(file4.getText().toString());
        }
        if(file5.isChecked() && !file5.getText().toString().isEmpty()){
            checkBoxId.add(file5.getText().toString());
        }
        for(int i = 0; i < checkBoxId.size(); i++){
            final int count = i;
            StorageReference storageReference = firebaseStorage.getReference().child("files/" + folder_name);
            storageReference.listAll()
                    .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            //int count = 0;
                            for (StorageReference item : listResult.getItems()) {
                                if(item.getName().equals(checkBoxId.get(count))){
                                    item.delete();
                                    Toast.makeText(getApplicationContext(), "Deleted!", Toast.LENGTH_SHORT).show();
                                }
                            }
                            finish();
                            startActivity(getIntent());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Uh-oh, an error occurred!
                        }
                    });
        }
        //checkBoxId.clear();

    }

    private static final int INTENT_CODE_SELECTFILE = 10;
    private Uri pdfUri;

    public void onUpload(View v){

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
            assert pdfUri != null;
            //String uriString = pdfUri.toString();
            SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
            User currentUser = getSharedPref(sharedPreferences);
            uploadFile(pdfUri,currentUser);
        }
        else{
            Toast.makeText(this,"Please select a file",Toast.LENGTH_SHORT).show();
        }
    }

    private User getSharedPref(SharedPreferences sharedPreferences){
        User currentUser = new User();
        currentUser.setName(sharedPreferences.getString("name",""));
        currentUser.setPhonenumber(sharedPreferences.getString("phone",""));
        currentUser.setEmail(sharedPreferences.getString("email", ""));
        currentUser.setWebsite(sharedPreferences.getString("website", ""));
        currentUser.setInstagram(sharedPreferences.getString("insta",""));
        currentUser.setSnapchat(sharedPreferences.getString("snap",""));
        currentUser.setGitHub(sharedPreferences.getString("github",""));
        currentUser.setLinkedIn(sharedPreferences.getString("linkedIn",""));
       // currentUser.setFiles1(sharedPreferences.getString("file1",""));
        //currentUser.setFiles2(sharedPreferences.getString("file2",""));
        return currentUser;
    }

    private void uploadFile(final Uri pdfUriFile, final User myUser){

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading files....");
        progressDialog.setProgress(0);
        progressDialog.show();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageReference = storage.getReference().child("files/" + myUser.getPhonenumber() + "/"+ getFileName());
        UploadTask uploadTask = storageReference.putFile(pdfUriFile);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"File successfully Uploaded",Toast.LENGTH_SHORT).show();
                // Restart the activity
                finish();
                startActivity(getIntent());
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "File not uploaded !!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
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

    private String getFileName(){
        String displayName = null;
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
        return displayName;
    }
}
