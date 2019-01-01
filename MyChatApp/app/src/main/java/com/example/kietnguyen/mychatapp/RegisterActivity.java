package com.example.kietnguyen.mychatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtDisplayName;
    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnCreate;

    private ProgressDialog mRegProgress;

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        edtDisplayName = findViewById(R.id.edt_display_name);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnCreate = findViewById(R.id.btnCreateNewAccount);

        mRegProgress = new ProgressDialog(this);


        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
//        set title for toolbar
        getSupportActionBar().setTitle("Create Account");
//        set icon next to title on toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayname = edtDisplayName.getText().toString();
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();
                if(!TextUtils.isEmpty(displayname) || !TextUtils.isEmpty(email) || ! TextUtils.isEmpty(password)){
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait a minute");
//                    disable cancle when click outside
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    registerUser(displayname, email, password);
                }


            }
        });
    }

    private void registerUser(String displayname, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mRegProgress.dismiss();
                    Intent mainintent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(mainintent);
                    finish();
                }else {
                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this, "Occur Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
