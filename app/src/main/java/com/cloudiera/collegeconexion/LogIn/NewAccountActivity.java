package com.cloudiera.collegeconexion.LogIn;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Utils.CheckInputs;
import com.cloudiera.collegeconexion.Utils.FirebaseMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("VisibleForTests")
public class NewAccountActivity extends AppCompatActivity{

    private Context mContext = NewAccountActivity.this;
    private static final String TAG = "NewAccountActivity";

    // FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private DatabaseReference myRef;

    //XML FIELDS
    private EditText mEmail, mPassword, mRePassword,mFirstName, mLastName, mUsername;
    private Spinner mCollegeSpinner;
    private TextView mSignUp, mTerms, mPrivacy, mSignIn;
    private RelativeLayout mProgress, mStep2;
    private TextView mFinalButton;

    // OTHER VARIABLES
    private String email, password, rePassword, firstname, lastname, username, collegeId;
    private HashMap<String,String> mCollegeMap;
    private FirebaseMethods fireBaseMethods;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        // HIDE THE KEYBOARD ON OPENING ACTIVITY
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // INITIALIZE ALL THE VARIABLES AND FIELDS USED IN THE ACTIVITY
        initializeFields();

        // SETUP CLICK LISTENERS FOR REQUIRED FIELDS
        setupListeners();

    }

    // THIS FUNCTION INITIALIZE ALL THE FIELDS AND VARIABLES
    private void initializeFields() {

        // SETUP FIREBASE VARIABLE
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // SETUP FIREBASE METHODS FOR ACCOUNT CREATION
        fireBaseMethods = new FirebaseMethods(mContext);

        mProgress = findViewById(R.id.progress_signUp);
        mStep2 = findViewById(R.id.rlStep2);

        // XML FIELDS
        mEmail = findViewById(R.id.etEmail);
        mPassword = findViewById(R.id.etPassword);
        mRePassword = findViewById(R.id.etRePassword);
        mFirstName = findViewById(R.id.etFirstname);
        mLastName = findViewById(R.id.etLastname);
        mUsername = findViewById(R.id.etUsername);

        mCollegeSpinner = findViewById(R.id.spiCollegeName);
        mCollegeMap = new HashMap<>();

        mTerms = findViewById(R.id.tvTerms);
        mPrivacy = findViewById(R.id.tvPrivacy);
        mSignIn = findViewById(R.id.tvSignIn);

        mSignUp = findViewById(R.id.tvSignUp);
        mFinalButton = findViewById(R.id.finalButton);

        // GET COLLEGE LIST FROM THE DATABASE
        setupCollegeList();


    }

    // THIS FUNCTION SETUP LISTENERS FOR THE FIELDS
    private void setupListeners() {

        //  SETUP THE SIGNIN BUTTON
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //  ENABLE ENTER BUTTON ON KEYBOARD TO CALL SUBMIT BUTTON
        mRePassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Log.i(TAG,"Enter pressed");

                    email = mEmail.getText().toString().trim();
                    password = mPassword.getText().toString().trim();
                    rePassword = mRePassword.getText().toString().trim();

                    // VALIDATING ALL THE INPUTS
                    if (validateInputs(email, password, rePassword)) {
                        mStep2.setVisibility(View.VISIBLE);
                    }else{
                        mStep2.setVisibility(View.GONE);
                    }

                }
                return false;
            }
        });


        // SETTING ON-CLICK LISTENER FOR SIGN-UP BUTTON
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = mEmail.getText().toString().trim();
                password = mPassword.getText().toString().trim();
                rePassword = mRePassword.getText().toString().trim();

                // VALIDATING ALL THE INPUTS
                if (validateInputs(email, password, rePassword)) {
                    mStep2.setVisibility(View.VISIBLE);
                }else{
                    mStep2.setVisibility(View.GONE);
                }
            }
        });


        // SETTING ON-CLICK LISTENER FOR LET'S CONNECT BUTTON
        mFinalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               prepareRegistration();
            }
        });
    }

    // THIS FUNCTION SETUP COLLEGE LIST FOR SPINNER FROM DATABASE
    private void setupCollegeList() {

        //QUERYING LIST OF COLLEGE PROFILES
        firebaseFirestore.collection("CollegeProfile")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        HashMap<String,String> collegeMap = new HashMap<>();

                        //ADDING ALL COLLEGE NAMES AND ID'S INTO THE HASHMAP
                        for(DocumentSnapshot collegeProfile : queryDocumentSnapshots.getDocuments()){
                            collegeMap.put(collegeProfile.get("name").toString(),collegeProfile.getId());
                        }

                        //SETUP COLLEGE LIST TO THE SPINNER
                        setupCollegeListAdapter(collegeMap);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    //THIS FUNCTION SET COLLGELIST TO THE ADPAPTER
    private void setupCollegeListAdapter(HashMap<String,String> collegeMap) {

        ArrayList<String> collegeList = new ArrayList<>(collegeMap.keySet());
        Log.d(TAG, "setupCollegeListAdapter: " + collegeList);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, collegeList);

        mCollegeMap = collegeMap;
        mCollegeSpinner.setAdapter(adapter);

    }

    // THIS FUNCTION VALIDATES ALL THE INPUTS
    private boolean validateInputs(String email, String password, String rePassword) {

        // CHECK WHETHER THE INPUTS ARE NULL ARE NOT
        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password) && TextUtils.isEmpty(rePassword)) {
            Toast.makeText(mContext, "Fields are Empty!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // CHECK WHETHER THE FORMAT OF EMAIL IS RIGHT OR NOT
        if (!CheckInputs.isEmailValid(email)) {
            Toast.makeText(mContext, "Invalid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        // CHECK IF THE PASSWORD AND RE-PASSWORD ARE SAME
        if (!password.equals(rePassword)) {
            Toast.makeText(mContext, "Password didn't matched!", Toast.LENGTH_LONG).show();
            return false;
        }

        // CHECK THE LENGTH OF THE PASSWORD
        if (password.length() <= 6) {
            mPassword.setError(" Password should be of at least 6 characters");
            return false;
        }

        return true;
    }

    // THIS FUNCTION START'S REGISTRATION PROCESS
    private void startRegistration() {

        // CREATING NEW ACCOUNT FOR THE USER
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {

                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Authstate Changed :: " + mAuth.getCurrentUser().getUid());

                            //STORE THE USERNAME INTO DATABASE
                            Map<String,Object> usernameMap=new HashMap<String, Object>();
                            usernameMap.put("username",username);
                            usernameMap.put("userId", mAuth.getCurrentUser().getUid());

                            firebaseFirestore.collection("Usernames")
                                    .document(username)
                                    .set(usernameMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            //SENDING VERIFICATION EMAIL
                                            task.getResult()
                                                    .getUser()
                                                    .sendEmailVerification()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            String userUid = task.getResult().getUser().getUid();

                                                            //CREATING DATABASE FOR THE USER
                                                            Map<String, Object> userMap = new HashMap<>();
                                                            userMap.put("id", userUid);
                                                            userMap.put("fname", firstname);
                                                            userMap.put("lname", lastname);
                                                            userMap.put("username", username);
                                                            userMap.put("college_Id", collegeId);
                                                            userMap.put("verified", false);
                                                            userMap.put("email", email);
                                                            userMap.put("bio",getString(R.string.default_bio));

                                                            firebaseFirestore.collection("Users")
                                                                    .document(userUid)
                                                                    .set(userMap)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            Toast.makeText(NewAccountActivity.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                                                                            finish();
                                                                            mProgress.setVisibility(View.GONE);


                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            mProgress.setVisibility(View.GONE);
                                                                            Toast.makeText(mContext, "Something Went wrong. Please try again later", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            mProgress.setVisibility(View.GONE);
                                                            Toast.makeText(mContext, "Something Went wrong. Please try again later", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mProgress.setVisibility(View.GONE);
                                            Toast.makeText(mContext, "Something Went wrong. Please try again later", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            mProgress.setVisibility(View.GONE);

                        } else{
                            mProgress.setVisibility(View.GONE);
                            Log.d(TAG, "onComplete: TASK RESULT " + task.getResult());
                            Toast.makeText(mContext, R.string.auth_failed,Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }

    //THIS FUNCTION PREPARE DATA FOR REGISTRATION PROCESS
    private void prepareRegistration(){
        Log.d(TAG, "onClick: Attempting to create New account for the user");

        firstname = mFirstName.getText().toString().trim();
        lastname = mLastName.getText().toString().trim();
        username = mUsername.getText().toString().toLowerCase().trim();
        collegeId = mCollegeMap.get(mCollegeSpinner.getSelectedItem().toString());

        // CHECK WHETHER INPUTS ARE EMPTY ARE NOT
        if(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(firstname) && !TextUtils.isEmpty(lastname)
                && !TextUtils.isEmpty(collegeId)){

            //ENABLE PROGRESS BAR
            mProgress.setVisibility(View.VISIBLE);

            //CHECK FOR THE AVAILABILITY OF THE USERNAME
            firebaseFirestore.collection("Usernames")
                    .document(username)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(!documentSnapshot.exists()){
                                startRegistration();
                            }else{
                                mProgress.setVisibility(View.GONE);
                                Toast.makeText(mContext, "Username Already Exist!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgress.setVisibility(View.GONE);
                            Toast.makeText(mContext, "Something went wrong. Please try again later!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else {
            mProgress.setVisibility(View.GONE);
            Toast.makeText(mContext, "Some Fields are empty!", Toast.LENGTH_SHORT).show();
        }

    }

}
