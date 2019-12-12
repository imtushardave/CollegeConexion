package com.cloudiera.collegeconexion.Friends;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudiera.collegeconexion.Models.BlockList;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Utils.CheckInputs;
import com.cloudiera.collegeconexion.Utils.EmptyRecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Followers extends Fragment {

    private static final String TAG = "Followers";
    private EmptyRecyclerView mFriendsList;

    private DatabaseReference mFollowersDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    private LinearLayout emptyMessage;
    private DatabaseReference mBlockListDatabase;

    public Followers() {
        // Required empty public constructor
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFriendsList = null;
        mFollowersDatabase = null;
        mUsersDatabase = null;
        mAuth = null;
        mCurrent_user_id = null;
        mMainView = null;
        emptyMessage = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_followers, container, false);

        mFriendsList = (EmptyRecyclerView) mMainView.findViewById(R.id.followers);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mBlockListDatabase = FirebaseDatabase.getInstance().getReference().child("block_list");
        mBlockListDatabase.keepSynced(true);

        mFollowersDatabase = FirebaseDatabase.getInstance().getReference().child("followers").child(mCurrent_user_id);
        mFollowersDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_verified_user));
        mUsersDatabase.keepSynced(true);

        emptyMessage = (LinearLayout)mMainView.findViewById(R.id.empty_message_followers);
        emptyMessage.setVisibility(View.GONE);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        View emptyView = mMainView.findViewById(R.id.empty_message_followers);
        mFriendsList.setEmptyView(emptyView);

        // Inflate the layout for this fragment

        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<BlockList, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<BlockList, FriendsViewHolder>(

                BlockList.class,
                R.layout.layout_user_showing,
                FriendsViewHolder.class,
                mFollowersDatabase

        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, BlockList model, int position) {


                final String list_user_id = getRef(position).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        viewHolder.setName(dataSnapshot.child("profile_name").getValue().toString());
                        viewHolder.setImage(getContext(),dataSnapshot.child("profile_img_thumb").getValue().toString());
                        viewHolder.setCourse(dataSnapshot.child("course").getValue().toString(),
                                             dataSnapshot.child("roll_no").getValue().toString());
                        viewHolder.setBranch(dataSnapshot.child("branch").getValue().toString());
                        viewHolder.setUserOnline(dataSnapshot.child("online").getValue().toString());
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mBlockListDatabase.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.child("blocked_by").hasChild(list_user_id)) {
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent showingProfile = new Intent(getContext(), ShowingFriendsProfile.class);
                                    showingProfile.putExtra("user_id", list_user_id);
                                    startActivity(showingProfile);
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

        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }


        public void setName(String name){
            TextView profileName = mView.findViewById(R.id.user_profile_name);
            profileName.setText(name);
        }
        public void setCourse(String course,String rollNo){
            TextView courseName = mView.findViewById(R.id.user_course_name);
            String year = CheckInputs.getCourseYear(rollNo);
            if(year != null){
                if(year.equals(" ")){
                    courseName.setText(course);
                }else{
                    rollNo = course + " "+ year;
                    courseName.setText(rollNo);
                }
            }else{
                courseName.setText(course);
            }
            year = null;
            courseName = null;
        }
        public void setBranch(String branch){
            TextView branchName = mView.findViewById(R.id.user_branch_name);
            branchName.setText(branch);
        }
        public void setImage(final Context ctx, final String thumb_img){
            final CircleImageView profile_img = mView.findViewById(R.id.user_profile_image);

            Picasso.with(ctx).load(thumb_img).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.student).into(profile_img, new Callback() {
                @Override
                public void onSuccess() {

                }
                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb_img).placeholder(R.drawable.student).into(profile_img);
                }
            });
        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }


    }


}

