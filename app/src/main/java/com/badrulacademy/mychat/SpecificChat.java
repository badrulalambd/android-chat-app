package com.badrulacademy.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SpecificChat extends AppCompatActivity {

    EditText mGetMessage;
    ImageView mSenderMessageButton;

    CardView mSendMessageCardView;

    androidx.appcompat.widget.Toolbar mToolbarOfSpecificChat;
    ImageView mImageViewOfSpecificUser;
    TextView mNameOfSpecificUser;

    private String enteredMessage;
    Intent intent;
    String mReceiverName, mSenderName, mReceiverUid, mSenderUid;

    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    String senderRoom, receiverRoom;

    ImageButton mBackButtonOfSpecificChat;

    RecyclerView mMessageRecyclerView;

    String currentTime;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    MessagesAdapter messagesAdapter;
    ArrayList<Messages> messagesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_chat);

        mGetMessage = findViewById(R.id.getMessage_id);
        mSendMessageCardView = findViewById(R.id.cardViewOfSendMessage_id);
        mSenderMessageButton = findViewById(R.id.imageViewOfSendMessage_id);
        mToolbarOfSpecificChat = findViewById(R.id.toolBarOfSpecificChat_id);
        mNameOfSpecificUser = findViewById(R.id.nameOfSpecificUser_id);
        mImageViewOfSpecificUser = findViewById(R.id.specificUserImageInImageView_id);
        mBackButtonOfSpecificChat = findViewById(R.id.backButtonOfSpecificChat_id);

        messagesArrayList = new ArrayList<>();
        mMessageRecyclerView = findViewById(R.id.recyclerViewOfSpecificChat_id);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(linearLayoutManager);
        messagesAdapter = new MessagesAdapter(SpecificChat.this, messagesArrayList);
        mMessageRecyclerView.setAdapter(messagesAdapter);


        intent = getIntent();

        setSupportActionBar(mToolbarOfSpecificChat);
        mToolbarOfSpecificChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "ToolBar is Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm a");

        //take sender UID
        mSenderUid = firebaseAuth.getUid();
        //Receive receiver UID from ChatFragment
        mReceiverUid = getIntent().getStringExtra("receiveruid");
        mReceiverName = getIntent().getStringExtra("name");

        //It's not possible to one-to-one chat only using sender&receiver uid
        //Need sender&receiver room to chat one-to-one
        //Create senderRoom adding sender and receiver uid
        senderRoom = mSenderUid + mReceiverUid;
        //Create receiverUid adding receiver and sender uid
        receiverRoom = mReceiverUid + mSenderUid;


        DatabaseReference databaseReference = firebaseDatabase.getReference().child("chats").child(senderRoom).child("messages");
        messagesAdapter = new MessagesAdapter(SpecificChat.this, messagesArrayList);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    Messages messages = snapshot1.getValue(Messages.class);
                    messagesArrayList.add(messages);
                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mBackButtonOfSpecificChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mNameOfSpecificUser.setText(mReceiverName);
        String uri = intent.getStringExtra("imageuri");
        if(uri.isEmpty()){
            Toast.makeText(getApplicationContext(), "null is received", Toast.LENGTH_SHORT).show();
        }
        else {
            Picasso.get()
                    .load(uri)
                    .into(mImageViewOfSpecificUser);
        }

        mSenderMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enteredMessage = mGetMessage.getText().toString();
                if(enteredMessage.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter message first", Toast.LENGTH_SHORT).show();
                }
                else {
                    Date date = new Date();
                    currentTime = simpleDateFormat.format(calendar.getTime());
                    Messages messages  = new Messages(enteredMessage, firebaseAuth.getUid(), date.getTime(), currentTime);
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    //here chat is place where we store all message
                    firebaseDatabase.getReference().child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    firebaseDatabase.getReference()
                                            .child("chats")
                                            .child(receiverRoom)
                                            .child("messages")
                                            .push()
                                            .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });
                                }
                            });
                    mGetMessage.setText(null);

                }
            }
        });

    }



    @Override
    public void onStart() {
        super.onStart();
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(messagesAdapter!=null){
            messagesAdapter.notifyDataSetChanged();
        }
    }

}