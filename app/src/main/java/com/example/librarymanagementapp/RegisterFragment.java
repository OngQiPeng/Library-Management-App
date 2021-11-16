package com.example.librarymanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RegisterFragment extends Fragment {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        TextView tv_login = view.findViewById(R.id.tv_login);
        EditText mName = view.findViewById(R.id.et_reg_name);
        EditText mEmail = view.findViewById(R.id.et_reg_email);
        EditText mPassword = view.findViewById(R.id.et_reg_password);
        Button mRegisterBtn = view.findViewById(R.id.btn_register);
        Button mChangeUserTypeBtn = view.findViewById(R.id.btn_changeUserType);
        ProgressBar mProgressBar = view.findViewById(R.id.reg_progressBar);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        mProgressBar.setVisibility(View.GONE);

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginRegister.viewpager.setCurrentItem(0);
            }
        });

        mChangeUserTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRegisterBtn.getText().toString().equals("Signup")) {
                    mRegisterBtn.setText("Signup as admin");
                }
                else
                {
                    mRegisterBtn.setText("Signup");
                }
            }
        });


        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = mName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                boolean valid = true;
                boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();

                if (name.isEmpty()) {
                    mName.setError("Please fill in your name!");
                    valid = false;
                }

                if (email.isEmpty()) {
                    mEmail.setError("Please fill in your email!");
                    valid = false;
                }

                if (!email.isEmpty() && !emailValid) {
                    mEmail.setError("This is not a valid email!");
                    valid = false;
                }

                if (password.isEmpty()) {
                    mPassword.setError("Please fill in your password!");
                    valid = false;
                }

                if (!password.isEmpty()) {
                    if (password.length() < 6) {
                        mPassword.setError("The password should be more than or equal to 6 characters!");
                        valid = false;
                    }

                    if (TextUtils.isDigitsOnly(password) || password.matches("[a-zA-Z]+")) {
                        mPassword.setError("The password should contain digit and letter!");
                        valid = false;
                    }

                }

                if (valid)
                {
                    mProgressBar.setVisibility(View.VISIBLE);

                    fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                userId = fAuth.getCurrentUser().getUid();
                                DocumentReference documentReference = fStore.collection("User").document(userId);
                                Map<String, Object> user = new HashMap<>();
                                user.put("Name", name);
                                user.put("Email", email);
                                user.put("Book borrowed", "-");
                                if (mRegisterBtn.getText().toString().equals("Signup")) {
                                    user.put("userType", "User");
                                } else {
                                    user.put("userType", "Admin");
                                }

                                documentReference.set(user);

                                databaseReference.child("users").child(fAuth.getCurrentUser().getUid()).child("email").setValue(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), "Failure in saving data : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Toast.makeText(getActivity(), "User account is created successfully", Toast.LENGTH_SHORT).show();

                                Intent i = new Intent(getActivity(), MainActivity.class);
                                startActivity(i);


                            } else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(getActivity(), "The account is already existed", Toast.LENGTH_SHORT).show();
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                    });

                }
            }
        });


        return view;



    }


}


