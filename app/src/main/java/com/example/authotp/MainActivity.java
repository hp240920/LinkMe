package com.example.authotp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    EditText phone, otpEnter;
    Button next;
    FirebaseAuth fAuth;
    String otpCode = null;
    String verificationId;
    CountryCodePicker countryCodePicker;
    PhoneAuthCredential credential;
    Boolean verificationOnProgress = false;
    PhoneAuthProvider.ForceResendingToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phone = findViewById(R.id.getNumber);
        next = findViewById(R.id.nextOTP);
        otpEnter = findViewById(R.id.otpEnter);
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
                        next.setText("Verify");
                        next.setEnabled(true);
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
                    }
                });
    }

    private void verifyIt(PhoneAuthCredential cred) {
        fAuth.signInWithCredential(cred).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Phone Verified." +
                            fAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "Can not Verify " +
                            "phone and Create Account.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
