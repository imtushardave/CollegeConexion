package com.cloudiera.collegeconexion.NoticeBox;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.CollegeConexion;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.cloudiera.collegeconexion.Friends.ShowingFriendsProfile;
import com.cloudiera.collegeconexion.Models.NoticeModel;
import com.cloudiera.collegeconexion.Navigation.AccountSettingsActivity;
import com.cloudiera.collegeconexion.Profile.EditProfileActivity;
import com.cloudiera.collegeconexion.Profile.ProfileActivity;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Talks.SearchUserActivity;
import com.cloudiera.collegeconexion.Utils.BottomNavigationViewHelper;
import com.cloudiera.collegeconexion.Utils.CheckInputs;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.cloudiera.collegeconexion.R.drawable.student;

public class NoticeBoxActivity extends AppCompatActivity implements ConnectionReceiver.ConnectionReceiverListener{

    private static final String TAG = "NoticeBoxActivity";
    private Context mContext = NoticeBoxActivity.this;
    private static final int ACTIVITY_NUM = 1;

    private String roll_no;
    private RecyclerView noticeView;

    private DatabaseReference mUserDatabase,mNoticeDatabase;
    private FirebaseAuth mAuth;


    private static final int TIME_DELAY = 2000;
    private static long back_pressed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_box);

        noticeView = (RecyclerView)findViewById(R.id.noticeView);
        noticeView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setAutoMeasureEnabled(true);
        noticeView.setHasFixedSize(true);
        noticeView.setLayoutManager(linearLayoutManager);

        setupBottomNavigation();
        setupToolbar();
        setupWidgets();

    }

    private void setupWidgets(){

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_verified_user));
        mUserDatabase.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                roll_no = dataSnapshot.child("roll_no").getValue().toString();
                String year = CheckInputs.getCourseYear(roll_no);
                if(year != null){

                    Log.d(TAG, "setupWidgets: YEAR OF THE STUDENT :" + year);
                        mNoticeDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_notice_box))
                                .child("rse001").child("verified_notice").child(year);
                        setupFirebaseAdapter();

                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FloatingActionButton addNoticeButton;
        addNoticeButton = (FloatingActionButton)findViewById(R.id.create_notice);
        addNoticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext,CreateNoticeActivity.class);
                startActivity(i);
            }
        });

    }


    /**
     * Setup the Top Toolbar
     */
    private void setupToolbar() {
        Log.d(TAG, "setupToolbar: Setting up top toolbar ");

        Toolbar toolbar = (Toolbar) findViewById(R.id.top_toolbar);
        setSupportActionBar(toolbar);
        ImageView menu = (ImageView) findViewById(R.id.top_toolbar_menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: sending Intent to account setting activity");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
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

    /**
     * SETUP FIREBASE RECYCELR ADAPTER FOR NOTICE VIEWS
     */
    private void setupFirebaseAdapter(){

        FirebaseRecyclerAdapter<NoticeModel,NoticeViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NoticeModel, NoticeViewHolder>(
                NoticeModel.class,
                R.layout.layout_notice,
                NoticeViewHolder.class,
                mNoticeDatabase
        ) {
            @Override
            protected void populateViewHolder(final NoticeViewHolder viewHolder, final NoticeModel model, int position) {

                viewHolder.setTitle(model.getHeading());
                viewHolder.setDescription(model.getDescription());
                Log.d(TAG, "populateViewHolder: TIMESTAMP : "+model.getStamp_time());

                viewHolder.setTime(mContext,model.getStamp_time());
                viewHolder.setDate(mContext,model.getStamp_time());

                viewHolder.setNoticeImage(mContext,model.getImageUri());

                viewHolder.noticeImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

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
                        Picasso.with(getApplicationContext()).load(model.getImageUri()).networkPolicy(NetworkPolicy.OFFLINE).into(image, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError() {
                                Picasso.with(getApplicationContext()).load(model.getImageUri()).into(image);
                            }
                        });
                        timeView.setText(viewHolder.getTimeDate(mContext, Long.parseLong(model.getStamp_time())));

                        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference()
                                .child(getString(R.string.dbname_verified_user)).child(model.getUser_id());
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

                viewHolder.userData.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if( model.getUser_id().equals(mAuth.getCurrentUser().getUid())){
                            Intent showingProfile1 = new Intent(mContext, ProfileActivity.class);
                            showingProfile1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            showingProfile1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            showingProfile1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(showingProfile1);
                        }else{
                            Intent showingProfile = new Intent(mContext, ShowingFriendsProfile.class);
                            showingProfile.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            showingProfile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            showingProfile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            showingProfile.putExtra("user_id", model.getUser_id());
                            startActivity(showingProfile);
                        }
                    }
                });

                mUserDatabase.child(model.getUser_id()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.setName(dataSnapshot.child("profile_name").getValue().toString());
                        viewHolder.setImage(mContext,dataSnapshot.child("profile_img_thumb").getValue().toString());
                        viewHolder.setRollNo(dataSnapshot.child("roll_no").getValue().toString());
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
        };

        noticeView.setAdapter(firebaseRecyclerAdapter);
    }


    public static  class NoticeViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public LinearLayout userData;
        public ImageView noticeImage;

        public NoticeViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            userData = (LinearLayout) mView.findViewById(R.id.userData);
            noticeImage = (ImageView)mView.findViewById(R.id.notice_post_image);
        }

        public void setTitle(String heading){
            TextView noticeTitle = mView.findViewById(R.id.notice_heading);
            noticeTitle.setText(heading);
        }

        public void setDate(Context ctx,String timestamp){
            TextView noticeDate = mView.findViewById(R.id.notice_date);

            String date = DateUtils.formatDateTime(ctx, Long.parseLong(timestamp), DateUtils.FORMAT_SHOW_DATE);
            noticeDate.setText(date);

        }
        public void setTime(Context ctx,String timestamp){
            TextView noticeTime = mView.findViewById(R.id.notice_time);

            String time = DateUtils.formatDateTime(ctx, Long.parseLong(timestamp), DateUtils.FORMAT_SHOW_TIME);
            noticeTime.setText(time);

        }

        public void setNoticeImage(final Context ctx, final String thumb_img){

            final ImageView noticeImage = mView.findViewById(R.id.notice_post_image);

            if (!thumb_img.equals(" ")) {
                noticeImage.setVisibility(View.VISIBLE);
                Picasso.with(ctx).load(thumb_img).placeholder(student).networkPolicy(NetworkPolicy.OFFLINE).into(noticeImage, new Callback() {
                    @Override
                    public void onSuccess() {
                    }
                    @Override
                    public void onError() {
                        Picasso.with(ctx).load(thumb_img).into(noticeImage);
                    }
                });

            } else {
                noticeImage.setVisibility(View.GONE);
            }
        }

        public void setDescription(String description){
            TextView noticeDescription = mView.findViewById(R.id.notice_description);
            noticeDescription.setText(description);
        }
        public void setName(String name){
            TextView postedByName = mView.findViewById(R.id.postedByName);
            postedByName.setText(name);
        }

        public void setRollNo(String rollNo){
            TextView postedByRollNO = mView.findViewById(R.id.postedByRollNo);
            postedByRollNO.setText(rollNo);
        }
        public void setImage(final Context ctx, final String thumb_img){

            final CircleImageView profile_img = mView.findViewById(R.id.posted_by_profile_image);

            Picasso.with(ctx).load(thumb_img).placeholder(student).networkPolicy(NetworkPolicy.OFFLINE).into(profile_img, new Callback() {
                @Override
                public void onSuccess() {
                }
                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb_img).placeholder(student).into(profile_img);
                }
            });

        }

        /*
      get Time and dat from the timestamp
       */
        public static String getTimeDate(Context ctx, long timestamp) {

            String time = DateUtils.formatDateTime(ctx, timestamp, DateUtils.FORMAT_SHOW_TIME);
            String date = DateUtils.formatDateTime(ctx, timestamp, DateUtils.FORMAT_SHOW_DATE);
            String timeData = date + " at " + time;
            return timeData;
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
