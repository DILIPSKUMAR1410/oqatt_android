//package com.dk.tagging;
//
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//
//import com.airbnb.lottie.LottieAnimationView;
//import com.bumptech.glide.Glide;
//import com.dk.App;
//import com.dk.main.R;
//import com.dk.models.Bucket;
//import com.dk.models.Tag;
//import com.dk.models.Tag_;
//import com.google.android.flexbox.FlexboxLayout;
//
//import java.util.List;
//
//import fisk.chipcloud.ChipCloud;
//import fisk.chipcloud.ChipCloudConfig;
//import fisk.chipcloud.ChipDeletedListener;
//import io.objectbox.Box;
//
///**
// * A simple {@link Fragment} subclass.
// */
//public class BucketFragment extends Fragment {
//    View rootView;
//    Bucket bucket;
//    Box<Tag> tagBox;
//    Box<Bucket> bucketBox;
//
//    public BucketFragment() {
//        // Required empty public constructor
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        rootView = inflater.inflate(R.layout.fragment_bucket, container, false);
//        Glide.with(getActivity()).load("https://goo.gl/N3JwQG").into((ImageView) rootView.findViewById(R.id.bulb));
//        final long bucketId = getArguments().getLong("bucketId");
//        bucketBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Bucket.class);
//        bucket = bucketBox.get(bucketId);
//        tagBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Tag.class);
//        //Deleteable
//        FlexboxLayout deleteableFlexbox = rootView.findViewById(R.id.flexbox_deleteable);
//        deleteableFlexbox.setVisibility(View.INVISIBLE);
//        LottieAnimationView animationView = rootView.findViewById(R.id.animation_view);
//        animationView.setAnimation("newAnimation.json");
//        animationView.loop(true);
//        animationView.playAnimation();
//
//        ChipCloudConfig deleteableConfig = new ChipCloudConfig()
//                .uncheckedChipColor(Color.parseColor("#20ACF7"))
//                .showClose(Color.parseColor("#FFFFFF"), 500);;
//
//        deleteableCloud = new ChipCloud(getActivity(), deleteableFlexbox, deleteableConfig);
//        deleteableCloud.addChips(bucket.getTagLableList());
//
//        deleteableCloud.setDeleteListener(new ChipDeletedListener() {
//            @Override
//            public void chipDeleted(int index, String tag_lable) {
//                bucket.tags.remove(index);
//                bucketBox.put(bucket);
//            }
//        });
//
//        return rootView;
//    }
//
//    public Bucket getBucket() {
//        return bucket;
//    }
//
//    public void addTag(String tag_lable) {
//        deleteableCloud.addChip(tag_lable);
//        Tag tag;
//        List<Tag> tagList = tagBox.find(Tag_.name, tag_lable);
//        if (tagList.isEmpty()){
//            tag= new Tag();
//            tag.setName(tag_lable);
//            tagBox.put(tag);
//        }
//        else {
//            tag = tagList.get(0);
//        }
//        bucket.tags.add(tag);
//        bucketBox.put(bucket);
//    }
//
//
//}