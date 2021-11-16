package com.example.librarymanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ChatRoomFragment extends Fragment {

    ListView userListView;
    ArrayAdapter arrayAdapter;
    ArrayList<String> users = new ArrayList<>();
    ArrayList<String> noUser = new ArrayList<>();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference databaseReference;

    ChatFragment chatFragment = new ChatFragment();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);



        databaseReference = FirebaseDatabase.getInstance().getReference();
        userListView = view.findViewById(R.id.userListView);

        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String email = dataSnapshot.child("email").getValue().toString();
                        if (!email.equals(mAuth.getCurrentUser().getEmail()))
                        {
                            users.add(email);
                        }
                    }
                    if (getActivity() != null)
                    {
                        if (users.size() != 0)
                        {
                            arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, users);
                        }
                        else
                        {
                            noUser.add("No user is available...");
                            arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, noUser);
                        }
                    }

                    userListView.setAdapter(arrayAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });


        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Bundle args = new Bundle();
                args.putString("email", users.get(position));
                chatFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                       chatFragment).commit();

            }
        });

        return view;
    }
}

