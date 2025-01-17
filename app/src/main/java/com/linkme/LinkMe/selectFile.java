package com.linkme.LinkMe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class selectFile extends AppCompatActivity {

    private static final int INTENT_CODE_SELECTFILE = 12;
    RadioButton file1,file2,file3,file4,file5;
    RadioGroup rbGroup;
    String myPhoneNumber;
    String send_to;
    String[] allFiles = new String[5];
    Uri pdfUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);
        setTitle("File Selection");
        int notificationId = 0;
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.cancel(notificationId);

        file1 = findViewById(R.id.rb1);
//file1.setText("Hello There");
        file2 = findViewById(R.id.rb2);
        file3 = findViewById(R.id.rb3);
        file4 = findViewById(R.id.rb4);
        file5 = findViewById(R.id.rb5);
        file1.setEnabled(false);
        file2.setEnabled(false);
        file3.setEnabled(false);
        file4.setEnabled(false);
        file5.setEnabled(false);
        final RadioButton[] files = new RadioButton[5];
        files[0] = file1;
        files[1] = file2;
        files[2] = file3;
        files[3] = file4;
        files[4] = file5;
        rbGroup = findViewById(R.id.rbGroup);
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
//Intent intent = getIntent();
        send_to = User.lastestNumber;
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);


        myPhoneNumber = sharedPreferences.getString("phone","");
        StorageReference storageReference = firebaseStorage.getReference().child("files/" + myPhoneNumber);
        storageReference.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        int count = 0;
//int size = listResult.getItems().size();
                        for (StorageReference item : listResult.getItems()) {
                            if(count >= 5){
                                break;
                            }
                            final int finalCount = count;
                            item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    System.out.println("Download Link: " + uri.toString());
                                    allFiles[finalCount] = uri.toString();
                                }
                            });

                            System.out.println("Item: " + item.getName());

                            files[count].setText(item.getName());
                            files[count].setEnabled(true);
                            files[count].setTag(count);
                            files[count].setVisibility(View.VISIBLE);

/*
if(count == 0){
file1.setText(item.getName());
file1.setEnabled(true);
file1.setTag(0);
}else if(count == 1){
file2.setText(item.getName());
file2.setEnabled(true);
file2.setTag(1);
}else if(count == 2){
file3.setText(item.getName());
file3.setEnabled(true);
file3.setTag(2);
}else if(count == 3){
file4.setText(item.getName());
file4.setEnabled(true);
file4.setTag(3);
}else{
file5.setText(item.getName());
file5.setEnabled(true);
file5.setTag(4);
}
*/


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
    public void onSend(View v){
        RadioButton selected = findViewById(rbGroup.getCheckedRadioButtonId());
        if(selected != null && !selected.getText().toString().equals("")){
            String uri = allFiles[Integer.parseInt(selected.getTag().toString())];
            //System.out.println(uri);
            Intent intent = new Intent(this,Conformation.class);
            intent.putExtra("myPhone",myPhoneNumber);
            intent.putExtra("sendTo",send_to);
//intent.putExtra("info","");
            intent.putExtra("uri", uri);
            sendBroadcast(intent);
            finish();
        }
        else{
            Toast.makeText(this,"ERROR",Toast.LENGTH_SHORT).show();
        }
    }

    public void onSelectAndSend(View v){

        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,INTENT_CODE_SELECTFILE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_CODE_SELECTFILE && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData(); // getting the uri of selected file
            assert pdfUri != null;
//String uriString = pdfUri.toString();
            SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);
            uploadFile(pdfUri);
        }
        else{
            Toast.makeText(this,"Please select a file",Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadFile(Uri pdfUriFile){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageReference = storage.getReference().child("Random Files/" + myPhoneNumber + "/"+ getFileName(pdfUriFile));
        UploadTask uploadTask = storageReference.putFile(pdfUriFile);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Intent intent = new Intent(selectFile.this,Conformation.class);
                        intent.putExtra("myPhone",myPhoneNumber);
                        intent.putExtra("sendTo",send_to);
//intent.putExtra("info","");
                        String stringUri = uri.toString();
                        intent.putExtra("uri", stringUri);
                        sendBroadcast(intent);
                    }
                });
                finish();
//Toast.makeText(getApplicationContext(),"File successfully Sent",Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "File not Sent", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                });
    }

    private String getFileName(Uri pdfUriFile){
        String displayName = null;
        String uriString = pdfUriFile.toString();
        File myFile = new File(uriString);
        String path = myFile.getAbsolutePath();

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = this.getContentResolver().query(pdfUriFile, null, null, null, null);
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