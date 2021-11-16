package com.example.librarymanagementapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {

    FirebaseAuth fAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        TextView tv_signUp = view.findViewById(R.id.tv_signUp);
        EditText mEmail = view.findViewById(R.id.et_login_email);
        EditText mPassword = view.findViewById(R.id.et_login_password);
        Button mLoginButton = view.findViewById(R.id.btn_login);
        ProgressBar mProgressBar = view.findViewById(R.id.login_progressBar);
        TextView tv_forgotPassword = view.findViewById(R.id.tv_forgotPassword);

        fAuth = FirebaseAuth.getInstance();

        mProgressBar.setVisibility(View.GONE);

        tv_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginRegister.viewpager.setCurrentItem(1);
            }
        });


        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                boolean valid = true;
                boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();

                if (email.isEmpty())
                {
                    mEmail.setError("Please fill in your email!");
                    valid = false;
                }

                if (!email.isEmpty() && !emailValid)
                {
                    mEmail.setError("This is not a valid email!");
                    valid = false;
                }

                if (password.isEmpty())
                {
                    mPassword.setError("Please fill in your password!");
                    valid = false;
                }

                if (valid)
                {
                    mProgressBar.setVisibility(View.VISIBLE);

                    fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(getActivity(), "Welcome to the library!", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(getActivity(), MainActivity.class);
                                startActivity(i);

                            }
                            else
                            {
                                mProgressBar.setVisibility(View.GONE);
                                if (task.getException() instanceof FirebaseAuthInvalidUserException)
                                {
                                    Toast.makeText(getActivity(), "User account does not exist", Toast.LENGTH_SHORT).show();
                                }

                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                                {
                                    Toast.makeText(getActivity(), "Incorrect password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }


            }
        });

        tv_forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText mResetEmail = new EditText(v.getContext());
                AlertDialog alertDialog = new AlertDialog.Builder(v.getContext())
                        .setTitle("Reset Password")
                        .setMessage("Enter your email to receive reset password link")
                        .setView(mResetEmail)
                        .setPositiveButton("Send Email", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String resetEmail = mResetEmail.getText().toString().trim();
                                boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches();
                                boolean valid = true;

                                if (resetEmail.isEmpty())
                                {
                                    Toast.makeText(getActivity(),"Please fill in your email!", Toast.LENGTH_SHORT).show();
                                    valid = false;
                                }

                                if (!resetEmail.isEmpty() && !emailValid)
                                {
                                    Toast.makeText(getActivity(),"This is not a valid email!", Toast.LENGTH_SHORT).show();
                                    valid = false;
                                }

                                if (valid)
                                {
                                    fAuth.sendPasswordResetEmail(resetEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getActivity(), "Reset link is sent to your email!", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity(), "The account does not exist!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        });

        return view;

    }


}