package com.cloudiera.collegeconexion.LogIn;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.CollegeConexion;
import com.cloudiera.collegeconexion.Home.HomeActivity;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Utils.CheckInputs;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;


public class LogInActivity extends AppCompatActivity  implements ConnectionReceiver.ConnectionReceiverListener {
    private Context mContext = LogInActivity.this;
    private static final String TAG = "LogInActivity";
    // FireBase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserDatabase;
    // Input Variables
    private EditText mEmail, mPassword;
    private TextView mForgotPassword;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        checkConnection();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
       /*
         * Get Input From the User
         */
        mEmail = (EditText) findViewById(R.id.emailInput);
        mPassword = (EditText) findViewById(R.id.passwordInput);
        mForgotPassword = (TextView)findViewById(R.id.forgotPassword);
        mProgressDialog = new ProgressDialog(mContext);
        setupFireBaseAuth();
        init();
        setForgotPassword();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_verified_user));

    }

    /**
     * Setup the method to set forogt password for the user
     */
    private void setForgotPassword(){

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext,ForgotPasswordActivity.class);
                startActivity(i);
            }
        });


    }

    /**
     * Check Whether the String is Null or nOt
     *
     * @param string to check
     * @return
     */
    private boolean isStringNull(String string) {
        if (string.equals("")) {
            return true;
        } else {
            return false;
        }
    }

      /*
     ** -------------------------------Firebase Setup ----------------------------
     */

    /**
     * Process on The log In
     */
    private void init() {

        // Initialise the log in Button
        final TextView btnLogIn = (TextView) findViewById(R.id.logInButton);
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();
                if(CheckInputs.isEmailValid(email)) {
                    Log.i("Email :: ", email);
                    String password = mPassword.getText().toString();
                    Log.i("Password :: ", password);
                    if (isStringNull(email) && isStringNull(password)) {;
                        Toast.makeText(mContext, "You Must Fill Out The Fields", Toast.LENGTH_SHORT).show();
                    } else {
                            mProgressDialog.setTitle("Signing In ");
                        mProgressDialog.setMessage("Please wait while we load your data");
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.show();
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                        // If sign in fails, display a message to the user. If sign in succeeds
                                        // the auth state listener will be notified and logic to handle the
                                        // signed in user can be handled in the listener.
                                        if (!task.isSuccessful()) {
                                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                                            Toast.makeText(LogInActivity.this, R.string.auth_failed,
                                                    Toast.LENGTH_LONG).show();
                                          mProgressDialog.dismiss();

                                        } else {
                                            Log.w(TAG, "signInWithEmail: Logged In successful");

                                            final String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                            final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                            mUserDatabase.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(userId)){
                                                        mUserDatabase.child(userId).child("device_token").setValue(deviceToken)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        FirebaseUser user = mAuth.getCurrentUser();
                                                                        if(task.isSuccessful()){
                                                                            try {
                                                                                Log.d(TAG, "onComplete: Success ! Email is Verified");
                                                                                mProgressDialog.dismiss();
                                                                                Intent intent = new Intent(LogInActivity.this, HomeActivity.class);
                                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                startActivity(intent);
                                                                            } catch (NullPointerException e) {
                                                                                Log.e(TAG, "onComplete: Null Pointer Exception : " + e.getMessage());
                                                                            }
                                                                        }else{
                                                                            Log.d(TAG, "onComplete: Something Went Wrong ");
                                                                        }
                                                                    }
                                                                });
                                                    }else{
                                                        Log.d(TAG, "onDataChange:  Intent to Home Activity");
                                                        mProgressDialog.dismiss();
                                                        Intent intent = new Intent(LogInActivity.this, HomeActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(intent);
                                                    }

                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });



                                        }


                                    }
                                });
                    }
                }else{
                     mEmail.setError("Email should be in right format");
                }
            }
        });


        /**
         * If the user Logged in then Navigate to the Home Activity and call finish()
         */
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(mContext, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /*
     ** Setup firebase auth object
     */
    private void setupFireBaseAuth() {
        Log.d(TAG, "setupFireBaseAuth: Setting up Firebase");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.i("Firebase ::", "Successful");
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
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
