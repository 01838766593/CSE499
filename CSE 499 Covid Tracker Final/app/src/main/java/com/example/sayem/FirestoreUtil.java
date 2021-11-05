package com.example.sayem;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Created by Sayem Mahmud on 11/24/2020.
 * Email : context.imran@gmail.com
 */
public class FirestoreUtil {

    private static FirestoreUtil instance;
    private int count = 0;

    private static final String TAG = "FirestoreUtil";

    public static FirestoreUtil getInstance(){
        if (instance == null){
            instance = new FirestoreUtil();
        }
        return instance;
    }

    public FirebaseFirestore getDocumentRef(){
        return FirebaseFirestore.getInstance();
    }

    public FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }
}
