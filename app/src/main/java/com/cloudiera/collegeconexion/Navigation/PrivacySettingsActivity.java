package com.cloudiera.collegeconexion.Navigation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.CollegeConexion;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PrivacySettingsActivity extends AppCompatActivity  implements ConnectionReceiver.ConnectionReceiverListener {

    private static final String TAG = "PrivacySettingsActivity";
    private Context mContext = PrivacySettingsActivity.this;

    // Views for showing information to the uesr
    TextView mRollNo,mDob,mGender,mPhoneNumber,mEmail;

    //Switch for knowing action
    Switch mRollNoSwitch,mDobSwitch,mGenderSwitch,mPhoneNumberSwitch,mEmailSwitch;

    //Save Changes Button
    Button saveChange;

    //Firebase setup
    FirebaseAuth mAuth;
    DatabaseReference mUserDatabase,mPrivacySettingDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);
        checkConnection();
        mAuth = FirebaseAuth.getInstance();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_verified_user));
        mUserDatabase.keepSynced(true);

        mPrivacySettingDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_privacy_settings));
        mPrivacySettingDatabase.keepSynced(true);

        setupWidgets();
        setValues();

        saveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSwitchStatus();
            }
        });


    }

    /*
     Setup the widgets for the operations
     */
    private void setupWidgets(){

        mRollNo = (TextView)findViewById(R.id.roll_no_privacy);
        mDob = (TextView)findViewById(R.id.dob_privacy);
        mGender = (TextView)findViewById(R.id.gender_privacy);
        mPhoneNumber = (TextView)findViewById(R.id.phoneNumber_privacy);
        mEmail = (TextView)findViewById(R.id.email_privacy);

        mRollNoSwitch = (Switch)findViewById(R.id.roll_no_switch);
        mDobSwitch = (Switch)findViewById(R.id.dob_switch);
        mGenderSwitch = (Switch)findViewById(R.id.gender_switch);
        mPhoneNumberSwitch = (Switch)findViewById(R.id.phoneNumber_switch);
        mEmailSwitch = (Switch)findViewById(R.id.email_switch);

        saveChange = (Button)findViewById(R.id.saveButton);

    }

    /*
     Set Values to the fields
     */
    private void setValues(){

        mUserDatabase.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String rollno = dataSnapshot.child("roll_no").getValue().toString();
                String dob = dataSnapshot.child("dob").getValue().toString();
                String gender = dataSnapshot.child("gender").getValue().toString();
                String phoneNumber = dataSnapshot.child("phone_number").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                mRollNo.setText(rollno);
                mDob.setText(dob);
                mGender.setText(gender);
                mPhoneNumber.setText(phoneNumber);
                mEmail.setText(email);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mPrivacySettingDatabase.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Log.d(TAG, "onDataChange: DataSnapShot is :: "+ dataSnapshot);
                    mRollNoSwitch.setChecked(Boolean.parseBoolean(dataSnapshot.child("roll_no").getValue().toString()));
                    mDobSwitch.setChecked(Boolean.parseBoolean(dataSnapshot.child("dob").getValue().toString()));
                    mGenderSwitch.setChecked(Boolean.parseBoolean(dataSnapshot.child("gender").getValue().toString()));
                    mPhoneNumberSwitch.setChecked(Boolean.parseBoolean(dataSnapshot.child("phone_number").getValue().toString()));
                    mEmailSwitch.setChecked(Boolean.parseBoolean(dataSnapshot.child("email").getValue().toString()));

                }else{
                    mRollNoSwitch.setChecked(true);
                    mDobSwitch.setChecked(true);
                    mGenderSwitch.setChecked(true);
                    mPhoneNumberSwitch.setChecked(true);
                    mEmailSwitch.setChecked(true);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getSwitchStatus(){

        final ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setTitle("Saving Changes");
        dialog.setMessage("Please wait ! while we save your changes");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Map updateStatus = new HashMap();
        updateStatus.put("roll_no",mRollNoSwitch.isChecked());
        updateStatus.put("dob",mDobSwitch.isChecked());
        updateStatus.put("gender",mGenderSwitch.isChecked());
        updateStatus.put("phone_number",mPhoneNumberSwitch.isChecked());
        updateStatus.put("email",mEmailSwitch.isChecked());

        mPrivacySettingDatabase.child(mAuth.getCurrentUser().getUid()).updateChildren(updateStatus, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null){
                    Toast.makeText(mContext, "Something went Wrong !\n Try again later", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                finish();
            }
        });


    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("No Internet Connection");
        builder.setMessage("Make sure that you are connected to Internet.");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkConnection();
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if(!isConnected) {
            //show a No Internet Alert or Dialog
            builder.show();
        }else{
            // dismiss the dialog or refresh the activity
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        CollegeConexion.getInstance().setConnectionListener((ConnectionReceiver.ConnectionReceiverListener) this);
    }

    private void checkConnection() {
        boolean isConnected = ConnectionReceiver.isConnected();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("No Internet Connection");
        builder.setMessage("Make sure that you are connected to Internet.");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkConnection();
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if(!isConnected) {
            //show a No Internet Alert or Dialog
            builder.show();
        }
    }

}
