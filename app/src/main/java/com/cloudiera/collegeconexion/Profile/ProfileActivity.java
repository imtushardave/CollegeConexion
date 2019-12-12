package com.cloudiera.collegeconexion.Profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.CollegeConexion;
import com.cloudiera.collegeconexion.Friends.ShowingUserPostsActivity;
import com.cloudiera.collegeconexion.Navigation.AccountSettingsActivity;
import com.cloudiera.collegeconexion.Talks.SearchUserActivity;
import com.cloudiera.collegeconexion.Utils.CheckInputs;
import com.cloudiera.collegeconexion.Models.CollegeProfile;
import com.cloudiera.collegeconexion.Models.StudentProfile;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Utils.BottomNavigationViewHelper;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.cloudiera.collegeconexion.Utils.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity  implements ConnectionReceiver.ConnectionReceiverListener {

    private static final String TAG = "ProfileActivity";
    private Context mContext = ProfileActivity.this;
    private static final int ACTIVITY_NUM = 4;
    private ImageView profilePhoto,collegeLogo;
    private TextView collegeName;
    private TextView mFollowers,mFollowings;
    private TextView profileName, bio, course, branch, roll_no, dob, gender, phone_number, email;

    private static final int TIME_DELAY = 2000;
    private static long back_pressed;

    private FloatingActionButton postsButton;

    // FireBase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        checkConnection();
        mFirebaseMethods = new FirebaseMethods(mContext);
        mProgressDialog = new ProgressDialog(ProfileActivity.this);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Please wait..till loaded");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        setupWidgets();
        setupFireBaseAuth();
        setupToolbar();
       // setupBottomNavigation();

    }


    private void setupWidgets() {
        Log.d(TAG, "setupWidgets: Setting Widgets of Profile Activity");
        bio = (TextView) findViewById(R.id.bio);
        course = (TextView) findViewById(R.id.course);
        branch = (TextView) findViewById(R.id.branch);
        roll_no = (TextView) findViewById(R.id.roll_no);
        dob = (TextView) findViewById(R.id.dob);
        gender = (TextView) findViewById(R.id.gender);
        phone_number = (TextView) findViewById(R.id.phoneNumber);
        email = (TextView) findViewById(R.id.email);
        profileName = (TextView)findViewById(R.id.profileName);
        profilePhoto = (ImageView) findViewById(R.id.profilePhoto);
        collegeLogo = (ImageView)findViewById(R.id.clgLogo);
        collegeName = (TextView)findViewById(R.id.clgNameTextView);
        mFollowers = (TextView)findViewById(R.id.profile_follower_value);
        mFollowings = (TextView)findViewById(R.id.profile_following_value) ;

        ImageView editProfile = (ImageView)findViewById(R.id.edit_profile_direct);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext,EditProfileActivity.class);
                startActivity(i);
            }
        });

        postsButton = (FloatingActionButton)findViewById(R.id.posts);


        Log.d(TAG, "setupWidgets: All widgets setup successfully");
    }

    private void setStudentProfileWidgets(final StudentProfile student){
        Log.d(TAG, "setWidgets:  Setting With data retrieved from firbase ");

        if(!student.getProfile_image().equals("")){
            Picasso.with(mContext).load(student.getProfile_image()).placeholder(R.drawable.student).networkPolicy(NetworkPolicy.OFFLINE).into(profilePhoto, new Callback() {
                @Override
                public void onSuccess() {
                }
                @Override
                public void onError() {
                    Picasso.with(mContext).load(student.getProfile_image()).placeholder(R.drawable.student).into(profilePhoto);
                }
            });
        }else{
            profilePhoto.setImageResource(R.drawable.student);
        }
        bio.setText(student.getBio());
        String year = CheckInputs.getCourseYear(student.getRoll_no());
        Log.d(TAG, "setStudentProfileWidgets: year of the student is :: " + year);
        if(year!=null){
            if(year.equals(" ")){
                course.setText(student.getCourse());
            }else{
                year = student.getCourse()+" " + year;
                course.setText(year);
            }
        }else{
            course.setText(student.getCourse());
        }

        postsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext,ShowingUserPostsActivity.class);
                i.putExtra("user_id",mAuth.getCurrentUser().getUid());
                i.putExtra("profile_name",student.getProfile_name());
                startActivity(i);
            }
        });

        branch.setText(student.getBranch());
        roll_no.setText(student.getRoll_no());
        dob.setText(student.getDob());
        gender.setText(student.getGender());
        phone_number.setText(String.valueOf(student.getPhone_number()));
        email.setText(student.getEmail());
        profileName.setText(student.getProfile_name());
        Log.d(TAG, "setWidgets: student Data :: "+student.toString());
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("followings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long following =  dataSnapshot.child(mAuth.getCurrentUser().getUid()).getChildrenCount();
                if(following != 0){
                    mFollowings.setText(String.valueOf(following));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        myRef.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long followers =  dataSnapshot.child(mAuth.getCurrentUser().getUid()).getChildrenCount();
                if(followers != 0){
                    mFollowers.setText(String.valueOf(followers));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    private void setCollegeProfileWidgets(final CollegeProfile college){
        Log.d(TAG, "setWidgets:  Setting With data retrieved from firebase " );
        collegeName.setText(college.getCollege_Name());
        Picasso.with(mContext).load(college.getCollege_logo()).placeholder(R.drawable.rtulogo).networkPolicy(NetworkPolicy.OFFLINE).into(collegeLogo, new Callback() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onError() {
                Picasso.with(mContext).load(college.getCollege_logo()).placeholder(R.drawable.rtulogo).into(collegeLogo);
            }
        });
        Log.d(TAG, "setWidgets: college Data :: "+college.toString());
    }

    /**
     * Setup the Top Toolbar
     */
    private void setupToolbar() {
        Log.d(TAG, "setupToolbar: Setting up top toolbar ");
        /**
         *  Toolbar setup
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.top_toolbar);
        setSupportActionBar(toolbar);
        ImageView menu = (ImageView)findViewById(R.id.top_toolbar_menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: sending Intent to account setting activity");
                Intent intent = new Intent(mContext,AccountSettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                Log.d(TAG, "onClick: setup intent");
                startActivity(intent);
            }
        });

        TextView search = (TextView)findViewById(R.id.search_top_toolbar);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, SearchUserActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });
    }

    /**
     * Setup the Bottom Navigation Bar
     */
    private void setupBottomNavigation() {
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation);
        if (bottomNavigationViewEx != null) {
            BottomNavigationViewHelper.setupBottomNavigation(bottomNavigationViewEx);
            BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
            Menu menu = bottomNavigationViewEx.getMenu();
            MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
            menuItem.setChecked(true);
        } else {
            Log.i("Object Is Null ::", "What is the Reason");
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

                Log.d(TAG, "onDataChange: DataSnapshots is "+ dataSnapshot);

                // Retrieve user profile information from firebase database
                StudentProfile studentProfile = mFirebaseMethods.getStudentProfile(dataSnapshot);

                // Retrieve college profile information from firebase database
                setCollegeProfileWidgets(mFirebaseMethods.getCollegeProfile(dataSnapshot,studentProfile.getCollege_id()));

                setStudentProfileWidgets(studentProfile);
                mProgressDialog.dismiss();

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

    @Override
    public void onBackPressed() {
        if (back_pressed + TIME_DELAY > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), "Press once again to exit!",
                    Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();
    }

}
