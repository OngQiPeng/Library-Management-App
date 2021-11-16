package com.example.librarymanagementapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ErrorCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.Result;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

public class BorrowFragment extends Fragment {

    private CodeScanner mCodeScanner;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    String userId = fAuth.getCurrentUser().getUid();
    CollectionReference collectionReference = fStore.collection("Book");
    DocumentReference userReference = fStore.collection("User").document(userId);

    List<Book> bookList = new ArrayList<>();
    Boolean found = false;
    Boolean valid = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_borrow, container, false);

        CodeScannerView scannerView = view.findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(getActivity(), scannerView);
        mCodeScanner.setCamera(CodeScanner.CAMERA_BACK);
        mCodeScanner.setFormats(CodeScanner.ALL_FORMATS);

        mCodeScanner.setAutoFocusEnabled(true);
        mCodeScanner.setScanMode(ScanMode.SINGLE);
        mCodeScanner.setFlashEnabled(false);
        setupPermission();

        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        readData(new FireStoreCallback() {
                            @Override
                            public void onCallback(List<Book> bookList, User user) {
                                if (bookList.size() != 0)
                                {
                                    for (Book book : bookList)
                                    {
                                        if (result.getText().trim().equals(book.getBook_id()))
                                        {

                                            found = true;

                                            if (!user.getBookBorrowed().equals("-"))
                                            {
                                                Toast.makeText(getActivity(),"Please return your book before borrow a new book!", Toast.LENGTH_LONG).show();
                                                valid = false;
                                            }

                                            if (!book.getBorrowedBy().equals("-"))
                                            {
                                                Toast.makeText(getActivity(),"This book is currently borrowed by another user!", Toast.LENGTH_LONG).show();
                                                valid = false;
                                            }

                                            if (found && valid)
                                            {
                                                DocumentReference bookReference = fStore.collection("Book").document(book.getBook_id());

                                                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                                        .setTitle("Borrow Book...")
                                                        .setMessage("Do you want to borrow this book?\n" +
                                                                "\nBook Name: " + book.getBook_name()+
                                                                "\nBook Type: " + book.getBook_type()+
                                                                "\nBook Genre: " + book.getBook_genre())
                                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                Map<String, Object> borrowedBy = new HashMap<>();
                                                                borrowedBy.put("BorrowedBy", user.getName());
                                                                bookReference.update(borrowedBy);
                                                                
                                                                Map<String, Object> borrowedBook = new HashMap<>();
                                                                borrowedBook.put("Book borrowed", book.getBook_name());
                                                                userReference.update(borrowedBook);

                                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                                                                String currentDateTime = sdf.format(new Date());
                                                                String borrowId = book.getBook_name() + user.getName() + currentDateTime;
                                                                borrowId = borrowId.replaceAll("\\s","");
                                                                DocumentReference borrowReference = fStore.collection("Borrow History").document(borrowId);

                                                                Map<String, Object> borrowHistory = new HashMap<>();
                                                                borrowHistory.put("Book id", book.getBook_id());
                                                                borrowHistory.put("Book name", book.getBook_name());
                                                                borrowHistory.put("User id", user.getId());
                                                                borrowHistory.put("UserName", user.getName());
                                                                borrowHistory.put("Borrow dateTime", currentDateTime);
                                                                borrowHistory.put("Return dateTime", "-");
                                                                borrowHistory.put("Status", "Borrowed");
                                                                borrowReference.set(borrowHistory);

                                                                Toast.makeText(getActivity(), "You have borrowed the book successfully!",Toast.LENGTH_LONG).show();
                                                                Intent i = new Intent(getActivity(), MainActivity.class);
                                                                startActivity(i);
                                                            }
                                                        })
                                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                mCodeScanner.startPreview();
                                                            }
                                                        }).show();
                                            }

                                            break;
                                        }

                                    }

                                    if (!found)
                                    {
                                        Toast.makeText(getActivity(), "This is not a valid QR code. Tap the screen to scan again.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else
                                {
                                    Toast.makeText(getActivity(),"No book is available now... Tap the screen to scan again.",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
            }
        });

        mCodeScanner.setErrorCallback(new ErrorCallback() {
            @Override
            public void onError(@NonNull Exception error) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("Main", "Camera initialization error:" + error.getMessage());
                    }
                });
            }
        });

        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCodeScanner.startPreview();
            }
        });

        return view;
    }

    private interface FireStoreCallback
    {
        void onCallback(List<Book> bookList, User user);
    }

    private void readData(FireStoreCallback fireStoreCallback)
    {
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    for (DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        String bookId = documentSnapshot.getId();
                        String bookName = documentSnapshot.getString("Name");
                        String bookType = documentSnapshot.getString("Type");
                        String bookGenre = documentSnapshot.getString("Genre");
                        String borrowedBy = documentSnapshot.getString("BorrowedBy");
                        Book book = new Book(bookId, bookName, bookType, bookGenre, borrowedBy);
                        bookList.add(book);

                    }

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

                                    fireStoreCallback.onCallback(bookList, user);

                                }
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    public void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    private void setupPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            makeRequest();
        }

    }

    private void makeRequest() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }


    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA))
                    {
                        Toast.makeText(getActivity(), "You need the camera permission to borrow book! Please turn on the camera permission manually", Toast.LENGTH_SHORT).show();
                    }

                    else if(!result)
                    {
                        Toast.makeText(getActivity(), "You need the camera permission to borrow book!", Toast.LENGTH_SHORT).show();
                    }
                    
                }

            });







}

