package com.cloudiera.collegeconexion.LogIn;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.Home.HomeActivity;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Utils.CheckInputs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;


public class LogInActivity extends AppCompatActivity {

    private Context mContext = LogInActivity.this;
    private static final String TAG = "LogInActivity";

    // FireBase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserDatabase;

    // Input Variables
    private EditText mEmail, mPassword;
    private TextView mForgotPassword, mSignUp;
    private ProgressDialog mProgressDialog;
    private RelativeLayout mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //Prevent keyboard to open when activity is started
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //Firebase Initialize
        mAuth = FirebaseAuth.getInstance();

        // Get Input From the User
        mEmail = (EditText) findViewById(R.id.emailInput);
        mPassword = (EditText) findViewById(R.id.passwordInput);
        mForgotPassword = (TextView) findViewById(R.id.forgotPassword);
        mSignUp = findViewById(R.id.newAccount);
        mProgressDialog = new ProgressDialog(mContext);
        mProgress = findViewById(R.id.progress_login);

        init();
        setForgotPassword();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_verified_user));

    }

    /**
     * Setup the method to set forogt password for the user
     */
    private void setForgotPassword() {

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, ForgotPasswordActivity.class);
                startActivity(i);
            }
        });

    }

    /**
     * Process on The log In
     */
    private void init() {

        // Initialise the log in Button
        final TextView btnLogIn = (TextView) findViewById(R.id.logInButton);

        //  SETUP SUBMIT BUTTON
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressLogIn();
            }
        });

        //  ENABLE ENTER BUTTON ON KEYBOARD TO CALL SUBMIT BUTTON
        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Log.i(TAG,"Enter pressed");
                    progressLogIn();

                }
                return false;
            }
        });

        //  SETUP SIGN UP BUTTON
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(mContext, NewAccountActivity.class));

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



    private void progressLogIn(){
        String email = mEmail.getText().toString();

        if (CheckInputs.isEmailValid(email)) {

            Log.i("Email :: ", email);

            String password = mPassword.getText().toString();
            Log.i("Password :: ", password);

            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {

                Toast.makeText(mContext, "You Must Fill Out The Fields", Toast.LENGTH_SHORT).show();

            } else {

                mProgress.setVisibility(View.VISIBLE);

//                        mProgressDialog.setTitle("Signing In ");
//                        mProgressDialog.setMessage("Please wait while we load your data");
//                        mProgressDialog.setCanceledOnTouchOutside(false);
//                        mProgressDialog.show();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                if (!task.isSuccessful()) {

                                    Log.w(TAG, "signInWithEmail:failed", task.getException());

                                    Toast.makeText(LogInActivity.this, R.string.auth_failed,
                                            Toast.LENGTH_LONG).show();
                                    mProgress.setVisibility(View.GONE);
                                    mProgressDialog.dismiss();

                                } else {

                                    Log.w(TAG, "signInWithEmail: Logged In successful");

                                    final String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                    final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                    mUserDatabase.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(userId)) {

                                                mUserDatabase.child(userId).child("device_token").setValue(deviceToken)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {

                                                                    Log.d(TAG, "onComplete: Success ! Email is Verified");
                                                                    mProgress.setVisibility(View.GONE);
                                                                    Intent intent = new Intent(LogInActivity.this, HomeActivity.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                    startActivity(intent);

                                                                } else {

                                                                    Log.d(TAG, "onComplete: Something Went Wrong ");
                                                                }
                                                            }
                                                        });
                                            } else {
                                                Log.d(TAG, "onDataChange:  Intent to Home Activity");
                                                mProgress.setVisibility(View.GONE);
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
        } else {
            mEmail.setError("Email should be in right format");
        }

    }

}
