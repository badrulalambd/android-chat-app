package com.badrulacademy.mychat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.zip.Inflater;

public class ChatFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    LinearLayoutManager linearLayoutManager;
    private FirebaseAuth firebaseAuth;

    ImageView mImageViewOfUser;

    FirestoreRecyclerAdapter<FirebaseModel, NoteViewHolder> chatAdapter;

    RecyclerView mRecyclerView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.chatfragment, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mRecyclerView = v.findViewById(R.id.recyclerView_id);

        //////Query to Fetch all users
        Query query = firebaseFirestore.collection("Users");//According to SetProfile
        //Query to Fetch all users Except me
        //Query query = firebaseFirestore.collection("Users").whereNotEqualTo("uid", firebaseAuth.getUid());
        FirestoreRecyclerOptions<FirebaseModel> allUsers = new FirestoreRecyclerOptions.Builder<FirebaseModel>().setQuery(query, FirebaseModel.class).build();

        //provide allUsers as parameter
        chatAdapter = new FirestoreRecyclerAdapter<FirebaseModel, NoteViewHolder>(allUsers) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull FirebaseModel model) {

                holder.particularUserName.setText(model.getName());
                String uri = model.getImage();

                Picasso.get()
                        .load(uri)
                        .into(mImageViewOfUser);

                if(model.getStatus().equals("Online")){
                    holder.statusOfUser.setText(model.getStatus());
                    holder.statusOfUser.setTextColor(Color.GREEN);
                }
                else {
                    holder.statusOfUser.setText(model.getStatus());
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Item is clicked", Toast.LENGTH_SHORT).show();
                        //After UpdateProfile.java code...
                        Intent intent = new Intent(getActivity(), SpecificChat.class);
                        intent.putExtra("name", model.getName());
                        intent.putExtra("receiveruid", model.getUid());
                        intent.putExtra("imageuri",model.getImage() );
                        startActivity(intent);
                    }
                });


            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatviewlayout, parent, false);
                return new NoteViewHolder(view);
            }
        };
        mRecyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(chatAdapter);

        return v;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{

        private TextView particularUserName;
        private TextView statusOfUser;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            particularUserName = itemView.findViewById(R.id.nameOfUser_id);
            statusOfUser = itemView.findViewById(R.id.statusOfUser_id);
            mImageViewOfUser = itemView.findViewById(R.id.imageViewOfUser_id);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        chatAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(chatAdapter!=null){
            chatAdapter.stopListening();
        }
    }
}
