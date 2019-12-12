package com.cloudiera.collegeconexion.Navigation;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.Profile.EditProfileActivity;
import com.cloudiera.collegeconexion.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private ListView mSettingsItemList;
    private FirebaseAuth mAuth;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View mView = inflater.inflate(R.layout.fragment_privacy_options, container, false);

        mAuth = FirebaseAuth.getInstance();
        mSettingsItemList = (ListView)mView.findViewById(R.id.settings_view);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.edit_profile)); // Index 0
        options.add(getString(R.string.privacy));// Index 1
        options.add(getString(R.string.change_password));// Index 2
        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, options);

        mSettingsItemList.setAdapter(adapter);

        mSettingsItemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d(TAG, "onItemClick: Navigationg to the settings item");
                 switch(position){
                     case 0 :
                         Intent i = new Intent(getContext(),EditProfileActivity.class);
                         startActivity(i);break;
                     case 1 :
                         Intent i2 = new Intent(getContext(),PrivacySettingsActivity.class);
                         startActivity(i2);break;
                     case 2 :
                         // Part 1)Confirming the changes for authenticated email
                         AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                         builder.setTitle("Verify Current Password");
                         // Set up the input
                         final EditText inputPassword = new EditText(getContext());
                         // Specify the type of input expected;
                         // this, for example, sets the input as a password, and will mask the text
                         inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                         builder.setView(inputPassword);
                         builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 String inputPass = null;
                                 inputPass = inputPassword.getText().toString();
                                 if (inputPass != null) {
                                     final ProgressDialog progressDialog = new ProgressDialog(getContext());
                                     progressDialog.setTitle("Please Wait");
                                     progressDialog.setMessage("Re-Authenticating");
                                     progressDialog.setCancelable(true);
                                     progressDialog.show();
                                     AuthCredential credential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(), inputPass);
                                     mAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task) {
                                             if(task.isSuccessful()){
                                                 Intent i = new Intent(getContext(),ChangePasswordActivity.class);
                                                 startActivity(i);
                                                 progressDialog.dismiss();
                                             }else{
                                                 inputPassword.setError("Wrong Password");
                                                 getActivity().finish();
                                                 Toast.makeText(getContext(), "Wrong Password", Toast.LENGTH_SHORT).show();
                                             }
                                         }
                                     });
                                 }else{
                                     inputPassword.setError("Please Enter Password");
                                 }
                             }
                         });
                         builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 dialog.cancel();
                             }
                         });
                         builder.show();
                         break;
                 }
            }
        });

        return mView;

    }

}
