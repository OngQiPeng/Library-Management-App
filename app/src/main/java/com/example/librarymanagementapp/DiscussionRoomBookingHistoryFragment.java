package com.example.librarymanagementapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class DiscussionRoomBookingHistoryFragment extends Fragment {

    private DatabaseReference reff;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_list, container, false);

        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        TextView tvNoBooking = view.findViewById(R.id.tv_no_booking);
        ListView listView = view.findViewById(R.id.lv_booking);

        tvNoBooking.setText("No booking...");

        reff = FirebaseDatabase.getInstance().getReference().child("Booking");

        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList <Booking> bookingList = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Booking booking = dataSnapshot.getValue(Booking.class);
                    bookingList.add(booking);
                }

                if (bookingList.size() != 0)
                {
                    listView.setVisibility(View.VISIBLE);
                    tvNoBooking.setVisibility(View.GONE);
                    listView.setAdapter(new BookingListAdapter((ArrayList<Booking>) bookingList, getContext()));
                }

                else
                {
                    listView.setVisibility(View.GONE);
                    tvNoBooking.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                reff.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        ArrayList <Booking> bookingList = new ArrayList<>();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            Booking booking = dataSnapshot.getValue(Booking.class);
                            bookingList.add(booking);
                        }

                        if (bookingList.size() != 0)
                        {
                            listView.setVisibility(View.VISIBLE);
                            tvNoBooking.setVisibility(View.GONE);
                            listView.setAdapter(new BookingListAdapter((ArrayList<Booking>) bookingList, getContext()));
                        }

                        else
                        {
                            listView.setVisibility(View.GONE);
                            tvNoBooking.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                pullToRefresh.setRefreshing(false);
            }
        });

        return view;
    }

}