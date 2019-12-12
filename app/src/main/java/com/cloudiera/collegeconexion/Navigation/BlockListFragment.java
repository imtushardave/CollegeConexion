package com.cloudiera.collegeconexion.Navigation;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cloudiera.collegeconexion.Friends.ShowingFriendsProfile;
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
public class BlockListFragment extends Fragment {

    private static final String TAG = "BlockListFragment";

    private EmptyRecyclerView blockUserList;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mBlockListDatabase;
    View mView;
    private FirebaseAuth mAuth;
    public BlockListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_block_list, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_verified_user));
        mDatabaseReference.keepSynced(true);
        mBlockListDatabase = FirebaseDatabase.getInstance().getReference().child("block_list")
                .child(mAuth.getCurrentUser().getUid()).child("blocked");
        mBlockListDatabase.keepSynced(true);
        blockUserList = (EmptyRecyclerView) getView().findViewById(R.id.block_list_view);
        blockUserList.setHasFixedSize(true);
        blockUserList.setLayoutManager(new LinearLayoutManager(getContext()));
        View emptyView = mView.findViewById(R.id.empty_message_block_list);
        blockUserList.setEmptyView(emptyView);
    }
    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<BlockList,BlockedUsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<BlockList, BlockedUsersViewHolder>(

                BlockList.class,
                R.layout.layout_user_block_list_showing,
                BlockedUsersViewHolder.class,
                mBlockListDatabase
        ) {
            @Override
            protected void populateViewHolder(final BlockedUsersViewHolder viewHolder, BlockList model, int position) {

                final String list_user_id = getRef(position).getKey();

                mDatabaseReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("profile_name").getValue().toString();
                        String userThumb = dataSnapshot.child("profile_img_thumb").getValue().toString();
                        String course = dataSnapshot.child("course").getValue().toString();
                        String rollNO = dataSnapshot.child("roll_no").getValue().toString();
                        String branch = dataSnapshot.child("branch").getValue().toString();
                        viewHolder.setName(userName);
                        viewHolder.setImage(getContext(),userThumb);
                        viewHolder.setCourse(course,rollNO);
                        viewHolder.setBranch(branch);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent showingProfile = new Intent(getContext(), ShowingFriendsProfile.class);
                        showingProfile.putExtra("user_id", list_user_id);
                        startActivity(showingProfile);
                    }
                });

            }

        };

        blockUserList.setAdapter(firebaseRecyclerAdapter);
    }

    public static  class BlockedUsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public BlockedUsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }
        public void setName(String name){
            TextView profileName = mView.findViewById(R.id.user_profile_name_block_list);
            profileName.setText(name);
        }
        public void setCourse(String course,String rollNo){
            TextView courseName = mView.findViewById(R.id.user_course_name_block_list);
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
            TextView branchName = mView.findViewById(R.id.user_branch_name_block_list);
            branchName.setText(branch);
        }

        public void setImage(final Context ctx, final String thumb_img){
            final CircleImageView profile_img = mView.findViewById(R.id.user_profile_image_block_list);

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

    }

}
