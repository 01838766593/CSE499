package com.example.sayem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AboutMe extends AppCompatActivity {
    public static final String TAG = "APP_TAG";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView textViewName;
    private TextView textViewEmail;
    private TextView textViewPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);
        textViewName = findViewById(R.id.name);
        textViewEmail = findViewById(R.id.email);
        textViewPhone = findViewById(R.id.phone);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final DocumentReference docRef = db.collection("users").document(App.getInstance().getLoggedInUserPhone());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        textViewName.setText(document.getString("name"));
                        textViewEmail.setText(document.getString("email"));
                        textViewPhone.setText(document.getString("phone"));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}