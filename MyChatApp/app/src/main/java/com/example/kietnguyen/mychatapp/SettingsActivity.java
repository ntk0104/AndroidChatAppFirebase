package com.example.kietnguyen.mychatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private CircleImageView userAvatar;
    private TextView usernameDisplay;
    private TextView userStatus;
    private Button btnChangeAvatar;
    private Button btnChangeStatus;
    //for get information about user
    private FirebaseUser mCurrentUser;
    //for get data from realtime db from firebase
    private DatabaseReference mUserDatabase;
    // for get files from Firebase Storage
    private StorageReference mFirebaseStorage;
    private ProgressDialog mProgressDialog;


    private final static int GALLERY_PICK = 1;
    private String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userAvatar = findViewById(R.id.settings_avatarUser);
        usernameDisplay = findViewById(R.id.settings_displayName);
        userStatus = findViewById(R.id.settings_status);
        btnChangeAvatar = findViewById(R.id.settings_changeImage);
        btnChangeStatus = findViewById(R.id.settings_changeStatus);

        mFirebaseStorage = FirebaseStorage.getInstance().getReference();
//        get current user logged in
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = mCurrentUser.getUid();
//        get data of current user from this node
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        /*save strings value for purposing offline capability*/
        mUserDatabase.keepSynced(true);
//        get data from firebase
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                Log.d("TAG", "image = " + image);

                usernameDisplay.setText(name);
                userStatus.setText(status);
//                if user hadn't set avatar display default avatar
                if(!image.equals("default")){
//                    placeholder(src) display the image will be display while waiting the real img load complete
                    Picasso.get().load(image).placeholder(R.drawable.defaultavatar).into(userAvatar);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                startActivity(statusIntent);
            }
        });

        btnChangeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                open the windows for user to select which image they want to use
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });

    }

//    function receive selected image of user then do crop
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            //show the progressdiaglog to let user wait a little bit
            mProgressDialog = new ProgressDialog(SettingsActivity.this);
            mProgressDialog.setTitle("Waiting");
            mProgressDialog.setMessage("Wait a minute for update your avatar");
//            disable tap outside to cancel
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1) /*set initial size to crop image*/
                    .start(this);

        }

//        get the cropped image uri
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
//                uri of the cropped image being pushed to firebase storage
                Uri resultUri = result.getUri();

//                Upload thumb image with bitmap format to firebase storage
//                create File object for uri of cropped image
                File thumb_file_path = new File(resultUri.getPath());

                try {
//                    custom the compressor to compress the image
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file_path);

                    ByteArrayOutputStream thumbByteArrayOutputStream = new ByteArrayOutputStream();
//                    compress the above thumb_bitmap to ByteArrayOutputStream
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, thumbByteArrayOutputStream);
                    byte[] thumb_byte_data = thumbByteArrayOutputStream.toByteArray();
//                    store thumb to firebase storage with path: profile_images -> thumbs -> namefile.jpg
                    final StorageReference thumb_path = mFirebaseStorage.child("profile_images").child("thumbs").child(uid + ".jpg ");

//                    Using uploadTask to upload bitmap format to firebase storage
                    UploadTask uploadTask = thumb_path.putBytes(thumb_byte_data);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(Task<UploadTask.TaskSnapshot> thumb_task) {
                            if(thumb_task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this, "Uploaded thumb image successfully", Toast.LENGTH_SHORT).show();
                                thumb_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String thumb_download_uri = uri.toString();
                                        mUserDatabase.child("thumb_image").setValue(thumb_download_uri);
                                    }
                                });
                            }else {
                                Toast.makeText(SettingsActivity.this, "Uploaded thumb image failed", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

//                    --------FINISHED WITH THUMB IMAGE



//                    Upload directly with original image format
//                named the original image file by uid
                    final StorageReference filepath = mFirebaseStorage.child("profile_images").child(uid + ".jpg");
//                put image file to firebase storage
                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Upload image successfully", Toast.LENGTH_SHORT).show();
                                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String download_uri = uri.toString();
                                        mUserDatabase.child("image").setValue(download_uri);
                                        mProgressDialog.dismiss();
                                        Log.d("TAG", download_uri);
                                        Toast.makeText(SettingsActivity.this, "Update image url succe", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else {
                                mProgressDialog.hide();
                                Toast.makeText(SettingsActivity.this, "Upload image failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
