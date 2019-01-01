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

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private  EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;

    private ProgressDialog mLoginProgress;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
//        set title for toolbar
        getSupportActionBar().setTitle("Login Page");
//        set button next to title of toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtEmail = findViewById(R.id.edt_email_login);
        edtPassword = findViewById(R.id.edt_password_login);
        btnLogin = findViewById(R.id.login_btn);
        mLoginProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input_email = edtEmail.getText().toString();
                String input_password = edtPassword.getText().toString();

                if(!TextUtils.isEmpty(input_email) || !TextUtils.isEmpty(input_password)){
                    mLoginProgress.setTitle("Loging in");
                    mLoginProgress.setMessage("Wait a minute");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                    loginUser(input_email, input_password);
                }
            }
        });
    }

    private void loginUser(String input_email, String input_password) {
        mAuth.signInWithEmailAndPassword(input_email, input_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mLoginProgress.dismiss();
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
//                    fix err when log in and navigated to MainActivity and press back btn won't go  to the login page again
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }else{
                    mLoginProgress.hide();
                    Toast.makeText(LoginActivity.this, "Got some errors", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
