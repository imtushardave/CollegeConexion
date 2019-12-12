package com.cloudiera.collegeconexion.Friends;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudiera.collegeconexion.Models.AddFriendsModel;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Talks.ChatActivity;
import com.cloudiera.collegeconexion.Utils.CheckInputs;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.cloudiera.collegeconexion.R.drawable.student;


/**
 *  Fragment class for the showing the list of
 *  the all students of the college
 */
public class AddFriendsFragment extends Fragment {

    private static final String TAG = "AddFriendsFragment";



    // Firebase variables and Database References
    private DatabaseReference mDatabaseReference; // reference to the showing users database node in firebase
    private DatabaseReference mBlockListDatabase; // reference to the node for the block list of the person in firebase
    private FirebaseAuth mAuth;// Getting the current user logged in

    // Recycler view for showing the list of the all the students
    private RecyclerView mUsersList;

    private LinearLayoutManager linearLayoutManager;

    public AddFriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for showing the students list

        return inflater.inflate(R.layout.fragment_add_friends, container, false);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabaseReference = null;
        mBlockListDatabase = null;
        mUsersList = null;
        mAuth = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_verified_user));
        mDatabaseReference.keepSynced(true);

        mBlockListDatabase = FirebaseDatabase.getInstance().getReference().child("block_list");
        mBlockListDatabase.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        mUsersList = (RecyclerView) getView().findViewById(R.id.add_friends_list);
        mUsersList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        mUsersList.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onStart() {
        super.onStart();

        Query sortName = mDatabaseReference.orderByChild("profile_name");

        FirebaseRecyclerAdapter<AddFriendsModel,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<AddFriendsModel, UsersViewHolder>(
                AddFriendsModel.class,
                R.layout.layout_user_showing,
                UsersViewHolder.class,
                sortName
        ) {
            @Override
            protected void populateViewHolder(final UsersViewHolder viewHolder, final AddFriendsModel model, int position) {
                Log.d(TAG, "populateViewHolder:  Getting information the user :: " + model);

                final String userId = getRef(position).getKey();

                if (userId.equals(mAuth.getCurrentUser().getUid())) {
                    viewHolder.hidePost();
                } else {


                    mBlockListDatabase.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.child("blocked_by").hasChild(userId)) {

                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        // Creating popup dialog of the clicked user
                                        final Dialog myDialog = new Dialog(getContext());
                                        myDialog.setContentView(R.layout.layout_profile_popup);
                                        // Views of the popup dialog
                                        TextView btnCancel = (TextView) myDialog.findViewById(R.id.cancelbtn);

                                        TextView profileNameView = (TextView) myDialog.findViewById(R.id.popup_profileName);
                                        profileNameView.setText(model.getProfile_name());

                                        TextView profileBio = (TextView) myDialog.findViewById(R.id.popup_bio);
                                        profileBio.setText(model.getBranch());

                                        //Check for the offline stored image using  NetworkPolicy.OFFLINE through Picasso Library
                                        final CircleImageView profileImage = (CircleImageView) myDialog.findViewById(R.id.popup_profileImage);
                                        Picasso.with(getContext()).load(model.getProfile_img_thumb())
                                                .placeholder(student).networkPolicy(NetworkPolicy.OFFLINE).into(profileImage, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                            }
                                            @Override
                                            public void onError() {
                                                Picasso.with(getContext()).load(model.getProfile_img_thumb())
                                                        .placeholder(student).into(profileImage);
                                            }
                                        });


                                        //setup the cancel button
                                        btnCancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                myDialog.dismiss();
                                            }
                                        });
                                        //Set transparent background
                                        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        myDialog.show();

                                        LinearLayout showProfile = (LinearLayout) myDialog.findViewById(R.id.showProfileLayout);
                                        showProfile.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                Intent showingProfile = new Intent(getContext(), ShowingFriendsProfile.class);
                                                showingProfile.putExtra("user_id", userId);
                                                startActivity(showingProfile);
                                            }
                                        });

                                        LinearLayout showChatBox = (LinearLayout) myDialog.findViewById(R.id.showMessageLayout);
                                        showChatBox.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                chatIntent.putExtra("user_id", userId);
                                                chatIntent.putExtra("user_name", model.getProfile_name());
                                                startActivity(chatIntent);

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

                    viewHolder.showPost();

                    Log.d(TAG, "populateViewHolder: USER ID "+userId);
                    viewHolder.setName(model.getProfile_name());
                    viewHolder.setCourse(model.getCourse(), model.getRoll_no());
                    viewHolder.setBranch(model.getBranch());
                    viewHolder.setImage(getContext(), model.getProfile_img_thumb());

                }
            }
        };



        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }


    public static  class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }


        public void setName(String name){

            Log.d(TAG, "setName:  Name of the User" + name);
            TextView profileName = mView.findViewById(R.id.user_profile_name);
            profileName.setText(name);
            profileName = null;
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
            branchName = null;
        }
        public void setImage(final Context ctx, final String thumb_img){
           final CircleImageView profile_img = mView.findViewById(R.id.user_profile_image);

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
        public void hidePost(){
            mView.setVisibility(View.GONE);
            mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }

        public void showPost(){
            mView.setVisibility(View.VISIBLE);
        }

    }


}
