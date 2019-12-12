package com.cloudiera.collegeconexion.Talks;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudiera.collegeconexion.Friends.ShowingFriendsProfile;
import com.cloudiera.collegeconexion.Models.AddFriendsModel;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Utils.CheckInputs;
import com.cloudiera.collegeconexion.Utils.EmptyRecyclerView;
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

@SuppressWarnings("ResourceType")
public class SearchUserActivity extends AppCompatActivity {
    private static final String TAG = "SelectingGroupUserActiv";
    private Context mContext = SearchUserActivity.this;


    private ImageView mSearchResult;

    private LinearLayout emptyMessage;

    private EditText mSearchText;

    private EmptyRecyclerView mUsersList;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;
    private DatabaseReference mBlockListDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        initWidgetsSetup();

    }

    /**
     * Setup all the widgets used in the layout file
     */
    private void initWidgetsSetup(){

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_verified_user));
        mDatabaseReference.keepSynced(true);

        mBlockListDatabase = FirebaseDatabase.getInstance().getReference().child("block_list");
        mBlockListDatabase.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        mUsersList = (EmptyRecyclerView)findViewById(R.id.search_name_results);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(mContext));


        emptyMessage = (LinearLayout)findViewById(R.id.search_empty_view);
        emptyMessage.setVisibility(View.GONE);

        View emptyView = findViewById(R.id.search_empty_view);
        mUsersList.setEmptyView(emptyView);

        mSearchText = (EditText)findViewById(R.id.group_search_text);
        mSearchResult = (ImageView)findViewById(R.id.search_users_group);
        mSearchResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseSearchName();
            }
        });

    }

    private void firebaseSearchName(){
        String searchText = mSearchText.getText().toString().trim();
        searchText = CheckInputs.capitalString(searchText);

        Query searchQuery = mDatabaseReference.orderByChild("profile_name").startAt(searchText)
                .endAt(searchText+"\uf8ff");

        FirebaseRecyclerAdapter<AddFriendsModel,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<AddFriendsModel, UsersViewHolder>(

                AddFriendsModel.class,
                R.layout.layout_user_showing,
                UsersViewHolder.class,
                searchQuery
        ) {
            @Override
            protected void populateViewHolder(final UsersViewHolder viewHolder, final AddFriendsModel model, final int position) {

                final String userId = getRef(position).getKey();
                final String profileName = model.getProfile_name();
                final String profileBranch = model.getBranch();
                final String imageUrl = model.getProfile_img_thumb();

                mBlockListDatabase.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.child("blocked_by").hasChild(userId)) {
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent showingProfile = new Intent(mContext, ShowingFriendsProfile.class);
                                    showingProfile.putExtra("user_id", userId);
                                    startActivity(showingProfile);
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                if(userId.equals(mAuth.getCurrentUser().getUid())){
                    viewHolder.hidePost();
                }else{
                    viewHolder.showPost();
                    viewHolder.setName(profileName);
                    viewHolder.setCourse(model.getCourse(),model.getRoll_no());
                    viewHolder.setBranch(profileBranch);
                    viewHolder.setImage(mContext,imageUrl);
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
            TextView profileName = mView.findViewById(R.id.user_profile_name);
            profileName.setText(name);
        }
        public void setCourse(String course,String rollNO){
            TextView courseName = mView.findViewById(R.id.user_course_name);
            String year = CheckInputs.getCourseYear(rollNO);
            if(year.equals(" ")){
                courseName.setText(course);
            }else{
                rollNO = course + " "+ year;
                courseName.setText(rollNO);
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

        public void hidePost(){

            mView.setVisibility(View.GONE);
            mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
        public void showPost(){
            mView.setVisibility(View.VISIBLE);
        }

    }


}
