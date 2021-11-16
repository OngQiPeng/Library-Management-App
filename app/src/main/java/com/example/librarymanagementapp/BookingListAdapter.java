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

public class BookingListAdapter extends BaseAdapter implements android.widget.ListAdapter {

    private ArrayList<Booking> list = new ArrayList<Booking>();
    private Context context;
    String booking;

    public BookingListAdapter(ArrayList<Booking> list, Context context) {
        this.list = list;
        this.context = context;
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


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_booking, null);
        }

        TextView tvBookingList = view.findViewById(R.id.tv_bookingList);


        booking = "Discussion Room: &nbsp;  &nbsp;  &nbsp;<b>" + list.get(position).getRoom() +
                "<br></b> Booking Date: &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<b>" + list.get(position).getDate() +
                "<br></b>Booking Time: &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <b>" + list.get(position).getTime() + "</b>";

        tvBookingList.setText(Html.fromHtml(booking, Html.FROM_HTML_MODE_LEGACY));

        tvBookingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("Details")
                        .setMessage(tvBookingList.getText().toString())
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();

            }
        });


        return view;
    }

}