package com.dk.tagging;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dk.App;
import com.dk.main.R;
import com.dk.models.Bucket;

import io.objectbox.Box;


public class ProfileFragment extends Fragment {
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView buckName = rootView.findViewById(R.id.buckName);
        long bucketId = getArguments().getLong("bucketId");
        Box<Bucket> bucketBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Bucket.class);
        Bucket bucket = bucketBox.get(bucketId);
        buckName.setText(bucket.getName());
        // Inflate the layout for this fragment
        return rootView;
    }


}
