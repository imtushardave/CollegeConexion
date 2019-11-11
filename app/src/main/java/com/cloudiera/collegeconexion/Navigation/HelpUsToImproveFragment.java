package com.cloudiera.collegeconexion.Navigation;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cloudiera.collegeconexion.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HelpUsToImproveFragment extends Fragment {

    private static final String TAG = "HelpUsToImproveFragment";

    private EditText mFeedback;
    private Button submitFeedback;


    public HelpUsToImproveFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help_us_to_improve, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mFeedback = (EditText)view.findViewById(R.id.feedback);
        submitFeedback =(Button)view.findViewById(R.id.feedback_submit);

        submitFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String feedback = mFeedback.getText().toString().trim();
                mFeedback.setText("");
                if(!feedback.equals("")){
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"info@collegeconexion.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT,"Feedback and Suggestions");
                    intent.putExtra(Intent.EXTRA_TEXT, feedback);
                    if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }else{
                    Toast.makeText(getContext(), "Empty Feedback", Toast.LENGTH_SHORT).show();
                }


            }
        });


        return view;
    }

}
