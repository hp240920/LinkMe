package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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


    private static final int REQUEST_CODE = 92;
    TextView phone_folder;
    String folder_name = null;
    FirebaseStorage firebaseStorage;
    ArrayList<String> checkBoxId = new ArrayList<>();
    CheckBox file1, file2, file3, file4, file5;
    private int number_of_files = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_files);
        setTitle("Manage File(s)");
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
        updateFileView();
    }


    private void updateFileView(){
        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("files/" + folder_name);
        storageReference.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        file1.setText("");
                        file2.setText("");
                        file3.setText("");
                        file4.setText("");
                        file5.setText("");
                        file1.setEnabled(false);
                        file2.setEnabled(false);
                        file3.setEnabled(false);
                        file4.setEnabled(false);
                        file5.setEnabled(false);
                        number_of_files = 0;
                        for (StorageReference item : listResult.getItems()) {
                            if(number_of_files >= 5){
                                break;
                            }
                            System.out.println("Item: " + item.getName());
                            if(number_of_files == 0){
                                file1.setText(item.getName());
                                file1.setEnabled(true);
                            }else if(number_of_files == 1){
                                file2.setText(item.getName());
                                file2.setEnabled(true);
                            }else if(number_of_files == 2){
                                file3.setText(item.getName());
                                file3.setEnabled(true);
                            }else if(number_of_files == 3){
                                file4.setText(item.getName());
                                file4.setEnabled(true);
                            }else{
                                file5.setText(item.getName());
                                file5.setEnabled(true);
                            }
                            number_of_files++;
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
                                    item.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            uncheckAll();
                                            Toast.makeText(getApplicationContext(), "Deleted!", Toast.LENGTH_SHORT).show();
                                            updateFileView();
                                        }
                                    });

                                }
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
        //checkBoxId.clear();

    }

    private void uncheckAll() {
        file1.setChecked(false);
        file2.setChecked(false);
        file3.setChecked(false);
        file4.setChecked(false);
        file5.setChecked(false);
    }

    private static final int INTENT_CODE_SELECTFILE = 10;
    private Uri pdfUri;

    public void onUpload(View v){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // If Permission not given
            if (Permissions.check_storage_permission(deleteFiles.this) == false) {
                get_storage_permission();
            }
            else{
                // If permission given
                selectFile();
            }
        }


    }

    private void selectFile(){
        if(number_of_files < 5){
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,INTENT_CODE_SELECTFILE);
        }
        else {
            Toast.makeText(this,"No Empty slot Available",Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void get_storage_permission(){
        requestPermissions(new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
            if(grantResults.length > 1 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)){
                selectFile();
            }
        }
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
                updateFileView();
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
