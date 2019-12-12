package com.cloudiera.collegeconexion.Home;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.CollegeConexion;
import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Utils.CheckInputs;
import com.cloudiera.collegeconexion.ConnectionReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

@SuppressWarnings("VisibleForTests")
public class PostActivity extends AppCompatActivity  implements ConnectionReceiver.ConnectionReceiverListener {

    private Context mContext = PostActivity.this;
    private static final String TAG = "PostActivity";
    private static final int GALLERY_PICK = 1;
    private AppCompatButton mPostButton;
    private EditText mDescription;
    private String mCurrentUser;
    private ProgressDialog mProgressDialog;
    // Firebase Variables
    private FirebaseAuth mAuth;
    private StorageReference mUserPostStorage;
    private DatabaseReference mUserPostDatabase;
    private ImageView postImage;
    private TextView imageText;
    private  byte[] thumb_byte;
    private Uri mImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


        checkConnection();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();
        mUserPostStorage = FirebaseStorage.getInstance().getReference();
        mUserPostDatabase = FirebaseDatabase.getInstance().getReference();
        mUserPostDatabase.keepSynced(true);
        setupWidgets();

    }
    /*Setup widgets used in post activity
       set OnClickListener on the widgets
        */
    private void setupWidgets() {
        mDescription = (EditText) findViewById(R.id.post_description);
        postImage = (ImageView) findViewById(R.id.post_image);
        imageText = (TextView)findViewById(R.id.post_image_gallery) ;
        imageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);

            }
        });
        mPostButton = (AppCompatButton) findViewById(R.id.postBtn);
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });

    }
    /*
     Send Post to the Database
     */
    private void startPosting() {

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage("Posting to your wall...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        final String description = mDescription.getText().toString().trim();
        String randomString = CheckInputs.random();
        Log.d(TAG, "startPosting: RANDOM :: " + randomString);
        if (randomString.equals("")) {
            for (; randomString.equals(""); ) {
                Log.d(TAG, "startPosting: " + randomString);
                randomString = CheckInputs.random();
            }
        }

        StorageReference filePath;
        filePath = mUserPostStorage.child("user_post_images").child(mCurrentUser).child(randomString);
        if (!TextUtils.isEmpty(description) && mImageUri != null) {
            // Image With Description going to be posted
            // Image is going to be posted
            UploadTask uploadTask  = filePath.putBytes(thumb_byte);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(mContext, " ERROR!\n Failed to upload post", Toast.LENGTH_SHORT).show();
                    }
                    UploadTask.TaskSnapshot taskUri = task.getResult();
                    String downloadUri = task.getResult().getStorage().getDownloadUrl().toString();
                    //Uplaod post to the database
                    DatabaseReference newPost = mUserPostDatabase.child("user_post").child("rse001").push();
                    newPost.child("image_uri").setValue(downloadUri);
                    newPost.child("desc").setValue(description);
                    newPost.child("timestamp").setValue(ServerValue.TIMESTAMP);
                    newPost.child("uid").setValue(mCurrentUser);
                    DatabaseReference userNewPost = mUserPostDatabase.child("user_post").child("users")
                            .child(mAuth.getCurrentUser().getUid());
                    userNewPost.child(newPost.getKey()).child("timestamp").setValue(ServerValue.TIMESTAMP);
                    mProgressDialog.dismiss();
                    Toast.makeText(mContext, "Successfully Posted to Your Wall", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });


        } else if (TextUtils.isEmpty(description) && mImageUri != null) {

            // Image is going to be posted
            UploadTask uploadTask  = filePath.putBytes(thumb_byte);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(mContext, " ERROR!\n Failed to upload post", Toast.LENGTH_SHORT).show();
                    }

                    String downloadUri = task.getResult().getStorage().getDownloadUrl().toString();
                    //Uplaod post to the database
                    DatabaseReference newPost = mUserPostDatabase.child("user_post").child("rse001").push();
                    newPost.child("image_uri").setValue(downloadUri);
                    newPost.child("desc").setValue("");
                    newPost.child("timestamp").setValue(ServerValue.TIMESTAMP);
                    newPost.child("uid").setValue(mCurrentUser);
                    DatabaseReference userNewPost = mUserPostDatabase.child("user_post").child("users")
                            .child(mAuth.getCurrentUser().getUid());
                    userNewPost.child(newPost.getKey()).child("timestamp").setValue(ServerValue.TIMESTAMP);
                    mProgressDialog.dismiss();
                    Toast.makeText(mContext, "Successfully Posted to Your Wall", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

        } else if (!TextUtils.isEmpty(description) && mImageUri == null) {
            // Description  is going to be posted

            //Uplaod post to the database
            DatabaseReference newPost = mUserPostDatabase.child("user_post").child("rse001").push();
            newPost.child("desc").setValue(description);
            newPost.child("image_uri").setValue("none");
            newPost.child("timestamp").setValue(ServerValue.TIMESTAMP);
            newPost.child("uid").setValue(mCurrentUser);
            DatabaseReference userNewPost = mUserPostDatabase.child("user_post").child("users")
                    .child(mAuth.getCurrentUser().getUid());
            userNewPost.child(newPost.getKey()).child("timestamp").setValue(ServerValue.TIMESTAMP);
            mProgressDialog.dismiss();
            Toast.makeText(mContext, "Successfully Posted to Your Wall", Toast.LENGTH_SHORT).show();
            finish();

        } else if (mImageUri == null && TextUtils.isEmpty(description)) {
            //Null Post
            mProgressDialog.dismiss();
            Toast.makeText(mContext, "Empty Post", Toast.LENGTH_SHORT).show();
        }


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                    .setMinCropWindowSize(300, 300)
                    .start(PostActivity.this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {

                    Bitmap thumb_bitmap = new Compressor(this)
                            .setQuality(70)
                            .compressToBitmap(thumb_filePath);
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                } catch (IOException e) {
                    Log.d(TAG, "onActivityResult:  Exception is Going on ");
                    e.printStackTrace();
                }

                thumb_byte = baos.toByteArray();
                postImage.setImageURI(resultUri);

            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, "onActivityResult: ERROR : "+error);
            }
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


}
