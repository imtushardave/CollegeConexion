package com.cloudiera.collegeconexion.Friends;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cloudiera.collegeconexion.CollegeConexion;
import com.cloudiera.collegeconexion.Utils.CheckInputs;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Talks.ChatActivity;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ShowingFriendsProfile extends AppCompatActivity  implements ConnectionReceiver.ConnectionReceiverListener {
    private static final String TAG = "ShowingFriendsProfile";
    private Context mContext = ShowingFriendsProfile.this;

    private String showingUserId;
    private boolean mProcessFollow = false;
    private ImageView mProfileImage;
    private TextView mProfileName, mCourse, mBio, mClgName;
    private ImageView mBackArrow, mFollow, mChat, mOptions,mPosts;

    private ProgressDialog mProgressDialog;
    private LinearLayout rollNoLayout,dobLayout,genderLayout,phoneLayout,emailLayout;
    private RelativeLayout contactLayout;
    //Firebase
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mRootRef,mBlockListDatabase,mPrivacyDatabase;

    private FirebaseUser mCurrentUser;
    private TextView mBranch, mToolbarName;
    private TextView mRollNo, mGender, mDob;
    private TextView mFollowers,mFollowings;
    private TextView mPhoneNumber, mEmail;
    private ImageView mClgLogo;
    private TextView followView,blockView;

    private boolean mProcessBlock = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showing_friends_profile);
        checkConnection();
        showingUserId = getIntent().getStringExtra("user_id");

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_verified_user)).child(showingUserId);
        mDatabaseReference.keepSynced(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        mBlockListDatabase = FirebaseDatabase.getInstance().getReference().child("block_list");
        mBlockListDatabase.keepSynced(true);
        mPrivacyDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_privacy_settings));
        mPrivacyDatabase.keepSynced(true);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mBackArrow = (ImageView) findViewById(R.id.back_arrow_showing_profile);
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mClgName = (TextView) findViewById(R.id.clgNameTextView);
        mClgLogo = (ImageView) findViewById(R.id.clgLogo);
        mProfileImage = (ImageView) findViewById(R.id.showing_profileImage);
        mProfileName = (TextView) findViewById(R.id.showing_profileName);
        mCourse = (TextView) findViewById(R.id.course);
        mBranch = (TextView) findViewById(R.id.branch);
        mToolbarName = (TextView) findViewById(R.id.showing_profile_toolbar_name);
        mBio = (TextView) findViewById(R.id.showing_profileBio);
        mRollNo = (TextView) findViewById(R.id.roll_no);
        mGender = (TextView) findViewById(R.id.gender);
        mDob = (TextView) findViewById(R.id.dob);
        mPhoneNumber = (TextView) findViewById(R.id.phoneNumber);
        mEmail = (TextView) findViewById(R.id.email);
        mFollow = (ImageView) findViewById(R.id.showing_profile_follow);
        followView = (TextView)findViewById(R.id.followText) ;
        blockView = (TextView)findViewById(R.id.blockText);
        mChat = (ImageView) findViewById(R.id.showing_profile_chatbox);
        mOptions = (ImageView) findViewById(R.id.Block);
        mPosts = (ImageView)findViewById(R.id.showing_profile_posts);
        mFollowers = (TextView)findViewById(R.id.showing_profile_follower_value);
        mFollowings = (TextView)findViewById(R.id.showing_profile_following_value) ;
        rollNoLayout = (LinearLayout)findViewById(R.id.rollNoLayout);
        dobLayout = (LinearLayout)findViewById(R.id.dobLayout);
        genderLayout = (LinearLayout)findViewById(R.id.genderLayout);
        phoneLayout = (LinearLayout)findViewById(R.id.phoneNumberLayout);
        emailLayout = (LinearLayout)findViewById(R.id.emailLayout);
        contactLayout = (RelativeLayout)findViewById(R.id.showing_contact_information);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Getting Values from Firebase Database");
                DatabaseReference clgDatabase;

                final String profileName = dataSnapshot.child("profile_name").getValue().toString();
                String course = dataSnapshot.child("course").getValue().toString();
                String branch = dataSnapshot.child("branch").getValue().toString();
                String phoneNumber = dataSnapshot.child("phone_number").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String dob = dataSnapshot.child("dob").getValue().toString();
                String gender = dataSnapshot.child("gender").getValue().toString();
                String rollno = dataSnapshot.child("roll_no").getValue().toString();
                String bio = dataSnapshot.child("bio").getValue().toString();
                final String profileImage = dataSnapshot.child("profile_img_thumb").getValue().toString();
                String clgId = dataSnapshot.child("college_id").getValue().toString();

                clgDatabase = FirebaseDatabase.getInstance().getReference(getString(R.string.dbname_college_profile))
                        .child(clgId);
                clgDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String clgName = dataSnapshot.child("college_name").getValue().toString();
                        final String clgLogo = dataSnapshot.child("college_logo").getValue().toString();
                        mClgName.setText(clgName);
                        Picasso.with(mContext).load(clgLogo).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.rtulogo).into(mClgLogo, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError() {
                                Picasso.with(mContext).load(clgLogo).placeholder(R.drawable.rtulogo).into(mClgLogo);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                mPrivacyDatabase.child(showingUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Log.d(TAG, "onDataChange: dataSnapShot :: "+dataSnapshot);
                        if(dataSnapshot.hasChildren()){
                            boolean isRollNoShow = Boolean.parseBoolean(dataSnapshot.child("roll_no").getValue().toString());
                            boolean isDobShow = Boolean.parseBoolean(dataSnapshot.child("dob").getValue().toString());
                            boolean isGenderShow = Boolean.parseBoolean(dataSnapshot.child("gender").getValue().toString());
                            boolean isPhoneShow = Boolean.parseBoolean(dataSnapshot.child("phone_number").getValue().toString());
                            boolean isEmailShow = Boolean.parseBoolean(dataSnapshot.child("email").getValue().toString());
                            if(isRollNoShow){
                                rollNoLayout.setVisibility(View.VISIBLE);
                            }else{
                                rollNoLayout.setVisibility(View.GONE);
                            }
                            if(isDobShow){
                                dobLayout.setVisibility(View.VISIBLE);
                            }else{
                                dobLayout.setVisibility(View.GONE);
                            }
                            if(isGenderShow){
                                genderLayout.setVisibility(View.VISIBLE);
                            }else{
                                genderLayout.setVisibility(View.GONE);
                            }
                            if(isPhoneShow){
                                phoneLayout.setVisibility(View.VISIBLE);
                            }else{
                                phoneLayout.setVisibility(View.GONE);
                            }
                            if(isEmailShow){
                                emailLayout.setVisibility(View.VISIBLE);
                            }else{
                                emailLayout.setVisibility(View.GONE);
                            }
                            if(isEmailShow || isPhoneShow){
                                contactLayout.setVisibility(View.VISIBLE);
                            }else{
                                contactLayout.setVisibility(View.GONE);
                            }
                        }else{
                            rollNoLayout.setVisibility(View.VISIBLE);
                            dobLayout.setVisibility(View.VISIBLE);
                            genderLayout.setVisibility(View.VISIBLE);
                            phoneLayout.setVisibility(View.VISIBLE);
                            emailLayout.setVisibility(View.VISIBLE);
                            contactLayout.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mToolbarName.setText(profileName);
                mProfileName.setText(profileName);
                Log.d(TAG, "onDataChange: User Roll No.:: "+rollno);
                mCourse.setText(getCourse(course,rollno));
                mBranch.setText(branch);
                mDob.setText(dob);
                mGender.setText(gender);
                mRollNo.setText(rollno);
                mEmail.setText(email);
                mPhoneNumber.setText(phoneNumber);
                mBio.setText(bio);

                    Picasso.with(mContext).load(profileImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.student).into(mProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onError() {
                                Picasso.with(mContext).load(profileImage).placeholder(R.drawable.student).into(mProfileImage);
                        }
                    });

                mProfileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog imageDialog = new Dialog(mContext,R.style.Theme_Dialog);
                        imageDialog.setContentView(R.layout.layout_image_zoom_popup);
                        final PhotoView image = imageDialog.findViewById(R.id.chatImage) ;
                        final TextView userName = imageDialog.findViewById(R.id.chatImageProfileName);
                        ImageView backArrow = imageDialog.findViewById(R.id.back_arrow_chat_image);
                        backArrow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageDialog.dismiss();
                            }
                        });
                        TextView timeView = imageDialog.findViewById(R.id.chatTime);
                        timeView.setText("Profile Picture");
                        Picasso.with(mContext).load(profileImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.student).into(image, new Callback() {
                                @Override
                                public void onSuccess() {
                                }
                                @Override
                                public void onError() {
                                    Picasso.with(mContext).load(profileImage).placeholder(R.drawable.student).into(image);
                                }
                            });
                        userName.setText(profileName);
                        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                        imageDialog.show();
                    }
                });

                mProgressDialog.dismiss();

            }  @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



        //Set Follow / Unfollow Button

        mRootRef.child("followings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(mCurrentUser.getUid()).hasChild(showingUserId)){
                    mFollow.setImageResource(R.drawable.ic_following);
                    followView.setText("Following");

                }else{
                    mFollow.setImageResource(R.drawable.ic_follow);
                    followView.setText("Follow");
                }
                long following =  dataSnapshot.child(showingUserId).getChildrenCount();
                if(following != 0){
                    mFollowings.setText(String.valueOf(following));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long followers =  dataSnapshot.child(showingUserId).getChildrenCount();
                if(followers != 0){
                    mFollowers.setText(String.valueOf(followers));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //******************* FOLLOW/UNFOLLOW Button ************************

        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProcessFollow = true;
                mRootRef.child("followings").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (mProcessFollow) {
                            if (dataSnapshot.child(mCurrentUser.getUid()).hasChild(showingUserId)) {
                                Map requestMap = new HashMap();
                                requestMap.put("followings/" + mCurrentUser.getUid() + "/" + showingUserId + "/timestamp", null);
                                requestMap.put("followers/" + showingUserId + "/" + mCurrentUser.getUid() + "/timestamp", null);
                                mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        mFollow.setImageResource(R.drawable.ic_follow);
                                    }
                                });
                                mProcessFollow = false;
                            } else {
                                Map requestMap = new HashMap();
                                requestMap.put("followings/" + mCurrentUser.getUid() + "/" + showingUserId + "/timestamp", ServerValue.TIMESTAMP);
                                requestMap.put("followers/" + showingUserId + "/" + mCurrentUser.getUid() + "/timestamp", ServerValue.TIMESTAMP);

                                mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        mFollow.setImageResource(R.drawable.ic_following);
                                    }
                                });
                                mProcessFollow = false;

                            }

                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        mChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(mContext, ChatActivity.class);
                chatIntent.putExtra("user_id", showingUserId);
                chatIntent.putExtra("user_name", mProfileName.getText());
                startActivity(chatIntent);

            }
        });

        mOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateBlockStatus();

            }
        });

        mPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext,ShowingUserPostsActivity.class);
                i.putExtra("user_id",showingUserId);
                i.putExtra("profile_name",mProfileName.getText().toString());
                startActivity(i);
            }
        });

        // Set Block Button
        mBlockListDatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("blocked").hasChild(showingUserId)){
                    blockView.setText("Blocked");
                }else{
                    blockView.setText("Block");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        }

    private void updateBlockStatus() {

        mProcessBlock = true;
        mRootRef.child("block_list").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(mProcessBlock){
                    if(dataSnapshot.child(mCurrentUser.getUid()).child("blocked").hasChild(showingUserId)){

                        Map requestMap = new HashMap();
                        requestMap.put(mCurrentUser.getUid()+"/" +"blocked"+ "/" + showingUserId , null);
                        requestMap.put(showingUserId + "/blocked_by/" + mCurrentUser.getUid() , null);

                        mBlockListDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                blockView.setText("Block");
                            }
                        });
                        mProcessBlock = false;

                    }else{
                        Map requestMap = new HashMap();
                        requestMap.put(mCurrentUser.getUid()+"/" +"blocked"+ "/" + showingUserId +"/timestamp",ServerValue.TIMESTAMP);
                        requestMap.put(showingUserId + "/blocked_by/" + mCurrentUser.getUid()+"/timestamp" ,ServerValue.TIMESTAMP);

                        mBlockListDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                blockView.setText("Blocked");
                            }
                        });
                        mProcessBlock = false;

                    }

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public String getCourse(String course,String rollNo){
        String year = CheckInputs.getCourseYear(rollNo);
        if(year.equals(" ")){
           return course;
        }
        year = course +" " + year;
        return year;
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