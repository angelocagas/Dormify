package com.example.kotlindormify;

import android.app.Application;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Clear Firestore cache
        FirebaseFirestore.getInstance().clearPersistence()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Successfully cleared cache
                        } else {
                            // Handle the error
                        }
                    }
                });
    }
}
