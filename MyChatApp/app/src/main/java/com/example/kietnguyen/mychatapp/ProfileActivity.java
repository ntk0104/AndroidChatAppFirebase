package com.example.kietnguyen.mychatapp;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.internal.Util;

public class ProfileActivity extends AppCompatActivity {

    private ImageView userAvatar;
    private TextView displayName;
    private TextView status;
    private TextView friends_info; /*textview display number of other friends and total mutual friends*/
    private Button btnSendFriendRequest;
    private Button btnCancelRequest;
    private Button btnUnfriend;
    private Button btnDeclineRequest;
    private Button btnAcceptRequest;
    private String friend_current_status;

    private FirebaseUser mCurrentUser; /*used to get current user logged info*/
    private DatabaseReference mSelectedUser; /*used to read-write data to realtime database FOR SELECTED USERS*/
    private DatabaseReference mFriendRequestDatabase; /*used to read-write data to realtime database FOR FRIEND_REQST*/
    private DatabaseReference mFriendListDatabase; /*used to read-write data to Friend_List*/
    private ProgressDialog mProgressDialog; /*used to display progressDialog while waiting task completion*/

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
        btnCancelRequest = findViewById(R.id.profile_btnCancelRequest);
        btnUnfriend = findViewById(R.id.profile_btnUnfriend);
        btnAcceptRequest = findViewById(R.id.profile_btnAcceptRequest);
        btnDeclineRequest = findViewById(R.id.profile_btnDeclineRequest);
        /*database for edit user request*/
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        mFriendListDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_List");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser(); /*use mSelectedUser to get info relate with current user logged in*/
        mSelectedUser = FirebaseDatabase.getInstance().getReference().child("Users").child(selected_userid); /*use to read/ write data to realtime database for USERs group*/


        /*Init visibility: false and turn off clickable of button */
        btnSendFriendRequest.setEnabled(false);
        btnSendFriendRequest.setVisibility(View.INVISIBLE);

        btnCancelRequest.setEnabled(false);
        btnCancelRequest.setVisibility(View.INVISIBLE);

        btnUnfriend.setEnabled(false);
        btnUnfriend.setVisibility(View.INVISIBLE);

        btnAcceptRequest.setEnabled(false);
        btnAcceptRequest.setVisibility(View.INVISIBLE);

        btnDeclineRequest.setEnabled(false);
        btnDeclineRequest.setVisibility(View.INVISIBLE);


        /*custom for ProgressBar*/
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading Profile");
        mProgressDialog.setMessage("Wait a minute");
        mProgressDialog.setCanceledOnTouchOutside(false); /*used to avoid user tap outside with canceling purpose*/
        mProgressDialog.show();
        // declare the default initial friend_status
        friend_current_status = "not_friend";
        // find in Friend_Requset database to get the status of the friend request with selected person
        mFriendRequestDatabase.child(mCurrentUser.getUid()).child(selected_userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    String status = dataSnapshot.child("request_type").getValue().toString();
                    if(!TextUtils.isEmpty(status) && status != null ){
                        /*if the current status is sent : meaning you have already sent a friend_request to this people*/
                        if(status.equals("sent")){
                            friend_current_status = "sent_req";
                            btnSendFriendRequest.setEnabled(false);
                            btnSendFriendRequest.setVisibility(View.INVISIBLE);
                            /*display the cancel button*/
                            btnCancelRequest.setEnabled(true);
                            btnCancelRequest.setVisibility(View.VISIBLE);

                            btnUnfriend.setEnabled(false);
                            btnUnfriend.setVisibility(View.INVISIBLE);

                            btnDeclineRequest.setEnabled(false);
                            btnDeclineRequest.setVisibility(View.INVISIBLE);

                            btnAcceptRequest.setEnabled(false);
                            btnAcceptRequest.setVisibility(View.INVISIBLE);

                        }else if(status.equals("received")){ /*if the current status is received: meaning this person had already sent to you a friend request*/
                            friend_current_status = "received_req";
                            btnSendFriendRequest.setEnabled(false);
                            btnSendFriendRequest.setVisibility(View.INVISIBLE);

                            btnCancelRequest.setEnabled(false);
                            btnCancelRequest.setVisibility(View.INVISIBLE);

                            btnUnfriend.setEnabled(false);
                            btnUnfriend.setVisibility(View.INVISIBLE);

                            btnDeclineRequest.setEnabled(true);
                            btnDeclineRequest.setVisibility(View.VISIBLE);

                            /*display the accept/decline button*/
                            btnAcceptRequest.setEnabled(true);
                            btnAcceptRequest.setVisibility(View.VISIBLE);

                        }
                    }
                }catch (Exception e){
                    /*if cannot found value in friend_request we will found value is friend database*/
                    mFriendListDatabase.child(mCurrentUser.getUid()).child(selected_userid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try{
                                String status = dataSnapshot.child("from").getValue().toString();

                                friend_current_status = "is_friend";
                                btnSendFriendRequest.setEnabled(false);
                                btnSendFriendRequest.setVisibility(View.INVISIBLE);

                                btnCancelRequest.setEnabled(false);
                                btnCancelRequest.setVisibility(View.INVISIBLE);

                                btnUnfriend.setEnabled(true);
                                btnUnfriend.setVisibility(View.VISIBLE);

                                btnAcceptRequest.setEnabled(false);
                                btnAcceptRequest.setVisibility(View.INVISIBLE);

                                btnDeclineRequest.setEnabled(false);
                                btnDeclineRequest.setVisibility(View.INVISIBLE);

                            }catch (Exception e){
                                // If don't have data in friend request between you and this person and didn't found value in friendlist also => we assume they are not friend
                                friend_current_status = "not_friend";
                                /*display SendReq button*/
                                btnSendFriendRequest.setEnabled(true);
                                btnSendFriendRequest.setVisibility(View.VISIBLE);

                                btnCancelRequest.setEnabled(false);
                                btnCancelRequest.setVisibility(View.INVISIBLE);

                                btnUnfriend.setEnabled(false);
                                btnUnfriend.setVisibility(View.INVISIBLE);

                                btnAcceptRequest.setEnabled(false);
                                btnAcceptRequest.setVisibility(View.INVISIBLE);

                                btnDeclineRequest.setEnabled(false);
                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        /*get value of selected user and display it to activity*/
        mSelectedUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String user_status = dataSnapshot.child("status").getValue().toString();
                String user_avatar = dataSnapshot.child("image").getValue().toString();

                displayName.setText(display_name);
                status.setText(user_status);
                if (!user_avatar.equals("default")) {
                    Picasso.get().load(user_avatar).placeholder(R.drawable.defaultavatar).into(userAvatar);
                    mProgressDialog.dismiss();
                } else {
                    mProgressDialog.hide();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*set event for btnSendFriendRequest*/
        btnSendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // disable and hide the send button
                btnSendFriendRequest.setEnabled(false);
                btnSendFriendRequest.setVisibility(View.INVISIBLE);
                /*show btn Cancel*/
                btnCancelRequest.setEnabled(true);
                btnCancelRequest.setVisibility(View.VISIBLE);
                /*update friend_status = sent_req*/
                friend_current_status = "sent_req";
                /*add new request data to Friend_Requets-CurrentUserID-ReceiverID-{request_type:sent}*/
                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(selected_userid).child("request_type").setValue("sent")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(Task<Void> task) {
                                if (task.isSuccessful()) {
                                    /*after sent_request pushed, update the received data to Friend_Request-ReceiverID-CurrentUserID-{request_type: received}*/
                                    mFriendRequestDatabase.child(selected_userid).child(mCurrentUser.getUid()).child("request_type").setValue("received");
                                    Toast.makeText(ProfileActivity.this, "Sent request successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });
        /*set event for btnCancelRequest*/
        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*disable and hide btnCancel*/
                btnCancelRequest.setEnabled(false);
                btnCancelRequest.setVisibility(View.INVISIBLE);
                /*display btnSend*/
                btnSendFriendRequest.setEnabled(true);
                btnSendFriendRequest.setVisibility(View.VISIBLE);
                /*update friend_status = sent_req*/
                friend_current_status = "not_friend";
                /*delete the sent_request data for sender*/
                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(selected_userid).removeValue();
                /*delete the received_request data for receiver*/
                mFriendRequestDatabase.child(selected_userid).child(mCurrentUser.getUid()).removeValue();
            }
        });

        /*set event for btnAccept*/
        btnAcceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Delete friend-request sent in Friend_request  for selected_userid*/
                mFriendRequestDatabase.child(selected_userid).child(mCurrentUser.getUid()).removeValue();
                /*Delete friend-request received in Friend_request for mCurrentUser*/
                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(selected_userid).removeValue();
                /*Insert mCurrentUser to list friend of selected_userid*/
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                mFriendListDatabase.child(selected_userid).child(mCurrentUser.getUid()).child("from").setValue(timeStamp);
                /*Insert selected_userid to list friend of mCurrentUser*/
                mFriendListDatabase.child(mCurrentUser.getUid()).child(selected_userid).child("from").setValue(timeStamp);
            }
        });

        /*set event for btn Unfriend*/
        btnUnfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*delete selected user in friend list of the currentUSer*/
                mFriendListDatabase.child(mCurrentUser.getUid()).child(selected_userid).removeValue();
                /*delete current user in friend list of selected user*/
                mFriendListDatabase.child(selected_userid).child(mCurrentUser.getUid()).removeValue();
            }
        });


    }
}
