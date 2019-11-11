package com.cloudiera.collegeconexion.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.cloudiera.collegeconexion.Models.CollegeProfile;
import com.cloudiera.collegeconexion.Models.StudentProfile;
import com.cloudiera.collegeconexion.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;

import static android.R.attr.value;
import static com.cloudiera.collegeconexion.R.string.common_google_play_services_unsupported_text;
import static com.cloudiera.collegeconexion.R.string.profile_name;

/**
 * Created by HP on 05-Dec-17.
 */
public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";
    private Context mContext;
    // FireBase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabse;
    private DatabaseReference myRef;
    private String userID;

    public FirebaseMethods(Context mContext) {
        this.mContext = mContext;
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabse = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabse.getReference();
        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    /**
     * Update the Student profile data
     *
     * @param field of Data
     * @param value value of field
     */
    public void updateStudentProfile(String field, String value) {
        Log.d(TAG, "updateStudentProfile:  Updating " + field + " as " + value);
        if (value != null) {
            myRef.child(mContext.getString(R.string.dbname_verified_user))
                    .child(userID)
                    .child(field)
                    .setValue(value);
        }
    }

    /**
     * Update the email of the user in the database
     *
     * @param email
     */
    public void updateEmail(String email) {
        Log.d(TAG, "updateEmail: Updating  email to: " + email);

        myRef.child(mContext.getString(R.string.dbname_verified_user))
                .child(userID)
                .child(mContext.getString(R.string.dbField_email))
                .setValue(email);

    }

    /**
     * Register New Email and password in the firebase Authentication
     * @param email
     * @param password
     */
    public void registerNewEmail(final String email, final String password) {

        final ProgressDialog progressDialog  = new ProgressDialog(mContext);
        progressDialog.setMessage("Creating CC Account");
        progressDialog.setTitle("Reserving Space For You");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(mContext, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        } else if (task.isSuccessful()) {

                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: Authstate Changed :: " + userID);
                            progressDialog.dismiss();

                        }


                    }
                });
    }

    /**
     * Send verification email to verify email of the user
     */
    public void sendVerificationEmail() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(mContext, "couldn't send verification email.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * Add New Student Under the database of College in firebase
     *
     * @param profile_name
     * @param profile_image
     * @param bio
     * @param course
     * @param branch
     * @param roll_no
     * @param dob
     * @param gender
     * @param phone_number
     * @param email
     */
    public void addNewUser(String college, String profile_name, String profile_image, String bio, String course,
                           String branch, String roll_no, String dob, String gender, String phone_number,
                           String email, String userId,boolean verified) {

        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        StudentProfile studentNewProfile = new StudentProfile(profile_name, profile_image,"none",
                bio, course, branch, roll_no, dob, gender, phone_number,
                email, college, userId, deviceToken,verified);

        myRef.child(mContext.getString(R.string.dbname_pending_user))
                .child(userId)
                .setValue(studentNewProfile);

    }


    /**
     * Retrieve Information of the student form the firebase database
     * Database : student_profile Node
     *
     * @param dataSnapshot
     * @return
     */
    public StudentProfile getStudentProfile(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getProfile: Retrieve Information from the data base");

        StudentProfile student = new StudentProfile();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            if (ds.getKey().equals(mContext.getString(R.string.dbname_verified_user))) {
                try {
                    Log.d(TAG, "getStudentProfile: datasnapshot: " + ds);
                    Log.d(TAG, "getStudentProfile: datasnapshot: " + ds.child(userID));
                    Log.d(TAG, "getStudentProfile: datasnapshot: " + ds.child(userID).getValue(StudentProfile.class));

                    student.setProfile_name(
                            ds.child(userID)
                                    .getValue(StudentProfile.class)
                                    .getProfile_name()
                    );

                    student.setProfile_image(
                            ds.child(userID)
                                    .getValue(StudentProfile.class)
                                    .getProfile_image()
                    );
                    student.setBio(
                            ds.child(userID)
                                    .getValue(StudentProfile.class)
                                    .getBio()
                    );
                    student.setCourse(
                            ds.child(userID)
                                    .getValue(StudentProfile.class)
                                    .getCourse()
                    );
                    student.setBranch(
                            ds.child(userID)
                                    .getValue(StudentProfile.class)
                                    .getBranch()
                    );
                    student.setDob(
                            ds.child(userID)
                                    .getValue(StudentProfile.class)
                                    .getDob()
                    );
                    student.setGender(
                            ds.child(userID)
                                    .getValue(StudentProfile.class)
                                    .getGender()
                    );
                    student.setRoll_no(
                            ds.child(userID)
                                    .getValue(StudentProfile.class)
                                    .getRoll_no()
                    );
                    student.setPhone_number(
                            ds.child(userID)
                                    .getValue(StudentProfile.class)
                                    .getPhone_number()
                    );
                    student.setEmail(
                            ds.child(userID)
                                    .getValue(StudentProfile.class)
                                    .getEmail()
                    );
                    student.setCollege_id(
                            ds.child(userID)
                                    .getValue(StudentProfile.class)
                                    .getCollege_id());
                } catch (NullPointerException e) {
                    Log.d(TAG, "getStudentProfile: Null Pointer Exception " + e.getMessage());
                }
            }

        }

        return student;
    }

    /**
     * Retrieve Information of the College form the firebase database
     * Database : College_profile Node
     *
     * @param dataSnapshot
     * @return
     */
    public CollegeProfile getCollegeProfile(DataSnapshot dataSnapshot, String collegeId) {
        Log.d(TAG, "getCollegeProfile: getting College Profile from Firebase database");
        CollegeProfile college = new CollegeProfile();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            if (ds.getKey().equals(mContext.getString(R.string.dbname_college_profile))) {
                try {
                    Log.d(TAG, "getCollegeProfile: Retrieve College Information");
                    college.setCollege_code(
                            ds.child(collegeId)
                                    .getValue(CollegeProfile.class)
                                    .getCollege_code()
                    );
                    college.setCollege_logo(
                            ds.child(collegeId)
                                    .getValue(CollegeProfile.class)
                                    .getCollege_logo()
                    );
                    college.setCollege_Name(
                            ds.child(collegeId)
                                    .getValue(CollegeProfile.class)
                                    .getCollege_Name()
                    );
                } catch (NullPointerException e) {
                    Log.d(TAG, "getCollegeProfile: Null Pointer Exception" + e.getMessage());
                }
            }
        }
        return college;
    }



}
