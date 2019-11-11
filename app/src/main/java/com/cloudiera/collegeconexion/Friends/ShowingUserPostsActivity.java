package com.cloudiera.collegeconexion.Friends;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.cloudiera.collegeconexion.Models.BlockList;
import com.cloudiera.collegeconexion.Models.HappeningsPost;
import com.cloudiera.collegeconexion.Profile.ProfileActivity;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Talks.ChatImageShowActivity;
import com.cloudiera.collegeconexion.Utils.PostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ShowingUserPostsActivity extends AppCompatActivity {

    private Context mContext = ShowingUserPostsActivity.this;

    private static final String TAG = "ShowingUserPostsActivit";

    String showingUserId;

    private ImageView backArrow;
    private TextView profileName;

    private FirebaseAuth mAuth;

    private RecyclerView postsView;
    private DatabaseReference mPostDatabase,mLikesDatabase;
    private boolean mProcessLike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showing_user_posts);

        showingUserId = getIntent().getStringExtra("user_id");
        String name = getIntent().getStringExtra("profile_name");

        mAuth = FirebaseAuth.getInstance();

        backArrow = (ImageView)findViewById(R.id.back_arrow_showing_profile);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        profileName = (TextView)findViewById(R.id.showing_profile_toolbar_name) ;
        profileName.setText(name);

        postsView = (RecyclerView)findViewById(R.id.postsView);
        postsView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setAutoMeasureEnabled(true);
        postsView.setHasFixedSize(true);
        postsView.setLayoutManager(linearLayoutManager);

        mPostDatabase = FirebaseDatabase.getInstance().getReference().child("user_post").child("users").child(showingUserId);
        mLikesDatabase = FirebaseDatabase.getInstance().getReference().child("likes");


    }

    @Override
    public void onStart() {
        super.onStart();

                    FirebaseRecyclerAdapter<BlockList,PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<BlockList, PostViewHolder>(
                            BlockList.class,
                            R.layout.layout_happenings_post,
                            PostViewHolder.class,
                            mPostDatabase
                    ) {
                        @Override
                        protected void populateViewHolder(final PostViewHolder viewHolder, final BlockList postModel, final int position) {

                            String post_id = getRef(position).getKey();

                            DatabaseReference postData = FirebaseDatabase.getInstance().getReference().child("user_post")
                                    .child("rse001").child(post_id);
                            postData.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final HappeningsPost model = dataSnapshot.getValue(HappeningsPost.class);
                                    Log.d(TAG, "onDataChange: MODEL CLASS :: "+ model);
                                    Log.d(TAG, "onDataChange: MODEL CLASS :: "+ model);
                                    Log.d(TAG, "onDataChange: MODEL CLASS :: "+ model);
                                    Log.d(TAG, "onDataChange: MODEL CLASS :: "+ model);
                                    Log.d(TAG, "onDataChange: MODEL CLASS :: "+ model);
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
                                                    viewHolder.setProfileName(profileName);
                                                    final String userData = "(Post Key -) "+ post_key + "\nPosted By - " + profileName + "\n"
                                                            + dataSnapshot.child("roll_no").getValue().toString() + "\n"
                                                            + dataSnapshot.child("branch").getValue().toString() + "\nWrite a Problem - ";
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
//                                                            case R.id.edit_post :
//                                                                Toast.makeText(mContext, "Edit Post", Toast.LENGTH_SHORT).show();
//                                                                return true;
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


                                        viewHolder.postImage.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
//                                                Intent i = new Intent(mContext, ChatImageShowActivity.class);
//                                                i.putExtra("imageUrl",model.getImage_uri());
//                                                i.putExtra("timestamp",viewHolder.getTimeDate(mContext,model.getTimestamp()));
//                                                i.putExtra("userId",model.getUid());
//                                                startActivity(i);
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
                                    }

                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });


                        }
                    };
        postsView.setNestedScrollingEnabled(false);
        postsView.setAdapter(firebaseRecyclerAdapter);
                }

}
