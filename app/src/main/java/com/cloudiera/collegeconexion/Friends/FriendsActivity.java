package com.cloudiera.collegeconexion.Friends;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.CollegeConexion;
import com.cloudiera.collegeconexion.LogIn.EntryActivity;
import com.cloudiera.collegeconexion.Navigation.AccountSettingsActivity;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Talks.SearchUserActivity;
import com.cloudiera.collegeconexion.Utils.BottomNavigationViewHelper;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.cloudiera.collegeconexion.Utils.SectionPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class FriendsActivity extends AppCompatActivity implements ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = "FriendsActivity";
    private Context mContext = FriendsActivity.this;
    private static final int ACTIVITY_NUM = 2;
    private String userId;

    private TextView mColleguesLabel;
    private TextView mFollowersLabel;
    private TextView mFollowingsLabel;

    private ViewPager mPager;

    private static final int TIME_DELAY = 2000;
    private static long back_pressed;

    // FireBase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);


        checkConnection();
        mColleguesLabel = (TextView)findViewById(R.id.allUsers);
        mFollowersLabel = (TextView)findViewById(R.id.followers_view);
        mFollowingsLabel = (TextView)findViewById(R.id.followings_view);
        mPager = (ViewPager)findViewById(R.id.friendsActivityViewpager);



        setupFireBaseAuth();
        //setupBottomNavigation();
        setupToolbar();
        setupTabs();
        myRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_verified_user)).child(mAuth.getCurrentUser().getUid());
        myRef.keepSynced(true);

    }

    /**
     * Setup tabs for the friends activity
     */
    private void setupTabs(){
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new AddFriendsFragment()); // Index 0
        adapter.addFragment(new Followings());  // Index 1
        adapter.addFragment(new Followers()); //Index 2
        mPager.setAdapter(adapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onPageSelected(int position) {
               changeTabs(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mColleguesLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(0);
            }
        });
        mFollowersLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(1);
            }
        });
        mFollowingsLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(2);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void changeTabs(int position) {

        if(position == 0 ){

            mColleguesLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tabselected));
            mColleguesLabel.setTextSize(19);

            mFollowersLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tab_not_selected));
            mFollowersLabel.setTextSize(16);

            mFollowingsLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tab_not_selected));
            mFollowingsLabel.setTextSize(16);
        }
        if(position == 1 ){

            mColleguesLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tab_not_selected));
            mColleguesLabel.setTextSize(16);

            mFollowersLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tabselected));
            mFollowersLabel.setTextSize(19);

            mFollowingsLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tab_not_selected));
            mFollowingsLabel.setTextSize(16);
        }
        if(position == 2 ){

            mColleguesLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tab_not_selected));
            mColleguesLabel.setTextSize(16);

            mFollowersLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tab_not_selected));
            mFollowersLabel.setTextSize(16);

            mFollowingsLabel.setTextColor(ContextCompat.getColor(mContext,R.color.tabselected));
            mFollowingsLabel.setTextSize(19);
        }


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
//    private void setupBottomNavigation(){
//        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation);
//        if(bottomNavigationViewEx!=null){
//            BottomNavigationViewHelper.setupBottomNavigation(bottomNavigationViewEx);
//            BottomNavigationViewHelper.enableNavigation(mContext,bottomNavigationViewEx);
//            Menu menu = bottomNavigationViewEx.getMenu();
//            MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
//            menuItem.setChecked(true);
//        }
//        else{
//            Log.i("Object Is Null ::","What is the Reason");
//        }
//    }


     /*
     ** -------------------------------   FIREBASE SETUP SECTION   ----------------------------
     */

    /**
     * check whether user is logged in or not
     */
    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking user status of log in");
        if (user == null) {
            Intent intent = new Intent(mContext, EntryActivity.class);
            startActivity(intent);
        }else{
              myRef.child("online").setValue(true);
        }
    }
    /*
     ** Setup firebase auth object
     */
    private void setupFireBaseAuth() {
        Log.d(TAG, "setupFireBaseAuth: Setting up Firebase");
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check Whether is logged in or not
                checkCurrentUser(user);
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
        mAuth.addAuthStateListener(mAuthListener);
        checkCurrentUser(mAuth.getCurrentUser());

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);

        }

        myRef.child("online").setValue(ServerValue.TIMESTAMP);

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
