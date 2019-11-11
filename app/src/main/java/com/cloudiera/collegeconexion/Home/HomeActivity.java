package com.cloudiera.collegeconexion.Home;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.CollegeConexion;
import com.cloudiera.collegeconexion.Friends.ShowingFriendsProfile;
import com.cloudiera.collegeconexion.LogIn.EntryActivity;
import com.cloudiera.collegeconexion.Models.BlockList;
import com.cloudiera.collegeconexion.Models.HappeningsPost;
import com.cloudiera.collegeconexion.Navigation.AccountSettingsActivity;
import com.cloudiera.collegeconexion.Profile.ProfileActivity;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Talks.ChatImageShowActivity;
import com.cloudiera.collegeconexion.Talks.SearchUserActivity;
import com.cloudiera.collegeconexion.Utils.BottomNavigationViewHelper;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.cloudiera.collegeconexion.Utils.PostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import static android.R.attr.mode;
import static com.cloudiera.collegeconexion.R.id.roll_no;
import static java.security.AccessController.getContext;


public class HomeActivity extends AppCompatActivity  implements ConnectionReceiver.ConnectionReceiverListener {

    private  Context mContext;
    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 0;
    private FloatingActionButton addPostBtn;
    private RecyclerView mHappenings;
    private boolean mProcessLike = false;

    private static final int TIME_DELAY = 2000;
    private static long back_pressed;
    // FireBase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mPostDatabase,mLikesDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mContext  = HomeActivity.this;
        checkConnection();
        setupFireBaseAuth();
        mAuth = FirebaseAuth.getInstance();

        mHappenings = (RecyclerView)findViewById(R.id.happeningsView);
        mHappenings.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setAutoMeasureEnabled(true);
        mHappenings.setHasFixedSize(true);
        mHappenings.setLayoutManager(linearLayoutManager);

        mPostDatabase = FirebaseDatabase.getInstance().getReference().child("user_post").child("rse001");
        mLikesDatabase = FirebaseDatabase.getInstance().getReference().child("likes");

        verifyPermissions();
        setupBottomNavigation();
        setupToolbar();


        addPostBtn = (FloatingActionButton)findViewById(R.id.create_post);
        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext,PostActivity.class);
                startActivity(i);
            }
        });

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
    private void setupBottomNavigation(){
         BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation);
        if(bottomNavigationViewEx!=null){
         BottomNavigationViewHelper.setupBottomNavigation(bottomNavigationViewEx);
         BottomNavigationViewHelper.enableNavigation(mContext,bottomNavigationViewEx);
         Menu menu = bottomNavigationViewEx.getMenu();
         MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
         menuItem.setChecked(true);
        }
        else{
            Log.i("Object Is Null ::","What is the Reason");
        }
     }

    /*
     ** -------------------------------Firebase Setup ----------------------------
     */

    /**
     * check whether user is logged in or not
     */
    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking user status of log in");
        if (user == null) {
            Intent intent = new Intent(mContext, EntryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
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
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check Whether is logged in or not
                checkCurrentUser(user);
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_verified_user));
                    userDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final Dialog myDialog = new Dialog(mContext);
                            myDialog.setContentView(R.layout.layout_account_status_popup);
                            myDialog.setCanceledOnTouchOutside(false);
                            myDialog.setCancelable(false);
                            final TextView title = myDialog.findViewById(R.id.title_message);
                            final TextView message = myDialog.findViewById(R.id.message);
                            Button reportConex = myDialog.findViewById(R.id.report_button);
                            reportConex.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"conexrtu@collegeconexion.com"});
                                    intent.putExtra(Intent.EXTRA_SUBJECT,"Reporting Conex");
                                    if (intent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(intent);
                                    }
                                }
                            });
                            Button signOutButton = myDialog.findViewById(R.id.signOut_button);
                            signOutButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mAuth.signOut();
                                    myDialog.dismiss();
                                    finish();
                                }
                            });
                            DatabaseReference rejectedUserDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_rejected_user));
                            rejectedUserDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user.getUid())){
                                        title.setText("YOUR ACCOUNT IS REJECTED");
                                        String reason = dataSnapshot.child(user.getUid()).child("rejection_reason")
                                                .getValue().toString();
                                        reason = ("*  Reason  *\n\n") + reason;
                                        message.setText(reason);
                                    }else{
                                        title.setText("YOUR ACCOUNT WILL BE VERIFIED WITHIN \n AN HOUR");
                                        message.setText(getString(R.string.account_not_verified));
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            if(dataSnapshot.hasChild(user.getUid())){
                                Log.d(TAG, "onDataChange:  USER IS VERIFIED ");
                                myDialog.dismiss();
                            }else{
                                Log.d(TAG, "onDataChange:  USER IS NOT VERIFIED");
                                myDialog.show();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
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

          if(mAuth.getCurrentUser()!=null){
                    FirebaseRecyclerAdapter<HappeningsPost,PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<HappeningsPost, PostViewHolder>(
                            HappeningsPost.class,
                            R.layout.layout_happenings_post,
                            PostViewHolder.class,
                            mPostDatabase
                    ) {
                        @Override
                        protected void populateViewHolder(final PostViewHolder viewHolder, final HappeningsPost model, final int position) {
                            Log.d(TAG, "onDataChange: model Value : " +model);

                            final String user = model.getUid();
                            if(user != null){
                                viewHolder.showPost();
                                final String post_key = getRef(position).getKey();
                                viewHolder.setDescription(model.getDesc());
                                viewHolder.setPostImage(getApplicationContext(),model.getImage_uri());
                                viewHolder.setPostTime(getApplicationContext(),model.getTimestamp());
                                viewHolder.setLikeIcon(post_key,getApplicationContext());
                                viewHolder.setTotalLike(post_key);

                                DatabaseReference userData = FirebaseDatabase.getInstance().getReference()
                                        .child(getString(R.string.dbname_verified_user)).child(user);
                                userData.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot!=null){
                                            Log.d(TAG, "onDataChange: DATASNAPSHOT :: "+ dataSnapshot);
                                            String profileName = dataSnapshot.child("profile_name").getValue().toString();
                                            String profileImageUri = dataSnapshot.child("profile_img_thumb").getValue().toString();
                                            viewHolder.setProfileImage(getApplicationContext(),profileImageUri);
                                            final String userData = "(Post Key -) "+ post_key + "\nPosted By - " + profileName + "\n"
                                                    + dataSnapshot.child("roll_no").getValue().toString() + "\n"
                                                     + dataSnapshot.child("branch").getValue().toString() + "\nWrite a Problem - ";
                                            viewHolder.setProfileName(profileName);
                                            viewHolder.postOption.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    PopupMenu dialog = new PopupMenu(mContext,viewHolder.postOption);
                                                    if(user.equals(mAuth.getCurrentUser().getUid())){
                                                        dialog.inflate(R.menu.happenings_menu_user);
                                                    }else{
                                                        dialog.inflate(R.menu.happenings_menu);
                                                    }
                                                    dialog.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                                        @Override
                                                        public boolean onMenuItemClick(MenuItem item) {
                                                            switch (item.getItemId()){
                                                                case R.id.report_problem :
                                                                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                                                                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                                                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@collegeconexion.com"});
                                                                    intent.putExtra(Intent.EXTRA_SUBJECT,"Report Problem");
                                                                    intent.putExtra(Intent.EXTRA_TEXT,userData);
                                                                    if (intent.resolveActivity(getPackageManager()) != null) {
                                                                        startActivity(intent);
                                                                    }
                                                                    return true;
                                                                case R.id.delete_post :
                                                                    AlertDialog.Builder confirm = new AlertDialog.Builder(mContext);
                                                                    confirm.setTitle("Are you sure to delete post?");
                                                                    confirm.setMessage("Once the post deleted cannot be recoverd in any way.");
                                                                    confirm.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {

                                                                            final ProgressDialog progress = new ProgressDialog(mContext);
                                                                            progress.setMessage("Deleting Post");
                                                                            progress.setCanceledOnTouchOutside(false);
                                                                            progress.setCancelable(false);
                                                                            progress.show();

                                                                            mPostDatabase.child(post_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    DatabaseReference userNewPost = FirebaseDatabase.getInstance().getReference()
                                                                                            .child("user_post").child("users").child(mAuth.getCurrentUser().getUid());
                                                                                    userNewPost.child(post_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            progress.dismiss();
                                                                                        }
                                                                                    });

                                                                                }
                                                                            });


                                                                        }
                                                                    });
                                                                    confirm.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            dialog.dismiss();
                                                                        }
                                                                    });
                                                                    confirm.show();
                                                                    return true;
//                                                    case R.id.edit_post :
//                                                        Toast.makeText(mContext, "Edit Post", Toast.LENGTH_SHORT).show();
//                                                        return true;
                                                            }
                                                            return false;
                                                        }
                                                    });
                                                    dialog.show();

                                                }
                                            });
                                        }else{
                                            Toast.makeText(mContext, "Something went Wrong", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });

                                viewHolder.userName.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(user.equals(mAuth.getCurrentUser().getUid())){
                                            Intent showingProfile1 = new Intent(mContext, ProfileActivity.class);
                                            showingProfile1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            showingProfile1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(showingProfile1);
                                        }else{
                                            Intent showingProfile = new Intent(mContext, ShowingFriendsProfile.class);
                                            showingProfile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            showingProfile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            showingProfile.putExtra("user_id", user);
                                            startActivity(showingProfile);
                                        }

                                    }
                                });



                                viewHolder.postImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
//                                        Intent i = new Intent(mContext, ChatImageShowActivity.class);
//                                        i.putExtra("imageUrl",model.getImage_uri());
//                                        i.putExtra("timestamp",viewHolder.getTimeDate(mContext,model.getTimestamp()));
//                                        i.putExtra("userId",model.getUid());
//                                        startActivity(i);
                                        final Dialog imageDialog = new Dialog(mContext,R.style.Theme_Dialog);
                                        imageDialog.setContentView(R.layout.layout_image_zoom_popup);
                                        imageDialog.setCanceledOnTouchOutside(false);
                                        final PhotoView image = imageDialog.findViewById(R.id.chatImage) ;
                                        TextView timeView = imageDialog.findViewById(R.id.chatTime);
                                        final TextView userName = imageDialog.findViewById(R.id.chatImageProfileName);
                                        ImageView backArrow = imageDialog.findViewById(R.id.back_arrow_chat_image);
                                        backArrow.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                               imageDialog.dismiss();
                                            }
                                        });
                                        Picasso.with(getApplicationContext()).load(model.getImage_uri()).networkPolicy(NetworkPolicy.OFFLINE).into(image, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                            }
                                            @Override
                                            public void onError() {
                                                Picasso.with(getApplicationContext()).load(model.getImage_uri()).into(image);
                                            }
                                        });
                                        timeView.setText(viewHolder.getTimeDate(mContext,model.getTimestamp()));

                                        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference()
                                                .child(getString(R.string.dbname_verified_user)).child(model.getUid());
                                        userDatabase.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                userName.setText(dataSnapshot.child("profile_name").getValue().toString());
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                                        imageDialog.show();

                                    }
                                });

                                // Like button Action
                                viewHolder.likeImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mProcessLike = true;
                                        mLikesDatabase.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(mProcessLike){
                                                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){
                                                        mLikesDatabase.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                                        mProcessLike = false;
                                                    }else{
                                                        MediaPlayer hiFiveSound = MediaPlayer.create(mContext,R.raw.hi_five_sound);
                                                        hiFiveSound.start();
                                                        mLikesDatabase.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("Random Value");
                                                        mProcessLike = false;
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }

                                });
                            }else{
                                mHappenings.removeView(viewHolder.mView);
                                viewHolder.hidePost();
                            }

                        }
                    };
                    mHappenings.setNestedScrollingEnabled(false);
                    mHappenings.setAdapter(firebaseRecyclerAdapter);
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


    private void verifyPermissions(){

        String[] permissions = {android.Manifest.permission.CAMERA,
                                 android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                   android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED){

        }else{
            ActivityCompat.requestPermissions(HomeActivity.this,permissions,1);
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
