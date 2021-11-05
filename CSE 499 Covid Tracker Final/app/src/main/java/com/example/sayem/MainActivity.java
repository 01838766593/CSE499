package com.example.sayem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN = 5000;
    Animation topAnim, bottomAnim;
    ImageView Image;
    TextView slogan;
    private static final String TAG = "APP_TAG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        Image = findViewById(R.id.imageView);
        slogan = findViewById(R.id.b);

        Image.setAnimation(topAnim);
        slogan.setAnimation(bottomAnim);

        /* Shared Preference */
        SharedPreferences sharedPref = getSharedPreferences("USER_PREFERENCE", Context.MODE_PRIVATE);
        String phoneNo = sharedPref.getString("LOGGED_IN_USER_PHONE", null);
        if (phoneNo != null){
            Log.d(TAG, "##### Phone: " + phoneNo);
            App.getInstance().setLoggedInUserPhone(phoneNo);
            Intent intent= new Intent(getApplicationContext(),UserDashboard.class);
            startActivity(intent);
        }else {
            Log.d(TAG, "##### Value not found. Phone is null ########");
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                    startActivity(intent);
                    finish();

                }

            }, SPLASH_SCREEN);
        }
    }
}
