package com.example.sayem;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken;
import static com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks;
import static com.google.firebase.auth.PhoneAuthProvider.getCredential;

public class VerifyPhoneNo extends AppCompatActivity {
    public static final String TAG = "APP_TAG";
    String verificationCodeBySystem;
    Button verify_btn;
    EditText phoneNoEntryByTheUser;
    ProgressBar progressBar;
    private String mEmail;
    private String mName;
    private String mPhone;
    private double mLat;
    private double mLon;
    private FusedLocationProviderClient fusedLocationClient;
    private String APP_TAG = "VFN";
    Map<String, Object> user = new HashMap<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // new FirebaseFireStore()


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_no);
        verify_btn = findViewById(R.id.verify_btn);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        phoneNoEntryByTheUser = findViewById(R.id.verification_code_entered_by_user);
        Log.d(APP_TAG, " startIntent has no extra value ");
        Intent startIntent = getIntent();

        if (startIntent.hasExtra("phone")) {
            mEmail = startIntent.getStringExtra("email");

            Log.d("VerifyPhone", "email number is  " + mEmail);

            mName = startIntent.getStringExtra("name");

            Log.d("VerifyPhone", "Password is " + mName);

            mPhone = startIntent.getStringExtra("phone");

            Log.d("VerifyPhone", "Phone number is " + mPhone);
        } else {
            Log.d("VerifyPhone", " startIntent has no extra value ");
        }


        if(mPhone != null){
            user.put("name", mName);
            user.put("email", mEmail);
            user.put("phone", mPhone);
            sendVerificationCodeToUser(mPhone);
        } else{
            Log.d("VerifyPhone"," Phone number is null " + mPhone);
        }
        verify_btn.setOnClickListener(new View.OnClickListener(){
            public  void onClick(View view){
                String code= phoneNoEntryByTheUser.getText().toString();
                if (code.isEmpty()|| code.length()<6){
                    phoneNoEntryByTheUser.setError("Wrong OTP...");
                    phoneNoEntryByTheUser.requestFocus();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d(APP_TAG, String.format("%f", location.getLatitude()));
                            Log.d(APP_TAG, String.format("%f", location.getLongitude()));
                            mLat = location.getLatitude();
                            mLon = location.getLongitude();
                        }
                    }
                });
         }


    private void sendVerificationCodeToUser(String _phoneNo) {
        Log.i("TAG", "Phone number: " + _phoneNo);
       PhoneAuthProvider.getInstance().verifyPhoneNumber(
                _phoneNo,        // Phone number to verify
                 60,                 // Timeout duration
                  TimeUnit.SECONDS,   // Unit of timeout
                this,   // Activity (for callback binding)
                 mCallbacks);        // OnVerificationStateChangedCallbacks

        //App.getInstance().setLoggedInUserPhone(mPhone); ///passing at app.java setLoggedIn UserPhone

        //Log.d(TAG, "Writing to shared preference: " + mPhone);


       // Intent intent= new Intent(getApplicationContext(),UserDashboard.class);
       // startActivity(intent);
    }
    private OnVerificationStateChangedCallbacks mCallbacks = new OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCodeBySystem= s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
        String code= phoneAuthCredential.getSmsCode();
        if(code!=null) {
            Log.d(TAG, "########## onVerificationCompleted #######");
            progressBar.setVisibility(View.VISIBLE);
            verifyCode(code);
        }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Log.d(TAG, "########## onVerificationFailed #######");
            Toast.makeText(VerifyPhoneNo.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };
    private void verifyCode(String code) {
        Log.d(TAG, "########## Verify Code #######");
        PhoneAuthCredential credential = getCredential(verificationCodeBySystem, code);
        signInTheUserByCredentials(credential);


    }

    private void signInTheUserByCredentials(PhoneAuthCredential credential){
            FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(VerifyPhoneNo.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                App.getInstance().setLoggedInUserPhone(mPhone); ///passing at app.java setLoggedIn UserPhone
                                SharedPreferences sharedPref = getSharedPreferences("USER_PREFERENCE", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit(); //creating object of shared pref editor

                                editor.putString("LOGGED_IN_USER_PHONE", mPhone);
                                editor.apply();
                                // firebase data savings
                                user.put("lat", mLat);
                                user.put("lon", mLon);
                                Log.d(TAG, user.toString());
                                db.collection("users").document(mPhone).set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener(){
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error writing document", e);
                                            }
                                        });
                                Log.d(TAG, "Writing to shared preference: " + mPhone);
                                Intent intent= new Intent(VerifyPhoneNo.this,UserDashboard.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(VerifyPhoneNo.this, task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
