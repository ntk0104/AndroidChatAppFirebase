package com.example.kietnguyen.mychatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatusInputLayout;
    private Button mBtnSaveStatus;
//    declare DatabaseReference to access Firebase database
    private DatabaseReference mDatabase;
//    declare FirebaseUser to get information of current user
    private FirebaseUser mCurrentUser;
//    declare progressdialog
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = findViewById(R.id.status_appbar);
        setSupportActionBar(mToolbar);
//        title of toolbar
        getSupportActionBar().setTitle("Change Status");
//        add button back in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        get current user id
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();
//        link to node which need to be update
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mStatusInputLayout = findViewById(R.id.status_inputStatus_field);
        mBtnSaveStatus = findViewById(R.id.status_saveStatus_btn);

        mBtnSaveStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog = new ProgressDialog(StatusActivity.this);
                mProgressDialog.setTitle("Changing Status");
                mProgressDialog.setMessage("Wait a minute");
                mProgressDialog.show();
                String status = mStatusInputLayout.getEditText().getText().toString();
//                update new status for current user
                mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgressDialog.dismiss();
                            Intent settingIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                            startActivity(settingIntent);
                            finish();
                        }else{
                            mProgressDialog.hide();
                        }
                    }
                });
            }
        });

    }
}
