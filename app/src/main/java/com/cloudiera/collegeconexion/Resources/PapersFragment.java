package com.cloudiera.collegeconexion.Resources;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudiera.collegeconexion.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PapersFragment extends Fragment {


    public PapersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_papers, container, false);
    }

}
