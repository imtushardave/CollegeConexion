package com.cloudiera.collegeconexion.Talks;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.CollegeConexion;

import com.cloudiera.collegeconexion.Friends.ShowingFriendsProfile;
import com.cloudiera.collegeconexion.LogIn.EntryActivity;
import com.cloudiera.collegeconexion.Models.ChatView;
import com.cloudiera.collegeconexion.Navigation.AccountSettingsActivity;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Utils.BottomNavigationViewHelper;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.cloudiera.collegeconexion.Utils.EmptyRecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class TalksActivity extends AppCompatActivity  implements ConnectionReceiver.ConnectionReceiverListener {

    private static final String TAG = "TalksActivity";
    private Context mContext = TalksActivity.this;
    private static final int ACTIVITY_NUM = 3;

    // FireBase
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;


    private EmptyRecyclerView mFriendsChatList;


    private DatabaseReference mConvDatabase;
    private DatabaseReference mFriendsChatDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;
    private DatabaseReference mBlockListDatabase; // reference to the node for the block list of the person in firebase

    private String mCurrent_user_id;

    FirebaseRecyclerAdapter<ChatView, FriendsChatViewHolder> friendsChatRecyclerViewAdapter;

    private static final int TIME_DELAY = 2000;
    private static long back_pressed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talks);
        checkConnection();
      //  setupBottomNavigation();
        setupToolbar();
        setupFireBaseAuth();
        myRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_verified_user)).child(mAuth.getCurrentUser().getUid());

        mFriendsChatList = (EmptyRecyclerView) findViewById(R.id.friends_chat_list);

        mBlockListDatabase = FirebaseDatabase.getInstance().getReference().child("block_list");
        mBlockListDatabase.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mConvDatabase.orderByChild("timestamp");

        mFriendsChatDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mFriendsChatDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_verified_user));
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setAutoMeasureEnabled(true);
        mFriendsChatList.setHasFixedSize(true);
        mFriendsChatList.setLayoutManager(linearLayoutManager);

        View emptyView = findViewById(R.id.empty_message_chat_list);
        mFriendsChatList.setEmptyView(emptyView);


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
     ** -------------------------------   FIREBASE SETUP SECTION   ----------------------------
     */

    /**
     * check whether user is logged in or not
     */
    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking user status of log in");
        if (user == null) {
            Intent intent = new Intent(mContext, EntryActivity.class);
            startActivity(intent);
        }else{
            myRef.child("online").setValue(true);
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
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // Check Whether is logged in or not
                checkCurrentUser(user);
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
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

        friendsChatRecyclerViewAdapter = new FirebaseRecyclerAdapter<ChatView, FriendsChatViewHolder>(
                ChatView.class,
                R.layout.layout_showing_chat_list,
                FriendsChatViewHolder.class,
                mConvDatabase

        ) {
            @Override
            protected void populateViewHolder(final FriendsChatViewHolder viewHolder, final ChatView conv, int position) {

                final String list_user_id = getRef(position).getKey();

                viewHolder.setUserId(mAuth.getCurrentUser().getUid(),list_user_id);

                Query lastMessageQuery = mFriendsChatDatabase.child(list_user_id).limitToLast(1);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Log.d(TAG, "onChildAdded:  datasnapshot " + dataSnapshot);

                        if(dataSnapshot.hasChild("message")){
                            String data = dataSnapshot.child("message").getValue().toString();
                            String time = dataSnapshot.child("time").getValue().toString();
                            String type = dataSnapshot.child("type").getValue().toString();
                            viewHolder.setLastMessage(data, conv.isSeen(),mContext,type,list_user_id,mAuth.getCurrentUser().getUid());
                            viewHolder.lastMessageTime(time,mContext,conv.isSeen());
                        }

                    }
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("profile_name").getValue().toString();
                        String userThumb = dataSnapshot.child("profile_img_thumb").getValue().toString();
                        viewHolder.setUserName(userName);
                        viewHolder.setUserImage(userThumb,mContext);
                        mBlockListDatabase.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.child("blocked_by").hasChild(list_user_id)) {
                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(mContext, ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", userName);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        };

        mFriendsChatList.setAdapter(friendsChatRecyclerViewAdapter);


    }


    @Override
    public boolean onContextItemSelected(MenuItem item){

        String currentUser = item.getIntent().getStringExtra("user_id");
        String chatUser = item.getIntent().getStringExtra("chat_user_id");

        if(item.getTitle()=="View Profile"){

            Intent chatIntent = new Intent(mContext, ShowingFriendsProfile.class);
            chatIntent.putExtra("user_id", chatUser);
            startActivity(chatIntent);

        }
        else if(item.getTitle()=="Delete Conversation"){

            DatabaseReference mChatDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("Chat").child(currentUser).child(chatUser);
            mChatDatabase.removeValue();
            friendsChatRecyclerViewAdapter.notifyDataSetChanged();

            DatabaseReference mChatMessages = FirebaseDatabase.getInstance().getReference()
                    .child("messages").child(currentUser).child(chatUser);
            mChatMessages.removeValue();

        }else{
            return false;
        }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);

        }
        myRef.child("online").setValue(ServerValue.TIMESTAMP);
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


    public static class FriendsChatViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        View mView;

        String currentUser;
        String chatUser;

        public FriendsChatViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            itemView.setOnCreateContextMenuListener(this);
        }

        public void setUserId(String userId,String chatUserId){

            currentUser = userId;
            chatUser = chatUserId;

        }


        public void lastMessageTime(String timestamp, Context context,boolean isSeen){
            TextView userStatusView = (TextView) mView.findViewById(R.id.last_message_time);
            if(timestamp!=null) {
                if(!isSeen){

                    userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
                } else {

                    userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
                }
                long time = Long.parseLong(timestamp);
                String timeData =  DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME);
                userStatusView.setText(timeData);

            }else{
                userStatusView.setText("");
            }
        }

        public void setUserName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_profile_chat_name);
            userNameView.setText(name);

        }

        public void setUserImage(final String thumb_image, final Context ctx){

            final CircleImageView profile_img = mView.findViewById(R.id.user_profile_chat_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.student).networkPolicy(NetworkPolicy.OFFLINE).into(profile_img, new Callback() {
                @Override
                public void onSuccess() {
                }
                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.student).into(profile_img);
                }
            });
        }



        public  void setLastMessage(String lastMessage, boolean isSeen,Context ctx,String type,String chatUser,String currentUser){
            TextView lastMessageView = mView.findViewById(R.id.last_message);
            if(!isSeen && !chatUser.equals(currentUser)){
//                lastMessageView.setTextColor(ctx.getResources().getColor(R.color.appBlueTheme));
                //    lastMessageView.setTypeface(lastMessageView.getTypeface(), Typeface.BOLD);
            } else {
//                lastMessageView.setTextColor(ctx.getResources().getColor(R.color.grey));
                //     lastMessageView.setTypeface(lastMessageView.getTypeface(), Typeface.NORMAL);
            }
            switch (type) {
                case "text":
                    lastMessageView.setText(lastMessage);
                    break;
                case "image":
                    lastMessageView.setText("Image");
                    break;
                case "pdf":
                    lastMessageView.setText("Document");
                    break;
                case "audio":
                    lastMessageView.setText("Audio");
                    break;
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            Intent data = new Intent();
            data.putExtra("user_id",currentUser);
            data.putExtra("chat_user_id",chatUser);
            menu.add(0, v.getId(), 0, "View Profile").setIntent(data);//groupId, itemId, order, title
            menu.add(0, v.getId(), 0, "Delete Conversation").setIntent(data);

        }
    }
}
