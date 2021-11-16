package com.example.librarymanagementapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class UserBorrowHistoryFragment extends Fragment {

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    CollectionReference collectionReference = fStore.collection("Borrow History");
    List <BorrowHistory> borrowHistoryList = new ArrayList<>();
    List <BorrowHistory> bookList = new ArrayList<>();

    String id = fAuth.getCurrentUser().getUid();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);

        SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setDistanceToTriggerSync(200);

        ListView listView = view.findViewById(R.id.lv_returnBook);
        TextView tv_no_book = view.findViewById(R.id.tv_no_book);
        tv_no_book.setText("You didn't borrow any book..");


        readData(new FireStoreCallback() {
            @Override
            public void onCallback(List<BorrowHistory> mBorrowHistoryList) {

                for (BorrowHistory borrowHistory : mBorrowHistoryList)
                {
                    if (borrowHistory.getUserId().equals(id))
                    {
                        bookList.add(borrowHistory);
                    }
                }

                Collections.sort(bookList);

                if (bookList.size() != 0)
                {
                    listView.setVisibility(View.VISIBLE);
                    tv_no_book.setVisibility(View.GONE);
                    listView.setAdapter(new ListAdapter((ArrayList<BorrowHistory>) bookList, getContext(), "UserBorrowHistoryFragment"));
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
                readData(new FireStoreCallback() {
                    @Override
                    public void onCallback(List<BorrowHistory> mBorrowHistoryList) {

                        for (BorrowHistory borrowHistory : mBorrowHistoryList)
                        {
                            if (borrowHistory.getUserId().equals(id))
                            {
                                bookList.add(borrowHistory);
                            }
                        }

                        Collections.sort(bookList);

                        if (bookList.size() != 0)
                        {
                            listView.setVisibility(View.VISIBLE);
                            tv_no_book.setVisibility(View.GONE);
                            listView.setAdapter(new ListAdapter((ArrayList<BorrowHistory>) bookList, getContext(), "UserBorrowHistoryFragment"));
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

    private void readData(UserBorrowHistoryFragment.FireStoreCallback fireStoreCallback)
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

                    fireStoreCallback.onCallback(borrowHistoryList);
                }
            }
        });
    }
}
