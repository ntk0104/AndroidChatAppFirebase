package com.example.kietnguyen.mychatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {

    TextView displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String selected_userid = getIntent().getStringExtra("selected_user_id");

        displayName = findViewById(R.id.profile_displayname);
        displayName.setText(selected_userid);
    }
}
