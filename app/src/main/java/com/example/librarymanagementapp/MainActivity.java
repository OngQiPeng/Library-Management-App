package com.example.librarymanagementapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String userId = fAuth.getCurrentUser().getUid();
    DocumentReference documentReference = fStore.collection("User").document(userId);
    NavigationView navigationView;
    TextView tv_userName;
    TextView tv_userType;

    private DrawerLayout drawer;

    String name;
    String email;
    String bookBorrowed;
    String userType;
    Bundle args = new Bundle();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);

        tv_userName = (TextView)view.findViewById(R.id.userName);
        tv_userType = (TextView)view.findViewById(R.id.userType);

        readData(new FireStoreCallback() {
            @Override
            public void onCallback(User user)
            {

                if (user.getUserType().equals("Admin"))
                {
                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.admin_menu);
                }


                args.putString("username", user.getName());

                tv_userName.setText(user.getName());
                tv_userType.setText(user.getUserType());

                getIntent().putExtra("user", user);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                readData(new FireStoreCallback() {
                    @Override
                    public void onCallback(User user)
                    {
                        tv_userName.setText(user.getName());
                        args.putString("username", user.getName());
                    }
                });
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) //show profile fragment when opening
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit(); //open profile fragment after user login
            navigationView.setCheckedItem(R.id.nav_profile);
        }

    }


    private interface FireStoreCallback
    {
        void onCallback(User user);
    }

    private void readData(FireStoreCallback fireStoreCallback)
    {
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null)
                    {
                        name = documentSnapshot.getString("Name");
                        email = documentSnapshot.getString("Email");
                        bookBorrowed = documentSnapshot.getString("Book borrowed");
                        userType = documentSnapshot.getString("userType");
                        User user = new User(userId, name, email, bookBorrowed, userType);

                        fireStoreCallback.onCallback(user);

                    }
                }
            }
        });
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        setTitle("LibraryManagementApp");

        switch (item.getItemId())
        {
            case R.id.nav_profile:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
                break;

            case R.id.nav_borrow:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new BorrowFragment()).commit();
                break;

            case R.id.nav_logout:

                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent (getApplicationContext(), LoginRegister.class);
                startActivity(i);
                finish();
                break;

            case R.id.nav_addBook:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AddBookFragment()).commit();
                break;

            case R.id.nav_returnBook:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ReturnBookFragment()).commit();
                break;

            case R.id.nav_history:

                if (tv_userType.getText().toString().equals("Admin"))
                {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new BorrowHistoryTab()).commit();
                }

                else
                {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new UserBorrowHistoryFragment()).commit();
                }

                break;

            case R.id.nav_chatRoom:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ChatRoomFragment()).commit();
                break;

            case R.id.nav_booking:

                DiscussionRoomBookingFragment fragment = new DiscussionRoomBookingFragment();
                fragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        fragment).commit();
                break;

            case R.id.nav_bookingHistory:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DiscussionRoomBookingHistoryFragment()).commit();
                break;

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof ChatFragment)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ChatRoomFragment()).commit();

            setTitle("LibraryManagementApp");
        }

        else if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }

        else
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Quit the app...")
                    .setMessage("Do you want to quit the app?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAffinity(); //close the app
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();

        }

    }


}