package com.example.kietnguyen.mychatapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/*This class for purposing offline capability*/

public class LapitChatApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
