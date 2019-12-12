package com.cloudiera.collegeconexion.NoticeBox;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("VisibleForTests")
public class CreateNoticeActivity extends AppCompatActivity {

    private static final String TAG = "CreateNoticeActivity";
    private Context mContext = CreateNoticeActivity.this;


    private Uri resultImageUri;
    private ArrayList<String> tags;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_notice);
         mAuth = FirebaseAuth.getInstance();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setNoticeAttachment();
        createNotice();

    }


    private void createNotice(){

        final EditText noticeHeading, noticeDescription;
        noticeHeading =  (EditText)findViewById(R.id.create_notice_heading);
        noticeDescription = (EditText)findViewById(R.id.create_notice_description);
        Button createNoticeButton = (Button)findViewById(R.id.create_notice_button);

        createNoticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNoticeTags();

                final ProgressDialog mProgress = new ProgressDialog(mContext);
                mProgress.setTitle("New Notice");
                mProgress.setMessage("Uploading Notice");
                mProgress.setCancelable(false);
                mProgress.setCanceledOnTouchOutside(false);

                if(!noticeHeading.getText().toString().equals("")){
                    if(!noticeDescription.getText().toString().equals("")){
                        if(!tags.isEmpty()){
                            mProgress.show();
                            final DatabaseReference noticeDatabase = FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.dbname_notice_box)).child("rse001");

                            final String push_key = noticeDatabase.push().getKey();

                            final Map noticePost = new HashMap();
                            noticePost.put("timestamp", ServerValue.TIMESTAMP);
                            noticePost.put("user_id",mAuth.getCurrentUser().getUid());
                            noticePost.put("description",noticeDescription.getText().toString().trim());
                            noticePost.put("heading",noticeHeading.getText().toString().trim());
                            noticePost.put("tags",tags);
                            noticePost.put("push_key",push_key);

                            if(resultImageUri == null){

                                noticePost.put("type","text");
                                noticePost.put("imageUri"," ");
                                noticeDatabase.child("pending_notice").child(push_key).updateChildren(noticePost, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        Toast.makeText(mContext, "Notice  Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                       mProgress.dismiss();
                                        finish();
                                    }
                                });

                            }else{

                                final StorageReference noticeStorage = FirebaseStorage.getInstance().getReference()
                                        .child("notice_box").child(push_key +".jpg");
                                mProgress.setMessage("Uploading Image");
                                noticeStorage.putFile(resultImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                        if(task.isSuccessful()){
                                            noticePost.put("type","image");

                                            UploadTask.TaskSnapshot taskUri = task.getResult();

                                            noticePost.put("imageUri",taskUri.getStorage().getDownloadUrl().toString());

                                            noticeDatabase.child("pending_notice").child(push_key).updateChildren(noticePost, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    Toast.makeText(mContext, "Notice  Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                                     mProgress.dismiss();
                                                    finish();
                                                }
                                            });

                                        }else{
                                            mProgress.dismiss();
                                            Toast.makeText(mContext, "Image Uploading Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });




                            }
                        }else{
                            Toast.makeText(mContext, "Give Proper Tags for Notice", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        noticeDescription.setError("Fill the Discription");
                    }
                }else{
                    noticeHeading.setError("Fill The Heading");
                }
            }
        });



    }


    /**
     * Get The Tags for The Notice
     */
    private void getNoticeTags(){

        CheckBox Iyear,IIyear,IIIyear,finalYear;
        Iyear = (CheckBox)findViewById(R.id.firstYearCheckBox);
        IIyear = (CheckBox)findViewById(R.id.secondYearCheckBox);
        IIIyear = (CheckBox)findViewById(R.id.thirdYearCheckBox);
        finalYear = (CheckBox)findViewById(R.id.finalYearCheckBox);

        tags = new ArrayList<String>();

        if(Iyear.isChecked()){
            tags.add("Ist Year");
        }
        if(IIyear.isChecked()){
            tags.add("IInd Year");
        }
        if(IIIyear.isChecked()){
            tags.add("IIIrd Year");
        }
        if(finalYear.isChecked()){
            tags.add("Final Year");
        }

    }

    /**
     * Set Attachments for the View
     */
    private void setNoticeAttachment() {
        TextView imageButton = (TextView) findViewById(R.id.create_notice_image);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Starting chooser for Gallery activity");
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start((Activity) mContext);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                resultImageUri = result.getUri();
                ImageView noticeImage = (ImageView)findViewById(R.id.create_notice_image_attachment);
                noticeImage.setImageURI(resultImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }


}



