package com.dk.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dk.App;
import com.dk.main.PollResultActivity;
import com.dk.main.R;
import com.dk.messages.DefaultMessagesActivity;
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
    DialogsListAdapter threadsListAdapter;
    private ArrayList<Thread> threads = new ArrayList<>();
    Box<Thread> threadBox = App.getInstance().getBoxStore().boxFor(Thread.class);


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
        threadsListAdapter.sortByLastMessageDate();
        threadsListView.setAdapter(threadsListAdapter);
        threadsListAdapter.setOnDialogClickListener(this);
        threadsListAdapter.setOnDialogLongClickListener(this);

        // Inflate the layout for this fragment
        return rootView;
    }


    @Override
    public void onDialogClick(Thread thread) {
        if (thread.getOptionString() ==null) {
            thread.setUnreadCount(0);
            threadBox.put(thread);
            threadsListAdapter.updateItemById(thread);
            Intent intent = new Intent(getActivity(), DefaultMessagesActivity.class);
            intent.putExtra("threadID", thread.getT_id());
            startActivity(intent);
        }
        else if (thread.getResultCount() > 0){
            thread.setUnreadCount(0);
            threadBox.put(thread);
            threadsListAdapter.updateItemById(thread);
            Intent intent = new Intent(getActivity(), PollResultActivity.class);
            intent.putExtra("threadID", thread.getT_id());
            startActivity(intent);
        }
        else {
            Toast.makeText(getActivity(), "No one replied!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDialogLongClick(Thread thread) {
        Log.e(">>>>>>>>>>.", "Menu options");

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
        if (event.type == 0) {
            threadsListAdapter.addItem(event.thread);
        }
        threadsListAdapter.updateItemById(event.thread);
    }

}


