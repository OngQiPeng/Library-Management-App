package com.example.librarymanagementapp;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ReturnBookFragment extends Fragment {

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    CollectionReference collectionReference = fStore.collection("Borrow History");
    List <BorrowHistory> borrowHistoryList = new ArrayList<>();
    List <BorrowHistory> bookList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);

        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setDistanceToTriggerSync(40);


        ListView listView = view.findViewById(R.id.lv_returnBook);
        TextView tv_no_book = view.findViewById(R.id.tv_no_book);
        tv_no_book.setText("There is no book to be returned. Refresh the page to get the latest borrow list.");

        readData(new FireStoreCallback() {
            @Override
            public void onCallback(List<BorrowHistory> mBorrowHistoryList) {

                for (BorrowHistory borrowHistory : mBorrowHistoryList)
                {
                    if (borrowHistory.getStatus().equals("Borrowed"))
                    {
                        bookList.add(borrowHistory);
                    }
                }

                if (bookList.size() != 0)
                {
                    listView.setVisibility(View.VISIBLE);
                    tv_no_book.setVisibility(View.GONE);
                    listView.setAdapter(new ListAdapter((ArrayList<BorrowHistory>) bookList, getContext(), "ReturnBookFragment"));

                }

                else
                {
                    listView.setVisibility(View.GONE);
                    tv_no_book.setVisibility(View.VISIBLE);
                }

            }
        });


        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                bookList.clear();
                Log.d("clear", String.valueOf(bookList.size()));
                readData(new FireStoreCallback() {
                    @Override
                    public void onCallback(List<BorrowHistory> mBorrowHistoryList) {

                        for (BorrowHistory borrowHistory : mBorrowHistoryList)
                        {
                            if (borrowHistory.getStatus().equals("Borrowed"))
                            {
                                bookList.add(borrowHistory);
                            }
                        }

                        Log.d("size", String.valueOf(bookList.size()));
                        if (bookList.size() != 0)
                        {
                            listView.setVisibility(View.VISIBLE);
                            tv_no_book.setVisibility(View.GONE);
                            listView.setAdapter(new ListAdapter((ArrayList<BorrowHistory>) bookList, getContext(), "ReturnBookFragment"));
                        }

                        else
                        {
                            listView.setVisibility(View.GONE);
                            tv_no_book.setVisibility(View.VISIBLE);
                        }

                    }
                });

                pullToRefresh.setRefreshing(false);
            }
        });

        return view;
    }


    private interface FireStoreCallback
    {
        void onCallback(List<BorrowHistory> mBorrowHistoryList);
    }

    private void readData(ReturnBookFragment.FireStoreCallback fireStoreCallback)
    {
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    borrowHistoryList.clear();
                    for (DocumentSnapshot documentSnapshot : task.getResult())
                    {
                        String bookId = documentSnapshot.getString("Book id");
                        String bookName = documentSnapshot.getString("Book name");
                        String userId = documentSnapshot.getString("User id");
                        String userName = documentSnapshot.getString("UserName");
                        String borrowDateTime = documentSnapshot.getString("Borrow dateTime");
                        String returnDateTime = documentSnapshot.getString("Return dateTime");
                        String status = documentSnapshot.getString("Status");
                        BorrowHistory borrowHistory = new BorrowHistory(
                                bookId, bookName, userId, userName, borrowDateTime, returnDateTime, status);
                        borrowHistoryList.add(borrowHistory);
                    }
                    Log.d("readData", String.valueOf(borrowHistoryList.size()));
                    fireStoreCallback.onCallback(borrowHistoryList);
                }
            }
        });
    }
}
