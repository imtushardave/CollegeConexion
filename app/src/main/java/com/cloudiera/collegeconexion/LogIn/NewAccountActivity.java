package com.cloudiera.collegeconexion.LogIn;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.cloudiera.collegeconexion.CollegeConexion;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.cloudiera.collegeconexion.Home.HomeActivity;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Utils.CheckInputs;
import com.cloudiera.collegeconexion.Utils.FirebaseMethods;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import id.zelory.compressor.Compressor;

import static android.R.attr.data;

@SuppressWarnings("VisibleForTests")
public class NewAccountActivity extends AppCompatActivity implements ConnectionReceiver.ConnectionReceiverListener {
    
    private Context mContext = NewAccountActivity.this;
    private static final String TAG = "NewAccountActivity";

    // FireBase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods fireBaseMethods;
    private DatabaseReference myRef,mUserDatabase;

    private Button uploadButton,proceedButton;
    private ImageView mCollegeIdImage;
    private String college,name,email,password,course,branch,rollNo,gender,dob;
    private EditText mRollNo,mName,mEmail,mPassword;
    private EditText mDobField;
    private Spinner courseSpinner,branchSpinner,genderSpinner;
    private Spinner mCollegeId;
    private Uri resultImageUri = null;
    private int check = 0;

    private ProgressDialog progressDialog;
    private boolean rollNoExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        // SETUP THE PROGRESS DIALOG AND FIREBASE METHODS FOR ACCOUNT CREATION
        fireBaseMethods = new FirebaseMethods(mContext);
        progressDialog = new ProgressDialog(mContext);

        // HIDE THE KEYBOARD ON OPENING ACTIVITY
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //DIALOG TO INFORM USERS ABOUT ACCOUNT CREATION
        Dialog myDialog = new Dialog(mContext);
        myDialog.setContentView(R.layout.layout_new_account_popup);
        myDialog.show();

        //INITIALIZE THE WIDGETS FOR THE LAYOUT
        setupFireBaseAuth();                         // SETUP FIREBASE AUTHENTICATION
        initSetupWidgets();                          //SETUP WIDGETS

    }



    /**
     * Setup the signup Form
     */
    public void setupSignUpForm()
    {
        // SETUP FOR GENDER SPINNER IN SIGN UP FORM
        ArrayList<String> genderList = new ArrayList<>();
        genderList.add("Male");
        genderList.add("Female");
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, genderList);
        genderSpinner.setAdapter(genderAdapter);

        // SETUP FOR COLLEGES SPINNER IN SIGN UP FORM
        ArrayList<String> COLLEGES = new ArrayList<String>();
        COLLEGES.add("RSE001- University Teaching Department, RTU Kota");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, COLLEGES);
        mCollegeId  = (Spinner) findViewById(R.id.registerCollegeIdSpinner);
        mCollegeId.setAdapter(adapter);

        // SETUP FOR COURSE SPINNER IN SIGN UP FORM
        ArrayList<String> courseList = new ArrayList<>();
        courseList.add("B.Tech");
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, courseList);
        courseSpinner.setAdapter(adapter1);

        // SETUP FOR BRANCH SPINNER IN SIGN UP FORM
        ArrayList<String> branchList = new ArrayList<>();
        branchList.add("Aeronautical Engineering");
        branchList.add("Civil Engineering");
        branchList.add("Computer Science Engineering");
        branchList.add("EC Engineering");
        branchList.add("Electrical Engineering");
        branchList.add("EIC Engineering");
        branchList.add("Information Technology");
        branchList.add("Mechanical Engineering");
        branchList.add("P&I Engineering");
        branchList.add("Petrochemical Engineering");
        branchList.add("Petroleum Engineering");
        ArrayAdapter<String> branchAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, branchList);
        branchSpinner.setAdapter(branchAdapter);

        // SETUP FOR DATE FIELD IN SIGN UP FORM
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

        Date maxDate = new Date();
        Date minDate = new Date();
        try {
            maxDate = sdf.parse("31/12/2005");
            minDate = sdf.parse("01/01/1970");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long timeMilliMax = maxDate.getTime();
        long timeMilliMin = minDate.getTime();

        final DatePickerDialog dpDialog = new DatePickerDialog(this, myDateListener, mYear, mMonth, mDay);
        dpDialog.getDatePicker().setMaxDate(timeMilliMax);
        dpDialog.getDatePicker().setMinDate(timeMilliMin);

        mDobField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dpDialog.show();
            }
        });

    }

    /**
     *  DATE PICKER DIALOG RETURNS VALUES HERE
     */
    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int year, int monthOfYear, int dayOfMonth) {

            Log.e("onDateSet()", "arg0 = [" + arg0 + "], year = [" + year + "], monthOfYear = [" + monthOfYear + "], dayOfMonth = [" + dayOfMonth + "]");

            Calendar myCalendar = Calendar.getInstance();
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String myFormat = "dd/MM/yyyy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

            mDobField.setText(sdf.format(myCalendar.getTime()));
        }
    };


    /**
     * SETUP WIDGETS
     */
    private void initSetupWidgets(){

        Log.d(TAG, "initSetupWidgets:  Initializing all the widgets");

        uploadButton = (Button)findViewById(R.id.uploadCollegeIdButton);
        mDobField = (EditText) findViewById(R.id.dobField);
        proceedButton = (Button)findViewById(R.id.signUpButton);
        mCollegeIdImage = (ImageView) findViewById(R.id.showingCollegeId);
        mRollNo = (EditText)findViewById(R.id.rollnoSignUp);
        mName = (EditText)findViewById(R.id.nameSignUp);
        mEmail = (EditText)findViewById(R.id.emailSignUp);
        mPassword = (EditText)findViewById(R.id.passwordSignUp);
        courseSpinner = (Spinner)findViewById(R.id.courseSpinnerSignUp);
        branchSpinner = (Spinner)findViewById(R.id.branchSignUpSpinner);
        genderSpinner = (Spinner)findViewById(R.id.genderSignUpSpinner);
        mCollegeId = (Spinner)findViewById(R.id.registerCollegeIdSpinner);

        setupSignUpForm();        // SETUP THE SIGN UP FORM
        setupButtons();          // SETUP THE BUTTON
        getWidgetsData();         // GET DATA FROM WIDGETS

    }


    /**
     * SETUP THE UPLOAD COLLEGE ID CARD BUTTON
     */
    private void setupButtons(){

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick:  Starting Chooser ");

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        Log.d(TAG, "onClick: Starting chooser for Gallery activity");
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start((Activity) mContext);
                    }
                } else {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start((Activity) mContext);
                }
            }
        });

    }

    /**
     *
     * @param name        // FULL NAME OF THE USER
     * @param email       // EMAIL OF THE USER
     * @param password    // PASSWORD OF THE USER
     * @return            // RESULT WHETHER INPUTS ARE NULL OR NOT
     */
    private boolean checkInput(String name,String email,String password){

        Log.d(TAG, "checkInput: Checking Inputs Whether they are null or not");

        if(email.equals("")||name.equals("")||password.equals("")){
            Toast.makeText(mContext, "All Field Must be Filled Out", Toast.LENGTH_SHORT).show();
            return false;
        }
        return  true;
    }



    /**
     * Get Value and Proceed to create new account of the user
     */
    private void getWidgetsData(){

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.setTitle("Reserving Space For You");
                progressDialog.setMessage("Creating Account");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Log.d(TAG, "onClick: Attempting to create New account for the user");

                Log.d(TAG, "onClick: Checking for the inputs first");

                college = mCollegeId.getSelectedItem().toString();
                college = college.substring(0,6);
                college = college.toLowerCase();

                name = mName.getText().toString();
                name = CheckInputs.capitalString(name);
                email = mEmail.getText().toString();
                password = mPassword.getText().toString();

                // CHECK FOR PASSWORD STRENGTH
                if(password.length()>= 6){
                    if(CheckInputs.isEmailValid(email)){

                        if(checkInput(email,name,password)){

                            course = courseSpinner.getSelectedItem().toString();
                            branch = branchSpinner.getSelectedItem().toString();
                            gender = genderSpinner.getSelectedItem().toString();

                            dob = mDobField.getText().toString();
                            rollNo = mRollNo.getText().toString();

                            progressDialog.setMessage("Verifying Roll No.");

                            if(CheckInputs.isRollNoValid(rollNo)){

                                Log.d(TAG, "onDataChange: Checking For Roll Numbers In Database");
                                Log.d(TAG, "isRollNoExist: Checking Roll number in Database");

                                // REFER TO THE VERIFIED USER DATABASE TO CHECK WHETHER ROLL NUMBER EXIST OR NOT
                                DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_verified_user));
                                mUserDatabase.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        Log.d(TAG, "onDataChange:  DATASNAPSHOT "+ dataSnapshot);

                                        rollNoExist = false;

                                        // LOOP THROUGH THE DATABASE TO CHECK WHETHER ROLL NO EXISTS OR NOT
                                        for(DataSnapshot db : dataSnapshot.getChildren()){
                                            if(db.child("roll_no").getValue().toString().equals(rollNo)){
                                                Log.d(TAG, "onDataChange: Roll number found in database ");
                                                rollNoExist = true;
                                                break;
                                            }
                                        }
                                        if(rollNoExist){

                                            // ROLL FOUND IN THE DATABASE
                                            mRollNo.setError("Roll No. Already Exist");

                                        }else{

                                            // ROLL NOT FOUND IN THE DATABASE PROCEED WITH DETAILS

                                            if(resultImageUri!=null){

                                                Log.d(TAG, "onDataChange:  Going to Create new account for the user ");

                                                // CREATING NEW ACCOUNT FOR THE USER
                                                mAuth.createUserWithEmailAndPassword(email, password)
                                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                                                                // If sign in fails, display a message to the user. If sign in succeeds
                                                                // the auth state listener will be notified and logic to handle the
                                                                // signed in user can be handled in the listener.
                                                                if (!task.isSuccessful()) {
                                                                    progressDialog.dismiss();
                                                                    if(check == 0){
                                                                        Log.d(TAG, "onComplete: TASK RESULT "+ task.getResult());
                                                                        Toast.makeText(mContext, R.string.auth_failed,
                                                                                Toast.LENGTH_SHORT).show();
                                                                        check = 1;
                                                                    }
                                                                } else if (task.isSuccessful()) {
                                                                    Log.d(TAG, "onComplete: Authstate Changed :: " + mAuth.getCurrentUser().getUid());
                                                                }

                                                            }
                                                        });
                                            }else{
                                                progressDialog.dismiss();
                                                Toast.makeText(mContext, "Upload Your College Id", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });

                            }else{
                                progressDialog.dismiss();
                                mRollNo.setError("Invalid Roll number");
                            }
                        }
                    }else{
                        progressDialog.dismiss();
                        mEmail.setError("Email should be in right format");
                    }
                }else {
                    progressDialog.dismiss();
                    mPassword.setError(" Password should be of at least 6 characters");
                }


            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                resultImageUri = result.getUri();
                mCollegeIdImage.setImageURI(resultImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }


    /*
    **     -------------------------------Firebase Setup ----------------------------
    */

    /*
    ** Setup firebase auth object
    */
    private void setupFireBaseAuth() {
        Log.d(TAG, "setupFireBaseAuth: Setting up Firebase");
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();


        //SETUP THE AUTH STATE LISTENER
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null && check == 0) {
                      check = 1;
                   // USER SIGNED IN SUCCESSFULLY
                    progressDialog.setMessage(" Account Created. Uploading Remaining..");
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    final  String userID = user.getUid();
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("user_images")
                                    .child(userID).child("college_id_image.jpg");
                            progressDialog.setMessage("Uploading College ID");

                            // ATTEMPTING TO COMPRESS COLLEGE ID IMAGE
                            File file = new File(resultImageUri.getPath());
                            File compressedImageFile;
                            try {
                                compressedImageFile = new Compressor(mContext).compressToFile(file);
                                filePath.putFile(Uri.fromFile(compressedImageFile)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                        if(task.isSuccessful()){

                                            String imageUri = task.getResult().getDownloadUrl().toString();
                                            // ADD DATABASE OF THE NEW USER
                                            progressDialog.setMessage("Account Created Successfully !");

                                            fireBaseMethods.addNewUser(college,name,"","Update",course,
                                                    branch,rollNo,dob,gender,"NOT AVAILABLE",email,userID,false);
                                            DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference()
                                                    .child(getString(R.string.dbname_pending_user));
                                            userDatabase.child(userID).child("online").setValue(true);
                                            userDatabase.child(userID).child("id_image").setValue(imageUri);
                                            progressDialog.dismiss();
                                            Intent i = new Intent(mContext,HomeActivity.class);
                                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(i);
                                            finish();
                                        }else{
                                            progressDialog.dismiss();
                                            Toast.makeText(mContext, "Failed to Upload Your Id Card", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        int progressCount = (int) progress;
                                        progressDialog.setMessage("Uploading Image \n" + progressCount + "% done");
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    // USER IS SIGNED OUT
                    Log.i("Firebase :: ", "Sign Out Successful");
                    progressDialog.dismiss();
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

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
