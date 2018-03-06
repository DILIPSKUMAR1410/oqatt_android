package com.dk.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dk.App;
import com.dk.main.R;
import com.dk.models.Poll;
import com.dk.models.Poll_;
import com.dk.queue.RemovePoll;
import com.dk.queue.UpdatePoll;
import com.ramotion.foldingcell.FoldingCell;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import io.objectbox.Box;


public class IncomingPollFragment extends Fragment {

    View rootView;
    IpFoldingCellListAdapter adapter;
    ListView theListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_incoming_poll, container, false);

        // get our list view
        theListView = rootView.findViewById(R.id.mainListView);

        // prepare elements to display
        Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);

        final List<Poll> incomingPolls = pollBoxBox.query().equal(Poll_.type, 1).build().find();
        Collections.reverse(incomingPolls);

        // create custom adapter that holds elements and their state (we need hold a id's of unfolded elements for reusable elements)

        adapter = new IpFoldingCellListAdapter(getContext(), incomingPolls);

        // set elements to adapter
        theListView.setAdapter(adapter);

        // set on click event listener to list view
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                // toggle clicked op_cell state
                ((FoldingCell) view).toggle(false);
                // register in adapter that state for selected op_cell is toggled
                adapter.registerToggle(pos);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
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

    // This method will be called when a RefreshEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnRemovePoll(RemovePoll event) {
        adapterDataSetChange();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdatePoll(UpdatePoll event) {
        adapterDataSetChange();
    }

    public void adapterDataSetChange(){
        adapter.clear();
        Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);
        List<Poll> incomingPolls = pollBoxBox.query().equal(Poll_.type, 1).build().find();
        Collections.reverse(incomingPolls);
        adapter.addAll(incomingPolls);
        adapter.notifyDataSetChanged();

    }
}