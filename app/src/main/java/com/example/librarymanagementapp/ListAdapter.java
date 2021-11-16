package com.example.librarymanagementapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import androidx.fragment.app.FragmentActivity;

public class ListAdapter extends BaseAdapter implements android.widget.ListAdapter {

    private ArrayList<BorrowHistory> list = new ArrayList<BorrowHistory>();
    private Context context;
    private String fragment;
    String borrow;

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public ListAdapter(ArrayList<BorrowHistory> list, Context context, String fragment) {
        this.list = list;
        this.context = context;
        this.fragment = fragment;
    }

    @Override
    public int getCount() { return list.size(); }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getFragment() { return fragment; }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_book, null);
        }

        TextView tv_borrow = view.findViewById(R.id.tv_borrowList);
        Button return_btn = view.findViewById(R.id.return_btn);

        if (getFragment().equals("ReturnBookFragment"))
        {
            return_btn.setVisibility(View.VISIBLE);
            borrow = "Book Name: &nbsp;  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<b>" + list.get(position).getBookName() +
                    "<br></b> Username: &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <b>" + list.get(position).getUserName() +
                    "<br></b> Borrow DateTime: <b>" + list.get(position).getBorrowDateTime() + "</b>";
        }
        else if (getFragment().equals("UserBorrowHistoryFragment"))
        {
            return_btn.setVisibility(View.GONE);
            borrow = "Book Name: &nbsp;  &nbsp;  &nbsp;  &nbsp;  &nbsp; <b>" + list.get(position).getBookName() +
                    "<br></b>Borrow DateTime: <b>" + list.get(position).getBorrowDateTime() +
                    "<br></b>Return DateTime:  &nbsp;<b>" + list.get(position).getReturnDateTime() + "</b>";
        }

        else if (getFragment().equals("AllUserBorrowHistoryFragment"))
        {
            return_btn.setVisibility(View.GONE);
            borrow = "Book Name: &nbsp;  &nbsp;  &nbsp;  &nbsp;  &nbsp; <b>" + list.get(position).getBookName() +
                    "<br></b> Username: &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<b>" + list.get(position).getUserName() +
                    "<br></b>Borrow DateTime: <b>" + list.get(position).getBorrowDateTime() +
                    "<br></b>Return DateTime:  &nbsp;<b>" + list.get(position).getReturnDateTime() + "</b>";
        }

        tv_borrow.setText(Html.fromHtml(borrow, Html.FROM_HTML_MODE_LEGACY));

        tv_borrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("Details")
                        .setMessage(tv_borrow.getText().toString())
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();

            }
        });

        return_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("Return Book...")
                        .setMessage("Do you want to return this book?\n\n" +
                                tv_borrow.getText().toString())
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String borrowHistoryId = list.get(position).getBookName() + list.get(position).getUserName() + list.get(position).getBorrowDateTime();
                                borrowHistoryId = borrowHistoryId.replaceAll("\\s", "");

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                                sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                                String currentDateTime = sdf.format(new Date());

                                DocumentReference borrowHistoryReference = fStore.collection("Borrow History").document(borrowHistoryId);
                                Map<String, Object> borrowHistory = new HashMap<>();
                                borrowHistory.put("Return dateTime", currentDateTime);
                                borrowHistory.put("Status", "Returned");
                                borrowHistoryReference.update(borrowHistory);

                                DocumentReference userReference = fStore.collection("User").document(list.get(position).getUserId());
                                Map<String, Object> borrowedBook = new HashMap<>();
                                borrowedBook.put("Book borrowed", "-");
                                userReference.update(borrowedBook);

                                DocumentReference bookReference = fStore.collection("Book").document(list.get(position).getBookId());
                                Map<String, Object> borrowedBy = new HashMap<>();
                                borrowedBy.put("BorrowedBy", "-");
                                bookReference.update(borrowedBy);

                                list.remove(position);
                                notifyDataSetChanged();


                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();

            }
        });


        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        if (getCount() == 0)
        {
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle("No book left..")
                    .setMessage("There is no book to be returned.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                    new ReturnBookFragment()).commit();
                        }
                    }).show();
        }
        super.notifyDataSetChanged();
    }
}