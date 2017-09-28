package com.dk.tagging;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dk.App;
import com.dk.main.R;
import com.dk.models.Bucket;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;

import io.objectbox.Box;

/**
 * A simple {@link Fragment} subclass.
 */
public class BucketFragment extends Fragment {
    NachoTextView nachoTextView;
    View rootView;
    private long bucketId;

    public BucketFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_bucket, container, false);
        nachoTextView = rootView.findViewById(R.id.nacho_text_view);
        nachoTextView.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
        nachoTextView.setIllegalCharacters(',', '~', '`', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')',
                '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '<', '>', '?', '/', '.', '-', '_', '+', '=',
                '[', '{', '}', ']', '|');
        bucketId = getArguments().getLong("bucketId");
        Box<Bucket> bucketBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Bucket.class);
        Bucket bucket = bucketBox.get(bucketId);
        Log.d(">>>>>>>>>>", String.valueOf(bucket.tags.size()) + ">>>>" + (bucket.getId()));
        nachoTextView.setText(bucket.getTagsString());
        return rootView;
    }

    public long getBucketId() {
        return bucketId;
    }
}