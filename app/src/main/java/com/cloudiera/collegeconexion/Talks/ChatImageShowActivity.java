package com.cloudiera.collegeconexion.Talks;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudiera.collegeconexion.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ChatImageShowActivity extends AppCompatActivity  {

    private Context mContext;
    private static final String TAG = "ChatImageShowActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image_show);

        mContext = ChatImageShowActivity.this;
        final String imageUrl = getIntent().getStringExtra("imageUrl");
        String time = getIntent().getStringExtra("timestamp");
        String userId = getIntent().getStringExtra("userId");

        final PhotoView image = (PhotoView)findViewById(R.id.chatImage) ;
        TextView timeView = (TextView)findViewById(R.id.chatTime);
        final TextView userName = (TextView)findViewById(R.id.chatImageProfileName);
        ImageView backArrow = (ImageView)findViewById(R.id.back_arrow_chat_image);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Picasso.with(getApplicationContext()).load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(image, new Callback() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onError() {
                Picasso.with(getApplicationContext()).load(imageUrl).into(image);
            }
        });

        timeView.setText(time);

        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference()
                     .child(getString(R.string.dbname_verified_user)).child(userId);
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName.setText(dataSnapshot.child("profile_name").getValue().toString());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

}
