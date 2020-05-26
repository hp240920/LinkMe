package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    EditText phone, otpEnter;
    Button next, login;
    FirebaseAuth fAuth;
    String otpCode = null;
    String verificationId;
    CountryCodePicker countryCodePicker;
    PhoneAuthCredential credential = null;
    Boolean verificationOnProgress = false;
    PhoneAuthProvider.ForceResendingToken token;
    TextView otpTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phone = findViewById(R.id.getNumber);
        next = findViewById(R.id.signUp);
        login = findViewById(R.id.logIn);
        otpEnter = findViewById(R.id.otpEnter);
        otpTV = findViewById(R.id.otpTextView);
        countryCodePicker = findViewById(R.id.ccp);
        fAuth = FirebaseAuth.getInstance();

        next.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!phone.getText().toString().isEmpty() && phone.getText().toString().length() == 10) {
                    if(!verificationOnProgress){
                        next.setEnabled(false);
                        String phoneNum = "+" + countryCodePicker.getSelectedCountryCode()
                                + phone.getText().toString();
                        Log.i("phone", "Phone No.: " + phoneNum);
                        requestPhoneAuth(phoneNum);
                    }else {
                        next.setEnabled(false);
                        otpEnter.setVisibility(View.GONE);
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

        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!phone.getText().toString().isEmpty() && phone.getText().toString().length() == 10) {
                    if(!verificationOnProgress){
                        login.setEnabled(false);
                        String phoneNum = "+" + countryCodePicker.getSelectedCountryCode()
                                + phone.getText().toString();
                        Log.i("phone", "Phone No.: " + phoneNum);
                        requestPhoneAuth(phoneNum);
                    }else {
                        login.setEnabled(false);
                        otpEnter.setVisibility(View.GONE);
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


    private void requestPhoneAuth(String phoneNum) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNum, 60L, TimeUnit.SECONDS,
                this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                        Toast.makeText(MainActivity.this, "OTP Timeout, " +
                                "Please Re-generate the OTP Again.", Toast.LENGTH_SHORT).show();
                        MainActivity.this.recreate();
                    }

                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationId = s;
                        token = forceResendingToken;
                        verificationOnProgress = true;
                        next.setText("Sign Up");
                        next.setEnabled(true);
                        otpEnter.setVisibility(View.VISIBLE);
                        otpTV.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            verifyIt(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
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
                        intent.putExtra("phoneNo", fAuth.getCurrentUser().getPhoneNumber());
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Your phone already exists.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, Dashboard.class);
                        intent.putExtra("phoneNo", fAuth.getCurrentUser().getPhoneNumber());
                        startActivity(intent);
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Cannot Verify " +
                            "Phone and Create Account.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
