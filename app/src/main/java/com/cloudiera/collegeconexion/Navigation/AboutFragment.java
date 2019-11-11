package com.cloudiera.collegeconexion.Navigation;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudiera.collegeconexion.R;
import com.cloudiera.collegeconexion.Resources.AcademicsDataActivity;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {

    ListView listView;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

         listView = (ListView) view.findViewById(R.id.about_us_listView);


        ArrayList<String> options = new ArrayList<>();

        options.add(getString(R.string.terms_of_use));
        options.add(getString(R.string.acknowledgement));

        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: Navigation as per response");

                switch(position){
                    case 0:
                        Dialog termsDialog = new Dialog(getContext());
                        termsDialog.setContentView(R.layout.layout_terms_of_use_dialog);
                        termsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        termsDialog.show();
                        break;
                    case 1 :
                        Dialog acknowledgementDialog = new Dialog(getContext());
                        acknowledgementDialog.setContentView(R.layout.layout_acknowledgement_dialog);
                        acknowledgementDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        acknowledgementDialog.show();
                        break;

                }
            }
        });

        return view;

    }



}
