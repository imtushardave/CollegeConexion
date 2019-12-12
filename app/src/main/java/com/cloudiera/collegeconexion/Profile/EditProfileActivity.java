package com.cloudiera.collegeconexion.Profile;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.CollegeConexion;
import com.cloudiera.collegeconexion.Models.StudentProfile;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Utils.CheckInputs;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.cloudiera.collegeconexion.Utils.FirebaseMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import id.zelory.compressor.Compressor;

/**
 * Created by HP on 07-Dec-17.
 */
public class EditProfileActivity extends AppCompatActivity implements ConnectionReceiver.ConnectionReceiverListener  {
    private static final String TAG = "EditProfileActivity";
    private Context mContext = EditProfileActivity.this;
    private ImageView mProfilePhoto;
    private EditText  mBio,  mDob, mPhone_number;
    private TextView mUsername;

    private String userId;

    private StudentProfile mStudentProfile;
    private static final int GALLERY_PICK = 1;
    private ProgressDialog mProgressDialog;
    // FireBase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        checkConnection();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mProfilePhoto = (ImageView) findViewById(R.id.editProfileImage);
        mFirebaseMethods = new FirebaseMethods(mContext);
        mImageStorage = FirebaseStorage.getInstance().getReference();
        setupWidgets();
        setupFireBaseAuth();
        enableDatePicker();
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Please wait while we load your profile");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        //back arrow Listener to navigate back to profile activity
        ImageView backArrow = (ImageView) findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating Back to Profile Activity");
                finish();
            }
        });
        // Submit Image Listener
        ImageView submitArrow = (ImageView) findViewById(R.id.submit);
        submitArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick:  Attempting to save changes of profile");
                saveProfileSettings();
            }
        });
        // Submit Button Listner
        Button saveChangeButton = (Button) findViewById(R.id.save_changes_button);
        saveChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick:  Attempting to save changes of profile");
                saveProfileSettings();
            }
        });
        //Set Image for Profile
        TextView changeProfilePic = (TextView) findViewById(R.id.changeProfilePhoto);
        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Starting Gallery to Choose Image");
                Intent gallery_intent = new Intent();
                gallery_intent.setType("image/*");
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery_intent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });
    }

    /**
     * Choosing picture from the gallery to upload it to firebase
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(EditProfileActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.setCancelable(true);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {

                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);

                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                } catch (IOException e) {
                    Log.d(TAG, "onActivityResult:  Exception is Going on ");
                    e.printStackTrace();
                }

                final byte[] thumb_byte = baos.toByteArray();

                StorageReference filePath = mImageStorage.child("user_images")
                        .child(userId).child("profile_image.jpg");

                final StorageReference thumb_filepath = mImageStorage.child("user_images")
                        .child(userId).child("profile_image_thumbnail.jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Profile Image uploaded Successfully");

                            UploadTask.TaskSnapshot taskUri = task.getResult();

                            @SuppressWarnings("VisibleForTests") final String imageUri = taskUri.getStorage().getDownloadUrl().toString();

                            UploadTask uploadTask  = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    UploadTask.TaskSnapshot taskUri = task.getResult();
                                    @SuppressWarnings("VisibleForTests") String thumb_downloadUrl = taskUri.getStorage().getDownloadUrl().toString();

                                    if(task.isSuccessful()){
                                          // Update profile_img_thumb
                                        mStudentProfile.setProfile_img_thumb(thumb_downloadUrl);
                                        //Update profile_image
                                        mStudentProfile.setProfile_image(imageUri);

                                        Log.d(TAG, "saveProfileSettings:  Update profile Image ");

                                        mFirebaseMethods.updateStudentProfile("profile_image", imageUri);
                                        mFirebaseMethods.updateStudentProfile("profile_img_thumb", thumb_downloadUrl);
                                        mProgressDialog.dismiss();
                                        Toast.makeText(mContext, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();


                                    }else{
                                        mProgressDialog.dismiss();
                                        Toast.makeText(mContext, "Uploading Failed", Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });



                        } else {
                            mProgressDialog.dismiss();
                            Toast.makeText(mContext, "Uploading Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    /**
     * Enable the date Picker for the Date of birth
     */
    private void enableDatePicker() {

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

        mDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dpDialog.show();
            }
        });

    }

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
            mDob.setText(sdf.format(myCalendar.getTime()));
        }
    };

    /**
     * Setup the widgets of the edit profile activity
     */
    private void setupWidgets() {
        mUsername = (TextView) findViewById(R.id.profileNameField);
        mBio = (EditText) findViewById(R.id.bioField);
        mDob = (EditText) findViewById(R.id.dobField);
        mPhone_number = (EditText) findViewById(R.id.phoneNumberField);
        mProfilePhoto = (ImageView) findViewById(R.id.editProfileImage);
    }
    /**
     * Set the values to the field in the edit profile activity
     *
     * @param student
     */
    private void setStudentProfileWidgets(final StudentProfile student) {
        Log.d(TAG, "setWidgets:  Setting With data retrieved from firebase ");
        mStudentProfile = student;
        if(!student.getProfile_image().equals("")){
            Picasso.with(mContext).load(student.getProfile_image()).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.student).into(mProfilePhoto, new Callback() {
                @Override
                public void onSuccess() {
                }
                @Override
                public void onError() {
                    Picasso.with(mContext).load(student.getProfile_image()).placeholder(R.drawable.student).into(mProfilePhoto);
                }
            });
        }else{
            mProfilePhoto.setImageResource(R.drawable.student);
        }

        mUsername.setText(student.getProfile_name());
        mBio.setText(student.getBio());
        mDob.setText(student.getDob());
        mPhone_number.setText(student.getPhone_number());
        myRef.keepSynced(false);
        Log.d(TAG, "setWidgets: student Data :: " + student.toString());
        mProgressDialog.dismiss();
    }


    private void saveProfileSettings() {

        final String bio = mBio.getText().toString();
        final String dob = mDob.getText().toString();
        final String phoneNumber = mPhone_number.getText().toString();


        Log.d(TAG, "onDataChange: CURRENT EMAIL : " + mStudentProfile.getEmail());
        // Case 1 : Email is not Changed
        Log.d(TAG, "onDataChange: No Changes in email");

        if (!(mStudentProfile.getBio()).equals(bio)) {
            //Update bio of the user
            Log.d(TAG, "saveProfileSettings:  Update bio ");
            mFirebaseMethods.updateStudentProfile("bio", bio);
        }
        if (!(mStudentProfile.getDob()).equals(dob)) {
            //Update dob
            Log.d(TAG, "saveProfileSettings:  Update dob ");
            mFirebaseMethods.updateStudentProfile("dob", dob);
        }
        if (CheckInputs.isValidMobile(phoneNumber)) {
            if (!(mStudentProfile.getPhone_number()).equals(phoneNumber)) {
                //Update Phone Number
                Log.d(TAG, "saveProfileSettings:  Update Phone Number ");
                mFirebaseMethods.updateStudentProfile("phone_number", phoneNumber);
            }
        } else {
            mPhone_number.setError("Incorrect MobileNumber");
        }
        if (mBio.getError() == null &&
                mDob.getError() == null &&
                mPhone_number.getError() == null ) {

            finish();
        }

    }


   /*
     ** ------------------------------------- Firebase Setup --------------------------------------
     */

    /*
    ** Setup firebase auth object
    */
    private void setupFireBaseAuth() {
        Log.d(TAG, "setupFireBaseAuth: Setting up Firebase");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userId = mAuth.getCurrentUser().getUid();
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

            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Retrieve user profile information from firebase database
                setStudentProfileWidgets(mFirebaseMethods.getStudentProfile(dataSnapshot));
                // Retrieve Image information from firebase database

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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
