package com.example.librarymanagementapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ChatFragment extends Fragment {

    EditText message;
    Button send;
    ListView chatListView;
    ArrayAdapter arrayAdapter;
    ArrayList<String> messages = new ArrayList<>();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
    int count;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        message = view.findViewById(R.id.message);
        send = view.findViewById(R.id.send);
        chatListView = view.findViewById(R.id.chatListView);

        String otherEmail = getArguments().getString("email");
        //Log.d("user", otherEmail);
        String email = mAuth.getCurrentUser().getEmail();

        getActivity().setTitle("Chat with " + otherEmail);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Write a message!", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("sender", email);
                    messageData.put("receiver", otherEmail);
                    messageData.put("message", message.getText().toString());

                    databaseReference.child("chats").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists())
                            {
                                count = (int) (snapshot.getChildrenCount() + 1);
                            } else {
                                count = 1;
                            }
                            databaseReference.child("chats").child(String.valueOf(count)).setValue(messageData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        message.setText("");
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Error in sending message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });


        databaseReference.child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    messages.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren())
                    {
                        if ((dataSnapshot.child("sender").getValue().toString().equals(email) && dataSnapshot.child("receiver").getValue().toString().equals(otherEmail))
                                || (dataSnapshot.child("receiver").getValue().toString().equals(email) && dataSnapshot.child("sender").getValue().toString().equals(otherEmail))) {
                            String message = dataSnapshot.child("message").getValue().toString();

                            if (!dataSnapshot.child("sender").getValue().toString().equals(email)){
                                message = "> " + message;
                            }
                            messages.add(message);
                        }
                    }
                    if (getActivity() != null)
                    {
                        arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, messages);
                        chatListView.setAdapter(arrayAdapter);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }



}
