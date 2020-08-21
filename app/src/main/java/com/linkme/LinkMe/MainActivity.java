package com.linkme.LinkMe;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 3055;
    EditText phone;
    EditText otpEnter;
    Button submit;
    FirebaseAuth fAuth;
    String otpCode = null;
    String verificationId;
    CountryCodePicker countryCodePicker;
    PhoneAuthCredential credential = null;
    boolean verificationOnProgress = false;

    PhoneAuthProvider.ForceResendingToken token;
    ScrollView layoutMain;
    
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CONTACTS) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (true) {
                AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
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
            }else{
                requestPermissions(new String[] {
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }else{
            Toast.makeText(getApplicationContext(), "Permission already granted!", Toast.LENGTH_SHORT).show();
        }
    }




      @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
                //System.out.println(grantResults[0]  + grantResults[2] + grantResults[3] + grantResults[4] + grantResults[5] + grantResults[6]);
                if ((grantResults.length > 0) &&
                        (grantResults[0] + grantResults[1]+ grantResults[2] + grantResults[3] + grantResults[4] + grantResults[5] ) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                }  else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int notificationId = intent.getIntExtra("notification_id", 0);
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);

        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);


        if(sharedPreferences.contains("phone")){
            Intent intent1 = new Intent(this, Dashboard.class);
            startActivity(intent1);
        }


        setContentView(R.layout.activity_main);
        setTitle("Log In/Sign Up");
        phone = findViewById(R.id.getNumber);
        submit = findViewById(R.id.submit);
        //login = findViewById(R.id.logIn);
        otpEnter = findViewById(R.id.otpEnter);
        otpEnter.setEnabled(false);
        countryCodePicker = findViewById(R.id.ccp);
        fAuth = FirebaseAuth.getInstance();
        layoutMain = findViewById(R.id.layoutMain);

        layoutMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.layoutMain){
                    InputMethodManager inputKeyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    assert inputKeyboard != null;
                    inputKeyboard.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!phone.getText().toString().isEmpty() && phone.getText().toString().length() == 10) {
                    if(!verificationOnProgress){
                        submit.setEnabled(false);
                        otpEnter.setEnabled(true);
                        otpEnter.requestFocus();
                        String phoneNum1 = "+" + countryCodePicker.getSelectedCountryCode()
                                + phone.getText().toString();
                        Log.i("phone", "Phone No.: " + phoneNum1);
                        requestPhoneAuth(phoneNum1);
                    }else {
                        otpCode = otpEnter.getText().toString();
                        if(otpCode.isEmpty()){
                            otpEnter.setError("Required");
                            return;
                        }
                        credential = PhoneAuthProvider.getCredential(verificationId, otpCode);
                        verifyIt(credential);
                    }

                }else {
                    phone.setError("Valid Phone Required");
                }
            }
        });
    }

    boolean checkForPhoneNumber(String number){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        final boolean[] check = {false};
        ref.orderByChild("phonenumber").equalTo(number).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot datas: dataSnapshot.getChildren()){
                    String number = datas.child("phonenumber").getValue().toString();
                    Toast.makeText(getApplicationContext(), number, Toast.LENGTH_SHORT).show();
                }
                if(dataSnapshot.exists()) {
                    check[0] = true;
                }else {
                    check[0] = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return check[0];
    }


    private void requestPhoneAuth(String phoneNum) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNum, 60L, TimeUnit.SECONDS,
                this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                        Toast.makeText(MainActivity.this, "Auto-Verification Failed!", Toast.LENGTH_SHORT).show();
                        MainActivity.this.recreate();
                    }

                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationId = s;
                        token = forceResendingToken;
                        verificationOnProgress = true;
                        submit.setEnabled(true);
                        otpEnter.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            verifyIt(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    private void verifyIt(PhoneAuthCredential cred) {
        fAuth.signInWithCredential(cred).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = task.getResult().getUser();
                    long creationTimestamp = user.getMetadata().getCreationTimestamp();
                    long lastSignInTimestamp = user.getMetadata().getLastSignInTimestamp();
                    if (creationTimestamp == lastSignInTimestamp) {
                        Toast.makeText(MainActivity.this, "Phone Verified." +
                                Objects.requireNonNull(fAuth.getCurrentUser()).getPhoneNumber(), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this, Sign_Up.class);
                        intent.putExtra("check", false);
                        intent.putExtra("phoneNo", fAuth.getCurrentUser().getPhoneNumber());
                        SharePreHelper.setName(fAuth.getCurrentUser().getPhoneNumber());
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Your phone already exists.", Toast.LENGTH_SHORT).show();
                        final Intent intent = new Intent(MainActivity.this, Dashboard.class);

                        // give your the user with that phone number

                        FirebaseQuerry.getData(new FirebaseQuerry.FirestoreCallback() {
                            @Override
                            public void OncallBack(User currentUser) {
                                createSharedPref(currentUser);
                                startActivity(intent);
                            }

                            @Override
                            public void OncallBackKey(String key) {

                            }
                        }, fAuth.getCurrentUser().getPhoneNumber());


                    }
                }else {
                    Toast.makeText(MainActivity.this, "Cannot Verify " +
                            "Phone and Create Account.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
    private void createSharedPref(User myUser) {
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.authotp", Context.MODE_PRIVATE);

        sharedPreferences.edit().putString("name",myUser.getName()).apply();
        sharedPreferences.edit().putString("phone",myUser.getPhonenumber()).apply();
      //  SharePreHelper.setName(myUser.getPhonenumber());
        sharedPreferences.edit().putString("email",myUser.getEmail()).apply();
        sharedPreferences.edit().putString("website",myUser.getWebsite()).apply();
        sharedPreferences.edit().putString("insta",myUser.getInstagram()).apply();
        sharedPreferences.edit().putString("snap",myUser.getSnapchat()).apply();
        sharedPreferences.edit().putString("github",myUser.getGitHub()).apply();
        sharedPreferences.edit().putString("linkedIn",myUser.getLinkedIn()).apply();

       // sharedPreferences.edit().putString("file1",myUser.getFiles1()).apply();
       //  sharedPreferences.edit().putString("file2",myUser.getFiles2()).apply();
    }
}
