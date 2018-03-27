package com.dk.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dk.App;
import com.dk.main.R;
import com.dk.models.Anonymous;
import com.dk.models.Thread;
import com.dk.queue.UpdateThread;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import io.objectbox.Box;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThreadFragment extends Fragment implements DialogsListAdapter.OnDialogClickListener<Thread>,
        DialogsListAdapter.OnDialogLongClickListener<Thread> {

    protected ImageLoader imageLoader;
    View rootView;
    private ArrayList<Thread> threads = new ArrayList<>();
    private ArrayList<Anonymous> participants = new ArrayList<>();
    DialogsListAdapter threadsListAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_thread, container, false);
        DialogsList threadsListView = (DialogsList) rootView.findViewById(R.id.threadsList);

        imageLoader = (imageView, url) -> Picasso.with(getActivity()).load(url).into(imageView);
        threadsListAdapter = new DialogsListAdapter<>(imageLoader);

        Box<Thread> threadBox = App.getInstance().getBoxStore().boxFor(Thread.class);
        threads = (ArrayList<Thread>) threadBox.query().build().find();

        threadsListAdapter.addItems(threads);

        threadsListView.setAdapter(threadsListAdapter);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onDialogClick(Thread dialog) {

    }

    @Override
    public void onDialogLongClick(Thread dialog) {

    }
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateThread(UpdateThread event) {
        threadsListAdapter.addItem(event.thread);
        threadsListAdapter.notifyDataSetChanged();
    }

}


