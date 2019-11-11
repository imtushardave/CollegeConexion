package com.cloudiera.collegeconexion.Navigation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.cloudiera.collegeconexion.CollegeConexion;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.cloudiera.collegeconexion.Resources.AcademicsDataActivity;
import com.cloudiera.collegeconexion.Utils.SectionsStatePagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * Created by User on 6/4/2017.
 */

public class AccountSettingsActivity extends AppCompatActivity  implements ConnectionReceiver.ConnectionReceiverListener {

    private static final String TAG = "AccountSettingsActivity";
    private Context mContext;
    private SectionsStatePagerAdapter pagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout mRelativeLayout;
    FirebaseAuth mAuth;
    private DatabaseReference mCommunityMembersDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsettings);
        mContext = AccountSettingsActivity.this;
        checkConnection();
        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "onCreate: started.");
        mViewPager = (ViewPager) findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayout1);
        setupSettingsList(false);
        setupFragments(false);
        setupSettingsList(true);
        setupFragments(true);
//        mCommunityMembersDatabase = FirebaseDatabase.getInstance().getReference().child("community_members");
//        mCommunityMembersDatabase.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot ds : dataSnapshot.getChildren()){
//                    Log.d(TAG, "onDataChange:  Child :::  " + ds);
//                    if(ds.hasChild(mAuth.getCurrentUser().getUid())){
//                        Log.d(TAG, "onDataChange: hasChild :: true");
//                        String desig = ds.child(mAuth.getCurrentUser().getUid()).child("desig").getValue().toString();
//                        if(desig.equals("admin")|| desig.equals("editor")){
//                            Log.d(TAG, "onDataChange: admin Or editor:: true");
//
//                            break;
//                        }
//                        Log.d(TAG, "onDataChange: hasChild false");
//                    }
//                }
//
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });




        //setup the backarrow for navigating back to "ProfileActivity"
        ImageView backArrow = (ImageView) findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to 'ProfileActivity'");
                finish();
            }
        });
    }

    private void setupFragments(boolean hasCommunity){
        pagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new SettingsFragment(),getString(R.string.settings));// fragment 0
//        if(hasCommunity){
//            pagerAdapter.addFragment(new CommunityListFragment(),getString(R.string.your_communities));
//        }
//        pagerAdapter.addFragment(new CollegeCommunitesFragment(),getString(R.string.college_communities));
        pagerAdapter.addFragment(new BlockListFragment(),getString(R.string.blockList));
        pagerAdapter.addFragment(new HelpUsToImproveFragment(),getString(R.string.feedback)); // fragment 1
        pagerAdapter.addFragment(new AboutFragment(),getString(R.string.about));// fragment2
         pagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out)); //fragment 3
    }

    private void setViewPager(int fragmentNumber){
        mRelativeLayout.setVisibility(View.GONE);
        Log.d(TAG, "setViewPager: navigating to fragment #: " + fragmentNumber);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(fragmentNumber);
        mViewPager.setHorizontalScrollBarEnabled(false);
    }

    private void setupSettingsList(boolean hasCommunity){
        Log.d(TAG, "setupSettingsList: initializing 'Account Settings' list.");
        ListView listView = (ListView) findViewById(R.id.lvAccountSettings);

        ArrayList<String> options = new ArrayList<>();
       // options.add(getString(R.string.edit_profile));
        options.add(getString(R.string.resources));
        options.add(getString(R.string.settings));//fragment 0
//        if(hasCommunity){
//            options.add(getString(R.string.your_communities));
//        }
//        options.add(getString(R.string.college_communities));
        options.add(getString(R.string.blockList));
        options.add(getString(R.string.feedback)); // fragment 1
        options.add(getString(R.string.about)); //fragment 2
        options.add(getString(R.string.sign_out)); //fragment 3

        ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: Navigation as per response");
                    Log.d(TAG, "setViewPager: navigating to fragment #: " + position);
                if(position == 0 ){
                    Intent i = new Intent(mContext, AcademicsDataActivity.class);
                    startActivity(i);
                }else{
                    setViewPager(position-1);
                }
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