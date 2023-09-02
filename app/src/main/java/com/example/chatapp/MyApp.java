package com.example.chatapp;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MyApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("logged_in_user_cred").child(firebaseUser.getUid());
            Map<String, Object> data = new HashMap<>();
            data.put("is_online", true);
            databaseReference.updateChildren(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()) {
                        Log.e("Log: ", "onComplete: ", task.getException());
                        Toast.makeText(MyApp.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    String token = task.getResult();
                    Map<String, Object> tokenData = new HashMap<>();
                    tokenData.put("fcm_token", token);
                    databaseReference.updateChildren(tokenData);
                }
            });

        }

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("logged_in_user_cred").child(firebaseUser.getUid());
            Map<String, Object> data = new HashMap<>();
            data.put("is_online", false);
            databaseReference.updateChildren(data);

        }
    }
}
