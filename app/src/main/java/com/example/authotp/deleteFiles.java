package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

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
        file1 = findViewById(R.id.file1);
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
       Intent intent = new Intent(this, Dashboard.class);
       startActivity(intent);
    }
}
