package com.example.sayem;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;

public class SignUpActivity extends AppCompatActivity {
     EditText emailEditText, nameEditText, mobileEditText;
     Button btnSignUp;
     CountryCodePicker countryCodePicker;
     FirebaseAuth mFirebaseAuth;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mFirebaseAuth= FirebaseAuth.getInstance();
        emailEditText=findViewById(R.id.editTextTextEmailAddress);
        nameEditText=findViewById(R.id.editTextTextPassword);
        btnSignUp=findViewById(R.id.button);
        countryCodePicker=findViewById(R.id.country_code_picker);
        mobileEditText=findViewById(R.id.editTextPhone);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String name = nameEditText.getText().toString();
                String mob = mobileEditText.getText().toString();
                String _phoneNo="+" + countryCodePicker.getSelectedCountryCode()+mob;
                String emailPattern= "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                Context context = SignUpActivity.this;
                 if (email.isEmpty() && name.isEmpty()){
                    Toast.makeText(SignUpActivity.this,"Both fields are empty", Toast.LENGTH_SHORT ).show();
                }
                 else if (email.isEmpty()) {
                     Toast.makeText(SignUpActivity.this," Email are empty", Toast.LENGTH_SHORT ).show();
                 }
                 else if(!email.matches(emailPattern)){
                     Toast.makeText(SignUpActivity.this," Invalid Email address", Toast.LENGTH_SHORT ).show();
                 }

                else if (name.isEmpty()) {
                    Toast.makeText(SignUpActivity.this," nameEditText are empty", Toast.LENGTH_SHORT ).show();
                }
                 else if (name.length()<6) {
                     Toast.makeText(SignUpActivity.this," nameEditText length at least 6 needed!  ", Toast.LENGTH_SHORT ).show();
                 }
                else if(mob.isEmpty()){
                     Toast.makeText(SignUpActivity.this,"Mobile number is empty", Toast.LENGTH_SHORT ).show();
                 }

                 else if (!email.isEmpty() && (!name.isEmpty()) &&(!mob.isEmpty()) ){

                     Log.d("VerifyPhone"," phone , email , passworid " + email + name + mob);
                     Intent intent=new Intent(context, VerifyPhoneNo.class);
                     intent.putExtra("phone", _phoneNo);
                     intent.putExtra("email",email );
                     intent.putExtra("name", name );
                     startActivity(intent);
                 }
                 else{
                     Toast.makeText(SignUpActivity.this," Error Occurred", Toast.LENGTH_SHORT ).show();
                 }
            }
                                     }
        );


    }
}