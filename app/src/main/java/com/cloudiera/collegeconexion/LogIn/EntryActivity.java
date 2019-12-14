package com.cloudiera.collegeconexion.LogIn;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cloudiera.collegeconexion.CollegeConexion;
import com.cloudiera.collegeconexion.Home.HomeActivity;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.google.firebase.auth.FirebaseAuth;


@SuppressWarnings("VisibleForTests")
public class EntryActivity extends AppCompatActivity {

    private static final String TAG = "EntryActivity";
    // Activity Context
    private Context mContext = EntryActivity.this;

    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        mAuth = FirebaseAuth.getInstance();

        // IF USER IS SIGNNED IN THEN SEND HIM TO HOME ACIVITY
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(mContext, HomeActivity.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(mContext, LogInActivity.class);
            startActivity(intent);
            finish();
        }

    }





}
