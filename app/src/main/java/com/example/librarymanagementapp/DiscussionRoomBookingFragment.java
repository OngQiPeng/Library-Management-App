package com.example.librarymanagementapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DiscussionRoomBookingFragment extends Fragment implements View.OnClickListener{

    private EditText date;
    private Button btnChooseTime1, btnChooseTime2, btnChooseTime3, btnChooseTime4, btnChooseTime5;
    private Button btnChooseRoom1, btnChooseRoom2, btnChooseRoom3, btnChooseRoom4, btnChooseRoom5;
    private TextView chosenTime, chosenRoom;
    private Button btnBook;
    long number = 1;

    Booking booking;
    DatabaseReference reff = FirebaseDatabase.getInstance().getReference("Booking");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discussion_room_booking, container, false);

        date = (EditText)view.findViewById(R.id.etDate);
        chosenTime = (TextView)view.findViewById(R.id.tvBookTime);
        chosenRoom = (TextView)view.findViewById(R.id.tvBookRoom);

        btnChooseTime1 = (Button)view.findViewById(R.id.btnTime1);
        btnChooseTime1.setOnClickListener(this);
        btnChooseTime2 = (Button)view.findViewById(R.id.btnTime2);
        btnChooseTime2.setOnClickListener(this);
        btnChooseTime3 = (Button)view.findViewById(R.id.btnTime3);
        btnChooseTime3.setOnClickListener(this);
        btnChooseTime4 = (Button)view.findViewById(R.id.btnTime4);
        btnChooseTime4.setOnClickListener(this);
        btnChooseTime5 = (Button)view.findViewById(R.id.btnTime5);
        btnChooseTime5.setOnClickListener(this);

        btnChooseRoom1 = (Button)view.findViewById(R.id.btnRoom1);
        btnChooseRoom1.setOnClickListener(this);
        btnChooseRoom2 = (Button)view.findViewById(R.id.btnRoom2);
        btnChooseRoom2.setOnClickListener(this);
        btnChooseRoom3 = (Button)view.findViewById(R.id.btnRoom3);
        btnChooseRoom3.setOnClickListener(this);
        btnChooseRoom4 = (Button)view.findViewById(R.id.btnRoom4);
        btnChooseRoom4.setOnClickListener(this);

        btnBook = (Button)view.findViewById(R.id.btnBookNow);

        booking = new Booking();

        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    number += snapshot.getChildrenCount() ;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                booking.setName(getArguments().getString("username"));
                booking.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                booking.setDate(date.getText().toString());
                booking.setTime(chosenTime.getText().toString());
                booking.setRoom(chosenRoom.getText().toString());

                reff.child("Booking " + number).setValue(booking);

                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();

                Toast.makeText(getActivity(), "Booking Created Successfully!! ", Toast.LENGTH_SHORT).show();

            }
        });


        date.setInputType(InputType.TYPE_NULL);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog(date);
            }
        });


        return view;
    }

    private void showDateDialog(final EditText date) {
        final Calendar calendar=Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MM-yy");
                date.setText(simpleDateFormat.format(calendar.getTime()));

            }
        };

        new DatePickerDialog(getActivity(),dateSetListener,calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnTime1:
                chosenTime.setText("8:00 - 10:00 AM");
                break;

            case R.id.btnTime2:
                chosenTime.setText("10:00 - 12:00 PM");
                break;

            case R.id.btnTime3:
                chosenTime.setText("12:00 - 2:00 PM");
                break;

            case R.id.btnTime4:
                chosenTime.setText("2:00 - 4:00 PM");
                break;

            case R.id.btnTime5:
                chosenTime.setText("4:00 - 6:00 PM");
                break;

            case R.id.btnRoom1:
                chosenRoom.setText("Discussion Room 1");
                break;

            case R.id.btnRoom2:
                chosenRoom.setText("Discussion Room 2");
                break;

            case R.id.btnRoom3:
                chosenRoom.setText("Discussion Room 3");
                break;

            case R.id.btnRoom4:
                chosenRoom.setText("Discussion Room 4");
                break;

        }
    }


}
