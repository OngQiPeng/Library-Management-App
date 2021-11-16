package com.example.librarymanagementapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddBookFragment extends Fragment {

    FirebaseFirestore fStore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_book,container,false);

        EditText mBookName = view.findViewById(R.id.et_bookName);
        RadioGroup rg_bookType = view.findViewById(R.id.radioGroup_bookType);
        Spinner mBookGenre = view.findViewById(R.id.spinner_bookGenre);
        Button mAddBookButton = view.findViewById(R.id.btn_addBook);
        ProgressBar mProgressBar = view.findViewById(R.id.addBook_progressBar);

        mProgressBar.setVisibility(View.GONE);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.bookGenre));

        mBookGenre.setAdapter(adapter);

        fStore = FirebaseFirestore.getInstance();

        mAddBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String bookName = mBookName.getText().toString().trim();
                int selectedButtonId = rg_bookType.getCheckedRadioButtonId();
                RadioButton rb_bookType = view.findViewById(selectedButtonId);
                String genre = mBookGenre.getSelectedItem().toString();

                boolean valid = true;

                if (bookName.isEmpty())
                {
                    mBookName.setError("Please fill in the book name!");
                    valid = false;
                }

                if (selectedButtonId == -1)
                {
                    Toast.makeText(getActivity(), "Please select a book type", Toast.LENGTH_SHORT).show();
                    valid = false;
                }

                if (genre.equals(getResources().getStringArray(R.array.bookGenre)[0]))
                {
                    Toast.makeText(getActivity(),"Please select a book genre", Toast.LENGTH_SHORT).show();
                    valid = false;
                }

                if (valid)
                {
                    mProgressBar.setVisibility(View.VISIBLE);
                    
                    String type = rb_bookType.getText().toString();
                    String bookId = "PNM" + bookName + type + genre;
                    bookId = bookId.replaceAll("\\s","");
                    DocumentReference documentReference = fStore.collection("Book").document(bookId);
                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful())
                            {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists())
                                {
                                    Toast.makeText(getActivity(),"The book already exists!", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Map<String, Object> book = new HashMap<>();
                                    book.put("Name", bookName);
                                    book.put("Type", type);
                                    book.put("Genre", genre);
                                    book.put("BorrowedBy", "-");
                                    documentReference.set(book);

                                    Toast.makeText(getActivity(),"The book is added!", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                Toast.makeText(getActivity(), "Failed: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    mBookName.setText("");
                    rg_bookType.clearCheck();
                    mBookGenre.setSelection(0);
                    mProgressBar.setVisibility(View.GONE);

                }


            }
        });

        return view;
    }
}
