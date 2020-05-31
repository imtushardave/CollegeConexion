//package com.cloudiera.collegeconexion.Talks;
//
//import android.Manifest;
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.media.MediaPlayer;
//import android.net.Uri;
//import android.os.Build;
//import android.provider.MediaStore;
//import android.provider.Settings;
//import androidx.annotation.NonNull;
//import androidx.core.content.ContextCompat;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import android.os.Bundle;
//import androidx.cardview.widget.CardView;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.appcompat.widget.PopupMenu;
//import androidx.recyclerview.widget.RecyclerView;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.animation.AccelerateDecelerateInterpolator;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.cloudiera.collegeconexion.CollegeConexion;
//import com.cloudiera.collegeconexion.Friends.ShowingFriendsProfile;
//import com.cloudiera.collegeconexion.Utils.CheckInputs;
//import com.cloudiera.collegeconexion.LogIn.EntryActivity;
//import com.cloudiera.collegeconexion.Models.Messages;
//import com.cloudiera.collegeconexion.R;
//import com.cloudiera.collegeconexion.ConnectionReceiver;
//import com.cloudiera.collegeconexion.Utils.GetTimeAgo;
//import com.cloudiera.collegeconexion.Utils.MessageAdapter;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.ChildEventListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ServerValue;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.OnProgressListener;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;
//import com.squareup.picasso.Callback;
//import com.squareup.picasso.NetworkPolicy;
//import com.squareup.picasso.Picasso;
//
//import java.io.ByteArrayOutputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//import io.codetail.animation.ViewAnimationUtils;
//
//
//@SuppressWarnings("VisibleForTests")
//public class ChatActivity extends AppCompatActivity implements View.OnClickListener, ConnectionReceiver.ConnectionReceiverListener {
//
//    private static final String TAG = "ChatActivity";
//    private static final int CAMERA_REQUEST_CODE = 1;
//    private static final int PDF_REQUEST_CODE = 11;
//    private static final int GALLERY_REQUEST_CODE = 111;
//    private static final int AUDIO_REQUEST_CODE = 112;
//
//    private String imageId;
//    private ProgressDialog mProgress;
//    private Context mContext = ChatActivity.this;
//    boolean hidden = true;
//    private String mChatUser;
//    private TextView mLastSeen;
//    private CircleImageView mChatImage;
//    private String mCurrentUser;
//    private EditText chatMessage;
//    private RecyclerView mMessageList;
//    private LinearLayout mRevealItems;
//    private final List<Messages> messagesList = new ArrayList<>();
//    private MessageAdapter mAdapter;
//    //Firebase
//    private DatabaseReference mRootRef, myRef;
//    private FirebaseAuth mAuth;
//    private StorageReference mChatAttachmentStorage;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chat);
//        checkConnection();
//        mRevealItems = (LinearLayout) findViewById(R.id.reveal_items);
//        mRevealItems.setVisibility(View.INVISIBLE);
//        ImageView mAttachFile = (ImageView) findViewById(R.id.chat_attach_file);
//        mChatUser = getIntent().getStringExtra("user_id");
//        String mUserName = getIntent().getStringExtra("user_name");
//        CardView chatSendBtn = (CardView) findViewById(R.id.chat_messsage_send);
//        chatMessage = (EditText) findViewById(R.id.chat_message);
//        ImageButton mCameraSelector = (ImageButton) findViewById(R.id.camera_selection);
//        ImageButton mGallerySelector = (ImageButton) findViewById(R.id.gallery_selection);
////        mAudioSelector = (ImageButton) findViewById(R.id.audio_selection);
//        ImageButton mDocumentSelector = (ImageButton) findViewById(R.id.document_selection);
//        mProgress = new ProgressDialog(this);
//        mCameraSelector.setOnClickListener(this);
//        mGallerySelector.setOnClickListener(this);
////        mAudioSelector.setOnClickListener(this);
//        mDocumentSelector.setOnClickListener(this);
//
//        mAdapter = new MessageAdapter(messagesList, getApplicationContext());
//
//        mMessageList = (RecyclerView) findViewById(R.id.chatting_view);
//        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
//
//        mMessageList.setHasFixedSize(true);
//        mMessageList.setLayoutManager(mLinearLayout);
//        mMessageList.setAdapter(mAdapter);
//
//
//        mRootRef = FirebaseDatabase.getInstance().getReference();
//        mAuth = FirebaseAuth.getInstance();
//        myRef = FirebaseDatabase.getInstance().getReference()
//                .child(getString(R.string.dbname_verified_user)).child(mAuth.getCurrentUser().getUid());
//        myRef.keepSynced(true);
//        myRef.child("online").setValue(true);
//
//        mCurrentUser = mAuth.getCurrentUser().getUid();
//        mChatAttachmentStorage = FirebaseStorage.getInstance().getReference().child("Messages");
//
//
//        // Custom Action Bar items
//        TextView mChatTitle = (TextView) findViewById(R.id.custom_bar_display_name);
//        mChatTitle.setText(mUserName);
//        mLastSeen = (TextView) findViewById(R.id.custom_bar_last_seen);
//        mChatImage = (CircleImageView) findViewById(R.id.custom_bar_profile_picture);
//        ImageView mChatMenu = (ImageView) findViewById(R.id.custom_bar_menu);
//        ImageView backArrow = (ImageView) findViewById(R.id.custom_bar_back_arrow);
//
//        mAttachFile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setAttachView();
//            }
//        });
//
//
//        backArrow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//
//        mChatMenu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PopupMenu options = new PopupMenu(mContext, mChatImage);
//                options.inflate(R.menu.chat_menu_options);
//                options.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        switch (item.getItemId()) {
//
//                            case R.id.view_profile:
//                                Intent i = new Intent(mContext, ShowingFriendsProfile.class);
//                                i.putExtra("user_id", mChatUser);
//                                startActivity(i);
//                                break;
//                        }
//                        return false;
//                    }
//                });
//                options.show();
//            }
//        });
//        loadMessages();
//        if (hidden) {
//            chatMessage.setFocusableInTouchMode(true);
//        }
//        chatMessage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!hidden) {
//                    setAttachView();
//                    chatMessage.setFocusableInTouchMode(true);
//                }
//            }
//        });
//
//        mRootRef.child("Chat").child(mCurrentUser).child(mChatUser).child("seen").setValue(true);
//        mRootRef.child(getString(R.string.dbname_verified_user)).child(mChatUser).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot != null) {
//
//                    String online = dataSnapshot.child("online").getValue().toString();
//                    final String thumb_img = dataSnapshot.child("profile_img_thumb").getValue().toString();
//                    Picasso.with(mContext).load(thumb_img).placeholder(R.drawable.student).networkPolicy(NetworkPolicy.OFFLINE).into(mChatImage, new Callback() {
//                        @Override
//                        public void onSuccess() {
//                        }
//
//                        @Override
//                        public void onError() {
//                            Picasso.with(mContext).load(thumb_img).placeholder(R.drawable.student).into(mChatImage);
//                        }
//                    });
//                    if (online.equals("true")) {
//                        mLastSeen.setText("Online");
//                    } else {
//
//                        GetTimeAgo getTimeAgo = new GetTimeAgo();
//                        long lastTime = Long.parseLong(online);
//                        String lastSeen = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
//                        if (lastSeen == null) {
//                            mLastSeen.setText(" ");
//                        } else {
//                            lastSeen = "Last seen " + lastSeen;
//                            mLastSeen.setText(lastSeen);
//                        }
//
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        mRootRef.child("Chat").child(mCurrentUser).addValueEventListener(new ValueEventListener() {
//
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                if (!dataSnapshot.hasChild(mChatUser)) {
//
//                    Map chatAddMap = new HashMap();
//                    chatAddMap.put("seen", false);
//                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
//
//                    Map chatUserMap = new HashMap();
//                    chatUserMap.put("Chat/" + mCurrentUser + "/" + mChatUser, chatAddMap);
//                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUser, chatAddMap);
//
//                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
//                        @Override
//                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//
//                            if (databaseError != null) {
//
//                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
//
//                            }
//
//                        }
//                    });
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//
//        chatSendBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendMessage();
//            }
//        });
//
//    }
//
//
//    private void loadMessages() {
//
//        mRootRef.child("messages").child(mCurrentUser).child(mChatUser).addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//
//                Log.d(TAG, "onChildAdded:  MESSAGE FROM FIREBASE " + dataSnapshot);
//
//                Messages message = dataSnapshot.getValue(Messages.class);
//                Log.d(TAG, "onChildAdded: MESSAGE DETAILS " + message);
//
////                if (message.getFrom().equals(mChatUser) && mLastSeen.getText().toString().equals("Online")) {
////                    MediaPlayer recievedMessage = MediaPlayer.create(mContext, R.raw.to_the_point);
////                    recievedMessage.start();
////                }
//                if(message != null){
//                    Log.d(TAG, "onChildAdded: MESSAGE VALUE "+ message.toString());
//
//                    if (message.getFrom().equals(mChatUser)) {
//                        mRootRef.child("messages").child(mChatUser).child(mCurrentUser).child(message.getPush_id())
//                                .child("seen").setValue(true);
//                    }
//                    messagesList.add(message);
//                    mAdapter.notifyDataSetChanged();
//                    mMessageList.scrollToPosition(messagesList.size() - 1);
//                }
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//    private void sendMessage() {
//        final MediaPlayer messagesentSound = MediaPlayer.create(mContext, R.raw.definite);
//        String message = chatMessage.getText().toString();
//
//        chatMessage.setText("");
//        if (!TextUtils.isEmpty(message)) {
//
//            String current_user_ref = "messages/" + mCurrentUser + "/" + mChatUser;
//            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUser;
//
//            DatabaseReference user_message_push = mRootRef.child("messages")
//                    .child(mCurrentUser).child(mChatUser).push();
//            String push_id = user_message_push.getKey();
//
//            mRootRef.child("Chat").child(mChatUser).child(mCurrentUser).child("seen").setValue(false);
//            mRootRef.child("Chat").child(mChatUser).child(mCurrentUser).child("timestamp").setValue(ServerValue.TIMESTAMP);
//
//            Map messageMap = new HashMap();
//            messageMap.put("message", message);
//            messageMap.put("from", mCurrentUser);
//            messageMap.put("type", "text");
//            messageMap.put("seen", false);
//            messageMap.put("time", ServerValue.TIMESTAMP);
//            messageMap.put("push_id", push_id);
//
//            Map messageUserMap = new HashMap();
//            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
//            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
//
//            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
//                @Override
//                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//
//                    if (databaseError != null) {
//                        Log.d(TAG, "onComplete: DATABASE ERROR" + databaseError.getMessage().toString());
//                    }
//                    messagesentSound.start();
//
//                }
//            });
//        }
//    }
//
//
//    /**
//     * check whether user is logged in or not
//     */
//    private void checkCurrentUser(FirebaseUser user) {
//        Log.d(TAG, "checkCurrentUser: checking user status of log in");
//        if (user == null) {
//            Intent intent = new Intent(getApplicationContext(), EntryActivity.class);
//            startActivity(intent);
//        } else {
//            myRef.child("online").setValue(true);
//        }
//    }
//
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        checkCurrentUser(mAuth.getCurrentUser());
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        myRef.child("online").setValue(ServerValue.TIMESTAMP);
//    }
//
//    /*
//     File attachment Chooser dialog animator
//     */
//    private void setAttachView() {
//        int cx = (mRevealItems.getLeft() + mRevealItems.getRight());
//
//        int cy = mRevealItems.getBottom();
//
//        int radius = Math.max(mRevealItems.getWidth(), mRevealItems.getHeight());
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//
//            SupportAnimator animator =
//                    ViewAnimationUtils.createCircularReveal(mRevealItems, cx, cy, 0, radius);
//            animator.setInterpolator(new AccelerateDecelerateInterpolator());
//            animator.setDuration(800);
//
//            SupportAnimator animator_reverse = animator.reverse();
//
//            if (hidden) {
//                mRevealItems.setVisibility(View.VISIBLE);
//                animator.start();
//                hidden = false;
//            } else {
//                animator_reverse.addListener(new SupportAnimator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart() {
//                    }
//
//                    @Override
//                    public void onAnimationEnd() {
//                        mRevealItems.setVisibility(View.INVISIBLE);
//                        hidden = true;
//                    }
//
//                    @Override
//                    public void onAnimationCancel() {
//                    }
//
//                    @Override
//                    public void onAnimationRepeat() {
//                    }
//                });
//
//                animator_reverse.start();
//            }
//        } else {
//            if (hidden) {
//                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealItems, cx, cy, 0, radius);
//                mRevealItems.setVisibility(View.VISIBLE);
//                anim.start();
//                hidden = false;
//
//            } else {
//                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealItems, cx, cy, radius, 0);
//                anim.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        super.onAnimationEnd(animation);
//                        mRevealItems.setVisibility(View.INVISIBLE);
//                        hidden = true;
//                    }
//                });
//                anim.start();
//
//            }
//        }
//
//
//    }
//
//    @Override
//    public void onNetworkConnectionChanged(boolean isConnected) {
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        builder.setTitle("No Internet Connection");
//        builder.setMessage("Make sure that you are connected to Internet.");
//        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                checkConnection();
//            }
//        });
//        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        if (!isConnected) {
//            //show a No Internet Alert or Dialog
//            builder.show();
//        } else {
//            // dismiss the dialog or refresh the activity
//            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    dialog.dismiss();
//                }
//            });
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        // register connection status listener
//        CollegeConexion.getInstance().setConnectionListener((ConnectionReceiver.ConnectionReceiverListener) this);
//    }
//
//    private void checkConnection() {
//        boolean isConnected = ConnectionReceiver.isConnected();
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        builder.setTitle("No Internet Connection");
//        builder.setMessage("Make sure that you are connected to Internet.");
//        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                checkConnection();
//            }
//        });
//        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        if (!isConnected) {
//            //show a No Internet Alert or Dialog
//            builder.show();
//        }
//    }
//
//    /**
//     * Chat attachments on Click function
//     *
//     * @param v
//     */
//    @Override
//    public void onClick(View v) {
//
//        switch (v.getId()) {
//            case R.id.camera_selection:
//                // Create or Open Directory for media files
//                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
//                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
//                }
//                break;
//            case R.id.gallery_selection:
//                Intent galleryIntent = new Intent();
//                galleryIntent.setType("image/*");
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_REQUEST_CODE);
//                break;
////            case R.id.audio_selection:
////                Intent audioIntent = new Intent();
////                audioIntent.setType("audio/mpeg");
////                audioIntent.setAction(Intent.ACTION_GET_CONTENT);
////                if (audioIntent.resolveActivity(mContext.getPackageManager()) != null) {
////                    startActivityForResult(Intent.createChooser(audioIntent, "SELECT AUDIO"), AUDIO_REQUEST_CODE);
////                }
////                break;
//            case R.id.document_selection:
//                //for greater than lolipop versions we need the permissions asked on runtime
//                //so if the permission is not available user will go to the screen to allow storage permission
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
//                        Manifest.permission.READ_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                            Uri.parse("package:" + getPackageName()));
//                    startActivity(intent);
//                    return;
//                }
//                //creating an intent for file chooser
//                Intent intent = new Intent();
//                intent.setType("application/pdf");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
//                    startActivityForResult(Intent.createChooser(intent, "SELECT PDF FILE"), PDF_REQUEST_CODE);
//                }
//                break;
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (!hidden) {
//            setAttachView();
//        }
//        //when the user choses the file
//        if (requestCode == PDF_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            //if a file is selected
//            if (data.getData() != null) {
//                //uploading the file
//                uploadPdfFile(data.getData());
//            } else {
//                Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
//            }
//        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
//            //uploading the captured file
//            //get the camera image
//            Bundle extras = data.getExtras();
//            final Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//            byte[] dataBAOS = baos.toByteArray();
//            uploadCameraImage(dataBAOS);
//
//        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            //if a Gallery Image is selected
//            if (data.getData() != null) {
//                //uploading the file
//                uploadGalleryImages(data.getData());
//            } else {
//                Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
//            }
////        }if (requestCode == AUDIO_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
////            //if a Gallery Image is selected
////            if (data.getData() != null) {
////                //uploading the file
////                uploadAudioFile(data.getData());
////            }else{
////                Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
////            }
////
//
//        }
//    }
//
//    /**
//     * Method to upload the pdf files to the server
//     *
//     * @param data
//     */
//    private void uploadPdfFile(Uri data) {
//        mProgress.setMessage("Uploading PDF File...");
//        mProgress.setCanceledOnTouchOutside(false);
//        mProgress.show();
//        final MediaPlayer messagesentSound = MediaPlayer.create(mContext, R.raw.definite);
//        StorageReference sRef = mChatAttachmentStorage.child("PDF").child(mCurrentUser).child(CheckInputs.random() + System.currentTimeMillis() + ".pdf");
//        sRef.putFile(data)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @SuppressWarnings("VisibleForTests")
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                        String current_user_ref = "messages/" + mCurrentUser + "/" + mChatUser;
//                        String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUser;
//
//                        DatabaseReference user_message_push = mRootRef.child("messages")
//                                .child(mCurrentUser).child(mChatUser).push();
//                        String push_id = user_message_push.getKey();
//
//                        mRootRef.child("Chat").child(mChatUser).child(mCurrentUser).child("seen").setValue(false);
//                        mRootRef.child("Chat").child(mChatUser).child(mCurrentUser).child("timestamp").setValue(ServerValue.TIMESTAMP);
//
//                        Map messageMap = new HashMap();
//                        messageMap.put("message", taskSnapshot.getStorage().getDownloadUrl().toString());
//                        messageMap.put("from", mCurrentUser);
//                        messageMap.put("type", "pdf");
//                        messageMap.put("seen", false);
//                        messageMap.put("time", ServerValue.TIMESTAMP);
//                        messageMap.put("push_id", push_id);
//
//                        Map messageUserMap = new HashMap();
//                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
//                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
//
//                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
//                            @Override
//                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                if (databaseError != null) {
//                                    Log.d(TAG, "onComplete: DATABASE ERROR" + databaseError.getMessage().toString());
//                                }
//                                messagesentSound.start();
//
//                                mProgress.dismiss();
//                            }
//                        });
//
//                    }
//                })
//                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//
//                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                        int progressCount = (int) progress;
//                        mProgress.setMessage("Sending Pdf \n" + progressCount + "% done");
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
//                    }
//                });
//
//
//    }
//
//    /**
//     * Upload the captured image to the firebase database
//     *
//     * @param dataBAOS
//     */
//    private void uploadCameraImage(byte[] dataBAOS) {
//        mProgress.setMessage("Uploading Image...");
//        mProgress.setCanceledOnTouchOutside(false);
//        mProgress.show();
//        final MediaPlayer messagesentSound = MediaPlayer.create(mContext, R.raw.definite);
//        DatabaseReference user_message_push = mRootRef.child("messages")
//                .child(mCurrentUser).child(mChatUser).push();
//        final String push_id = user_message_push.getKey();
//        final String imageFilename = push_id + ".jpg";
//        StorageReference imageStorage = mChatAttachmentStorage.child("Images").child(mCurrentUser).child(imageFilename);
//
//        //upload image
//        UploadTask uploadTask = imageStorage.putBytes(dataBAOS);
//        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                if (task.isSuccessful()) {
//                    String downloadUri = task.getResult().getStorage().getDownloadUrl().toString();
//                    final String current_user_ref = "messages/" + mCurrentUser + "/" + mChatUser;
//                    final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUser;
//
//                    Map messageMap = new HashMap();
//                    messageMap.put("message", downloadUri);
//                    messageMap.put("seen", false);
//                    messageMap.put("type", "image");
//                    messageMap.put("time", ServerValue.TIMESTAMP);
//                    messageMap.put("from", mCurrentUser);
//                    messageMap.put("push_id", push_id);
//
//                    Map messageUserMap = new HashMap();
//                    messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
//                    messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
//                    chatMessage.setText("");
//
//                    mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
//                        @Override
//                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                            if (databaseError != null) {
//
//                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
//                                Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
//
//                            }
//                            mProgress.dismiss();
//                            messagesentSound.start();
//                        }
//                    });
//
//                } else {
//                    Toast.makeText(mContext, "Uploading Failed ! ", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//
//                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                int progressCount = (int) progress;
//                mProgress.setMessage("Sending Image \n" + progressCount + "% done");
//
//            }
//        });
//        ;
//    }
//
//    /**
//     * Method to upload the gallery images f to the server
//     *
//     * @param data
//     */
//    private void uploadGalleryImages(Uri data) {
//        mProgress.setMessage("Uploading Image ...");
//        mProgress.setCanceledOnTouchOutside(false);
//        mProgress.show();
//        final MediaPlayer messagesentSound = MediaPlayer.create(mContext, R.raw.definite);
//        StorageReference sRef = mChatAttachmentStorage.child("Images").child(mCurrentUser).child(CheckInputs.random() + System.currentTimeMillis() + ".pdf");
//        sRef.putFile(data)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @SuppressWarnings("VisibleForTests")
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                        String current_user_ref = "messages/" + mCurrentUser + "/" + mChatUser;
//                        String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUser;
//
//                        DatabaseReference user_message_push = mRootRef.child("messages")
//                                .child(mCurrentUser).child(mChatUser).push();
//                        String push_id = user_message_push.getKey();
//
//                        mRootRef.child("Chat").child(mChatUser).child(mCurrentUser).child("seen").setValue(false);
//                        mRootRef.child("Chat").child(mChatUser).child(mCurrentUser).child("timestamp").setValue(ServerValue.TIMESTAMP);
//
//                        Map messageMap = new HashMap();
//                        messageMap.put("message", taskSnapshot.getStorage().getDownloadUrl().toString());
//                        messageMap.put("from", mCurrentUser);
//                        messageMap.put("type", "image");
//                        messageMap.put("seen", false);
//                        messageMap.put("time", ServerValue.TIMESTAMP);
//                        messageMap.put("push_id", push_id);
//
//                        Map messageUserMap = new HashMap();
//                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
//                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
//
//                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
//                            @Override
//                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                if (databaseError != null) {
//                                    Log.d(TAG, "onComplete: DATABASE ERROR" + databaseError.getMessage().toString());
//                                }
//                                mProgress.dismiss();
//                                messagesentSound.start();
//                            }
//                        });
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
//                    }
//                })
//                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//
//                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                        int progressCount = (int) progress;
//                        mProgress.setMessage("Sending \n" + progressCount + "% done");
//
//                    }
//                });
//        ;
//
//
//    }
//
////    /**
////     * Method to upload the Audio files to the server
////     * @param data
////     */
////    private void uploadAudioFile(Uri data) {
////        mProgress.setMessage("Uploading Audio...");
////        mProgress.setCanceledOnTouchOutside(false);
////        mProgress.show();
////        final MediaPlayer messagesentSound = MediaPlayer.create(mContext,R.raw.definite);
////        StorageReference sRef = mChatAttachmentStorage.child("Audio").child(mCurrentUser).child(CheckInputs.random()+ System.currentTimeMillis() + ".mp3");
////        sRef.putFile(data)
////                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
////                    @SuppressWarnings("VisibleForTests")
////                    @Override
////                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
////
////                        String current_user_ref = "messages/" + mCurrentUser + "/" + mChatUser;
////                        String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUser;
////
////                        DatabaseReference user_message_push = mRootRef.child("messages")
////                                .child(mCurrentUser).child(mChatUser).push();
////                        String push_id = user_message_push.getKey();
////
////                        mRootRef.child("Chat").child(mChatUser).child(mCurrentUser).child("seen").setValue(false);
////                        mRootRef.child("Chat").child(mChatUser).child(mCurrentUser).child("timestamp").setValue(ServerValue.TIMESTAMP);
////
////                        Map messageMap = new HashMap();
////                        messageMap.put("message", taskSnapshot.getDownloadUrl().toString());
////                        messageMap.put("from", mCurrentUser);
////                        messageMap.put("type", "audio");
////                        messageMap.put("seen", false);
////                        messageMap.put("time", ServerValue.TIMESTAMP);
////                        messageMap.put("push_id", push_id);
////
////                        Map messageUserMap = new HashMap();
////                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
////                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
////
////                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
////                            @Override
////                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
////                                if (databaseError != null) {
////                                    Log.d(TAG, "onComplete: DATABASE ERROR" + databaseError.getMessage().toString());
////                                }
////                                mProgress.dismiss();
////                                messagesentSound.start();
////                            }
////                        });
////
////                    }
////                })
////                .addOnFailureListener(new OnFailureListener() {
////                    @Override
////                    public void onFailure(@NonNull Exception exception) {
////                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
////                    }
////                })
////        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
////            @Override
////            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
////
////                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
////                int progressCount = (int) progress;
////                mProgress.setMessage("Sending Audio \n" + progressCount + "% done");
////
////            }
////        });
////
////
////    }
//
//}