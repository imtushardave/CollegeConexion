package com.cloudiera.collegeconexion.Utils;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudiera.collegeconexion.R;
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
 * Created by HP on 13-Jan-18.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {

   public View mView;
    public LinearLayout likeImage;
    TextView likeTextView, totallike;
    public TextView userName;
    public ImageView postOption;
    public ImageView likeImageBtn,postImage;
    DatabaseReference likesDatabase;
    FirebaseAuth mAuth;
    public PostViewHolder(View itemView) {

        super(itemView);
        mView = itemView;
        postImage = (ImageView)mView.findViewById(R.id.happenings_post_image);
        userName = (TextView) mView.findViewById(R.id.happenings_profile_name);
        likeImage = (LinearLayout) mView.findViewById(R.id.like_image);
        likeTextView = (TextView) mView.findViewById(R.id.total_like);
        totallike = (TextView) mView.findViewById(R.id.total_like_post);
        likeImageBtn = (ImageView)mView.findViewById(R.id.likeImage_btn);
        postOption = (ImageView)mView.findViewById(R.id.post_options) ;
        mAuth = FirebaseAuth.getInstance();
        likesDatabase = FirebaseDatabase.getInstance().getReference().child("likes");
        likesDatabase.keepSynced(true);

    }

    public void setLikeIcon(String push_key, final Context ctx) {
        if(mAuth!=null){
            likesDatabase.child(push_key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(mAuth!=null){
                        if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                            likeImageBtn.setImageResource(R.drawable.ic_like);
                            likeTextView.setText("Hi-Five");
                            likeTextView.setTextColor(ctx.getResources().getColor(R.color.darkBlueTheme));
                        } else {
                            likeImageBtn.setImageResource(R.drawable.ic_unlike);
                            likeTextView.setText("Hi");
                            likeTextView.setTextColor(ctx.getResources().getColor(R.color.appBlueTheme));
                        }
                    }

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }



    }

    public void setTotalLike(String push_key) {

        likesDatabase.child(push_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                if (count != 0) {
                    totallike.setVisibility(View.VISIBLE);
                    String total = "* "+count + "Hi-Five";
                    totallike.setText(total);
                } else {
                    totallike.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    public void setDescription(String desc) {
        TextView description = (TextView) mView.findViewById(R.id.happenings_description);
        if (!TextUtils.isEmpty(desc)) {
            description.setVisibility(View.VISIBLE);
            description.setText(desc);
        } else {
            description.setVisibility(View.GONE);
        }

    }

    public void setPostImage(final Context ctx, final String imageUri) {
        final ImageView postImage = (ImageView) mView.findViewById(R.id.happenings_post_image);
        if (!TextUtils.isEmpty(imageUri)) {
            postImage.setVisibility(View.VISIBLE);
            Picasso.with(ctx).load(imageUri).networkPolicy(NetworkPolicy.OFFLINE).into(postImage, new Callback() {
                @Override
                public void onSuccess() {
                }
                @Override
                public void onError() {
                          Picasso.with(ctx).load(imageUri).into(postImage);
                }
            });

        } else {
            postImage.setVisibility(View.GONE);
        }
    }

    public void setProfileImage(final Context ctx, final String profieImgUri) {
        final CircleImageView profileImg = (CircleImageView) mView.findViewById(R.id.happenings_profile_image);

        Picasso.with(ctx).load(profieImgUri).placeholder(R.drawable.student).networkPolicy(NetworkPolicy.OFFLINE).into(profileImg, new Callback() {
            @Override
            public void onSuccess() {

            }
            @Override
            public void onError() {
                Picasso.with(ctx).load(profieImgUri).placeholder(R.drawable.student).into(profileImg);
            }
        });

    }

    public void setProfileName(String profileName) {
        TextView profile_name = (TextView) mView.findViewById(R.id.happenings_profile_name);
        profile_name.setText(profileName);
    }

    public void setPostTime(Context ctx, long timestamp) {
        TextView timeStampData = (TextView) mView.findViewById(R.id.happenings_post_time);
        timeStampData.setText(getTimeDate(ctx, timestamp));
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

    public void hidePost(){

        mView.setVisibility(View.GONE);
        mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
    }
    public void showPost(){
        mView.setVisibility(View.VISIBLE);
    }

}

