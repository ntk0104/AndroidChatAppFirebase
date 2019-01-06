package com.example.kietnguyen.mychatapp;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ImageView userAvatar;
    private TextView displayName;
    private TextView status;
    private TextView friends_info;
    private Button btnSendFriendRequest;
    private String friend_current_status;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

//        the userID got from Usersactivity
        final String selected_userid = getIntent().getStringExtra("selected_user_id");

        userAvatar = findViewById(R.id.profile_avatar);
        displayName = findViewById(R.id.profile_displayname);
        status = findViewById(R.id.profile_status);
        friends_info = findViewById(R.id.profile_friends_info);
        btnSendFriendRequest = findViewById(R.id.profile_btnSendFriendRequest);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading Profile");
        mProgressDialog.setMessage("Wait a minute");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        friend_current_status = "not_friends";

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(selected_userid);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String user_status = dataSnapshot.child("status").getValue().toString();
                String user_avatar = dataSnapshot.child("image").getValue().toString();

                displayName.setText(display_name);
                status.setText(user_status);
                if(!user_avatar.equals("default")){
                    Picasso.get().load(user_avatar).placeholder(R.drawable.defaultavatar).into(userAvatar);
                    mProgressDialog.dismiss();
                }else {
                    mProgressDialog.hide();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        btnSendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(friend_current_status.equals("not_friends")){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(selected_userid).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mFriendRequestDatabase.child(selected_userid).child(mCurrentUser.getUid()).child("request_type").setValue("received");
                                        Toast.makeText(ProfileActivity.this, "Sent request successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            });


                }

            }
        });


    }
}
