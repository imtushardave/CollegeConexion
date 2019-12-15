package com.cloudiera.collegeconexion.LogIn;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cloudiera.collegeconexion.CollegeConexion;
import com.cloudiera.collegeconexion.Home.HomeActivity;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.cloudiera.collegeconexion.Utils.Database.UserDatabaseHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;


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

        //SPLASH SCREEN

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                //IF USER IS NOT SIGNNED IN THEN SEND HIM TO LOGIN ACTIVITY
                if(FirebaseAuth.getInstance().getCurrentUser()==null){
                    startActivity(new Intent(mContext, LogInActivity.class));
                    finish();

                }else{

                    // IF USER IS SIGNNED IN THEN SEND HIM TO HOME ACIVITY
                    if(isOnline()){

                        //IF UESER IS CONNECTED TO INTERNET, AUTO RE-SIGNIN
                        final UserDatabaseHelper userHelper = new UserDatabaseHelper(mContext);
                        Cursor rs = userHelper.getData(1);
                        rs.moveToFirst();

                        String email = rs.getString(rs.getColumnIndex(UserDatabaseHelper.CONTACTS_COLUMN_EMAIL));
                        String pass = rs.getString(rs.getColumnIndex(UserDatabaseHelper.CONTACTS_COLUMN_PASS));

                        if (!rs.isClosed()) {
                            rs.close();
                        }

                        // FIRST SIGNING OUT FROM THE LAST SESSION
                        FirebaseAuth.getInstance().signOut();

                        // AUTO - RESIGNIN PROCESS STARTS HERE
                        FirebaseAuth.getInstance()
                                .signInWithEmailAndPassword(email,pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {

                                        // SEND USER TO THE HOME ACTIVITY
                                        Intent intent = new Intent(mContext, HomeActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        userHelper.deleteContact(1);
                                        Toast.makeText(mContext, "Authentication Revoked", Toast.LENGTH_SHORT).show();

                                        // SEND USER TO LOGIN SCREEN
                                        Intent intent = new Intent(mContext, LogInActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }
                                });

                    }else{

                        // SEND USER TO HOME ACTIVITY, IF USER IS NOT CONNECTED TO INTERNET
                        Intent intent = new Intent(mContext, HomeActivity.class);
                        startActivity(intent);
                        finish();

                    }

                }

            }
        },1800);

    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }




}
