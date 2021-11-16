package com.example.librarymanagementapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseUser fUser = fAuth.getCurrentUser();

    String userId = fUser.getUid();
    DocumentReference userReference = fStore.collection("User").document(userId);
    CollectionReference borrowHistoryReference = fStore.collection("Borrow History");
    DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference chatsDatabase = FirebaseDatabase.getInstance().getReference();
    DatabaseReference bookingDatabase = FirebaseDatabase.getInstance().getReference();

    List<BorrowHistory> borrowHistoryList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView tv_userName = view.findViewById(R.id.tv_userName);
        TextView tv_email = view.findViewById(R.id.tv_email);
        Button mChangeEmailBtn = view.findViewById(R.id.btn_changeEmail);
        Button mChangePasswordBtn = view.findViewById(R.id.btn_changePassword);
        Button mChangeUserNameBtn = view.findViewById(R.id.btn_changeUserName);

        readUserData(new userFireStoreCallback() {
            @Override
            public void onCallback(User user) {
                tv_userName.setText(user.getName());
                tv_email.setText(user.getEmail());
            }
        });


        mChangePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUserData(new userFireStoreCallback() {
                    @Override
                    public void onCallback(User user) {
                        LinearLayout layout = new LinearLayout(getContext());
                        layout.setOrientation(LinearLayout.VERTICAL);

                        EditText mOldPassword = new EditText(v.getContext());
                        mOldPassword.setHint("Current Password");
                        mOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        layout.addView(mOldPassword);

                        EditText mNewPassword = new EditText(v.getContext());
                        mNewPassword.setHint("New Password");
                        mNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        layout.addView(mNewPassword);

                        AlertDialog alertDialog = new AlertDialog.Builder(v.getContext())
                                .setTitle("Change Password")
                                .setMessage("Please enter your old password and new password")
                                .setView(layout)
                                .setPositiveButton("Update Password", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        String oldPassword = mOldPassword.getText().toString().trim();
                                        String newPassword = mNewPassword.getText().toString().trim();
                                        boolean valid = true;

                                        if (oldPassword.isEmpty())
                                        {
                                            Toast.makeText(getActivity(),"Please fill in your current password!", Toast.LENGTH_SHORT).show();
                                            valid = false;
                                        }

                                        if (newPassword.isEmpty())
                                        {
                                            Toast.makeText(getActivity(),"Please fill in your new password!", Toast.LENGTH_SHORT).show();
                                            valid = false;
                                        }

                                        if (!newPassword.isEmpty())
                                        {
                                            if (newPassword.length() < 6)
                                            {
                                                Toast.makeText(getActivity(),"The password should be more than or equal to 6 characters!", Toast.LENGTH_SHORT).show();
                                                valid = false;
                                            }

                                            if (TextUtils.isDigitsOnly(newPassword) || newPassword.matches("[a-zA-Z]+"))
                                            {
                                                Toast.makeText(getActivity(),"The password should contain digit and letter!", Toast.LENGTH_SHORT).show();
                                                valid = false;
                                            }

                                            if (newPassword.equals(oldPassword))
                                            {
                                                Toast.makeText(getActivity(),"The new password is same as the current password!", Toast.LENGTH_SHORT).show();
                                                valid = false;
                                            }

                                        }

                                        if (valid)
                                        {
                                            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

                                            fUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        fUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    Toast.makeText(getActivity(), "Password is updated successfully!", Toast.LENGTH_SHORT).show();
                                                                }
                                                                else
                                                                {
                                                                    Toast.makeText(getActivity(), "Failed to update the password...", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(getActivity(), "Incorrect current password...", Toast.LENGTH_SHORT).show();
                                                    }
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

            }
        });

        mChangeEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUserData(new userFireStoreCallback() {
                    @Override
                    public void onCallback(User user) {
                        LinearLayout layout = new LinearLayout(getContext());
                        layout.setOrientation(LinearLayout.VERTICAL);

                        EditText mEmail = new EditText(v.getContext());
                        mEmail.setHint("New Email");
                        layout.addView(mEmail);

                        EditText mPassword = new EditText(v.getContext());
                        mPassword.setHint("Password");
                        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        layout.addView(mPassword);

                        AlertDialog alertDialog = new AlertDialog.Builder(v.getContext())
                                .setTitle("Change Email")
                                .setMessage("Please enter your new email and current password")
                                .setView(layout)
                                .setPositiveButton("Update Email", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        String newEmail = mEmail.getText().toString().trim();
                                        String password = mPassword.getText().toString().trim();
                                        boolean valid = true;
                                        boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(newEmail).matches();

                                        if (newEmail.isEmpty())
                                        {
                                            Toast.makeText(getActivity(),"Please fill in your new email!", Toast.LENGTH_SHORT).show();
                                            valid = false;
                                        }

                                        if (!newEmail.isEmpty() && !emailValid)
                                        {
                                            Toast.makeText(getActivity(), "This is not a valid email!", Toast.LENGTH_SHORT).show();
                                            valid = false;
                                        }

                                        if (user.getEmail().equals(newEmail))
                                        {
                                            Toast.makeText(getActivity(), "The new email is same as the current email", Toast.LENGTH_SHORT).show();
                                            valid = false;
                                        }

                                        if (password.isEmpty())
                                        {
                                            Toast.makeText(getActivity(),"Please fill in your password!", Toast.LENGTH_SHORT).show();
                                            valid = false;
                                        }

                                        if (valid)
                                        {

                                            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

                                            fUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        fUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    Map<String, Object> userData = new HashMap<>();
                                                                    userData.put("Email", newEmail);
                                                                    userReference.update(userData);
                                                                    tv_email.setText(newEmail);

                                                                    userDatabase.child("users/"+ userId +"/email").setValue(newEmail);

                                                                    bookingDatabase.child("Booking").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot)
                                                                        {

                                                                            ArrayList <String> bookingNo = new ArrayList<>();

                                                                            for (DataSnapshot dataSnapshot : snapshot.getChildren())
                                                                            {

                                                                                if (dataSnapshot.child("email").getValue(String.class).equals(user.getEmail()))
                                                                                {
                                                                                    bookingNo.add(dataSnapshot.getKey());
                                                                                }
                                                                            }

                                                                            for (String parentKey : bookingNo)
                                                                            {
                                                                                bookingDatabase.child("Booking/"+ parentKey + "/email").setValue(newEmail);
                                                                            }

                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                        }
                                                                    });

                                                                    chatsDatabase.child("chats").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                                            if (snapshot.getChildrenCount() != 0)
                                                                            {
                                                                                //for (int i = 0; i < snapshot.getChildrenCount(); i++) {
                                                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                                                                    DatabaseReference receiverReference = FirebaseDatabase.getInstance().getReference("chats");
                                                                                    receiverReference.child(dataSnapshot.getKey()).child("receiver").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                            if (snapshot.getValue(String.class).equals(user.getEmail())) {
                                                                                                chatsDatabase.child("chats/"+ dataSnapshot.getKey() +"/receiver").setValue(newEmail);
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                                        }
                                                                                    });

                                                                                    DatabaseReference senderReference = FirebaseDatabase.getInstance().getReference("chats");
                                                                                    senderReference.child(dataSnapshot.getKey()).child("sender").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                            if (snapshot.getValue(String.class).equals(user.getEmail())) {
                                                                                                chatsDatabase.child("chats/"+ dataSnapshot.getKey() +"/sender").setValue(newEmail);
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                                        }
                                                                                    });

                                                                                }
                                                                            }

                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                        }
                                                                    });

                                                                    Toast.makeText(getActivity(), "Email is updated successfully!", Toast.LENGTH_SHORT).show();


                                                                }
                                                                else
                                                                {
                                                                    if (task.getException() instanceof FirebaseAuthUserCollisionException)
                                                                    {
                                                                        Toast.makeText(getActivity(), "The email is used by other user...", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    else
                                                                    {
                                                                        Toast.makeText(getActivity(), "Failed to update the email...", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(getActivity(), "Incorrect password...", Toast.LENGTH_SHORT).show();
                                                    }
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

            }
        });

        mChangeUserNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUserData(new userFireStoreCallback() {
                    @Override
                    public void onCallback(User user) {
                        EditText mNewUserName = new EditText(v.getContext());
                        AlertDialog alertDialog = new AlertDialog.Builder(v.getContext())
                                .setTitle("Change Username")
                                .setMessage("Please enter your new username")
                                .setView(mNewUserName)
                                .setPositiveButton("Change Username", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        String newUserName = mNewUserName.getText().toString().trim();
                                        boolean valid = true;

                                        if (newUserName.isEmpty())
                                        {
                                            Toast.makeText(getActivity(),"Please fill in your new username!", Toast.LENGTH_SHORT).show();
                                            valid = false;
                                        }

                                        if (newUserName.equals(user.getName()))
                                        {
                                            Toast.makeText(getActivity(),"The new username is same as current username!", Toast.LENGTH_SHORT).show();
                                            valid = false;
                                        }

                                        if (valid)
                                        {

                                            Map<String, Object> userName = new HashMap<>();
                                            userName.put("Name", newUserName);
                                            userReference.update(userName);
                                            tv_userName.setText(newUserName);
                                            updateBorrowHistory(newUserName);


                                            bookingDatabase.child("Booking").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot)
                                                {
                                                    ArrayList <String> bookingNo = new ArrayList<>();

                                                    for (DataSnapshot dataSnapshot : snapshot.getChildren())
                                                    {

                                                        if (dataSnapshot.child("name").getValue(String.class).equals(user.getName()))
                                                        {
                                                            bookingNo.add(dataSnapshot.getKey());
                                                        }
                                                    }

                                                    for (String parentKey : bookingNo)
                                                    {
                                                        bookingDatabase.child("Booking/"+ parentKey + "/name").setValue(newUserName);
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });




                                            Toast.makeText(getActivity(), "Username is updated successfully!", Toast.LENGTH_SHORT).show();

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

            }
        });



        return view;
    }

    private interface userFireStoreCallback
    {
        void onCallback(User user);
    }

    private void readUserData(ProfileFragment.userFireStoreCallback fireStoreCallback)
    {

        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null)
                    {
                        String name = documentSnapshot.getString("Name");
                        String email = documentSnapshot.getString("Email");
                        String bookBorrowed = documentSnapshot.getString("Book borrowed");
                        String userType = documentSnapshot.getString("userType");
                        User user = new User(userId, name, email, bookBorrowed, userType);

                        fireStoreCallback.onCallback(user);

                    }
                }
            }
        });
    }

    private void updateBorrowHistory(String newUserName)
    {

        borrowHistoryReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {

                    for (DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        if (documentSnapshot.getString("User id").equals(userId))
                        {
                            DocumentReference borrowHistoryUserReference = fStore.collection("Borrow History").document(documentSnapshot.getId());

                            String bookId = documentSnapshot.getString("Book id");
                            String bookName = documentSnapshot.getString("Book name");
                            String userId = documentSnapshot.getString("User id");
                            String userName = documentSnapshot.getString("UserName");
                            String borrowDateTime = documentSnapshot.getString("Borrow dateTime");
                            String returnDateTime = documentSnapshot.getString("Return dateTime");
                            String status = documentSnapshot.getString("Status");

                            BorrowHistory borrowHistory = new BorrowHistory(bookId, bookName, userId,
                                    userName, borrowDateTime, returnDateTime, status);

                            borrowHistoryList.add(borrowHistory);

                            Map<String, Object> user = new HashMap<>();
                            user.put("UserName", newUserName);
                            borrowHistoryUserReference.update(user);
                        }
                    }

                }
            }
        });
    }


}
