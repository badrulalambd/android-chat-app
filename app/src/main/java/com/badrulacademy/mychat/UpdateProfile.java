package com.badrulacademy.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfile extends AppCompatActivity {

    private EditText mNewUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;


    private FirebaseFirestore firebaseFirestore;

    private ImageView mGetNewImageInImageView;

    private StorageReference storageReference;

    private String ImageUriAccessToken;

    androidx.appcompat.widget.Toolbar mToolBarOfUpdateProfile;
    ImageView mBackButtonOfUpdateProfile;
    android.widget.Button mUpdateProfileButton;

    ProgressBar mProgressBarOfUpdateProfile;
    private Uri imagePath;

    Intent intent;

    private static int PIC_IMAGE = 123;
    String newName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        Toast.makeText(getApplicationContext(), "Start Updating... ...", Toast.LENGTH_SHORT).show();

        mToolBarOfUpdateProfile = findViewById(R.id.toolBarOfUpdateProfile_id);
        mBackButtonOfUpdateProfile = findViewById(R.id.backButtonOfUpdateProfile_id);
        mGetNewImageInImageView = findViewById(R.id.getNewUserImage_inImageView_id);
        mProgressBarOfUpdateProfile = findViewById(R.id.progressBarOfUpdateProfile_id);
        mNewUserName = findViewById(R.id.getNewUserName_id);
        mUpdateProfileButton = findViewById(R.id.updateProfileButton_id);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        intent = getIntent();

        setSupportActionBar(mToolBarOfUpdateProfile);

        mBackButtonOfUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mNewUserName.setText(intent.getStringExtra("name"));


        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());

        mUpdateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newName = mNewUserName.getText().toString();
                if(newName.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Name is Empty", Toast.LENGTH_SHORT).show();
                }
                else if(imagePath!=null){

                    mProgressBarOfUpdateProfile.setVisibility(View.VISIBLE);
                    UserProfile mUserProfile = new UserProfile(newName, firebaseAuth.getUid());
                    databaseReference.setValue(mUserProfile);

                    updateImageToStorage();

                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                    mProgressBarOfUpdateProfile.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(UpdateProfile.this, ChatActivity.class);
                    startActivity(intent);
                    finish();

                }
                else{
                    mProgressBarOfUpdateProfile.setVisibility(View.VISIBLE);
                    UserProfile mUserProfile = new UserProfile(newName, firebaseAuth.getUid());
                    databaseReference.setValue(mUserProfile);

                    updateNameOnCloudFirestore();

                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                    mProgressBarOfUpdateProfile.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(UpdateProfile.this, ChatActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });



        //Select image to update
        mGetNewImageInImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, PIC_IMAGE);
            }
        });
        //Show image before updating
        storageReference = firebaseStorage.getReference();
        //as same as SetProfile
        storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageUriAccessToken = uri.toString();
                        Picasso.get()
                                .load(uri)
                                .into(mGetNewImageInImageView);
                    }
                });

    }



    ////////////////////////////
    //UpdateName
    private void updateNameOnCloudFirestore() {
        Toast.makeText(getApplicationContext(), "updateNameOnCloudFirestore()... ...", Toast.LENGTH_SHORT).show();
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", newName);
        userData.put("image", ImageUriAccessToken);
        userData.put("uid", firebaseAuth.getUid());
        userData.put("status", "Online");

        documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Profile Updated successfully", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to Update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    ///////////////////////////////
    //Update ProfileImage
    private void updateImageToStorage() {
        Toast.makeText(getApplicationContext(), "updateImageToStorage()... ...", Toast.LENGTH_SHORT).show();
        StorageReference imageRef = storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic");

        //Image compression
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);

        }catch (Exception e){
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        //Putting image to storage
        UploadTask uploadTask = imageRef.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageUriAccessToken = uri.toString();
                        Toast.makeText(getApplicationContext(), "URI get success", Toast.LENGTH_SHORT).show();
                        updateNameOnCloudFirestore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "URI get Failed", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(getApplicationContext(), "Image is Updated", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Image not Updated", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //To get What image is selected by the user on MainActivity../
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Toast.makeText(getApplicationContext(), "onActivityResult()... ...", Toast.LENGTH_SHORT).show();
        //take the imageCode successfully form the gallery
        if(requestCode == PIC_IMAGE && resultCode == RESULT_OK){
            imagePath = data.getData();
            mGetNewImageInImageView.setImageURI(imagePath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onStop() {
        super.onStop();
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("status", "OffLine").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Now user is Offline", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        DocumentReference documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("status", "Online").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Now user is Online", Toast.LENGTH_SHORT).show();
            }
        });
    }

}