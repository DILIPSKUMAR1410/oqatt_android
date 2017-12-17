package com.dk.tagging;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dk.App;
import com.dk.main.R;
import com.dk.models.Poll;
import com.dk.models.Poll_;
import com.ramotion.foldingcell.FoldingCell;

import java.util.List;

import io.objectbox.Box;


public class OutgoingPollFragment extends Fragment {

    View rootView;
    OpFoldingCellListAdapter adapter;
    ListView theListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_outgoing_poll, container, false);

        // get our list view
        theListView = rootView.findViewById(R.id.mainListView);

        // prepare elements to display
        Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);

        final List<Poll> outgoingPolls = pollBoxBox.query().equal(Poll_.type, 0).build().find();
        Log.d(">>>>>>>>.", String.valueOf(outgoingPolls.size()));

        // create custom adapter that holds elements and their state (we need hold a id's of unfolded elements for reusable elements)

        adapter = new OpFoldingCellListAdapter(getContext(), outgoingPolls);

        // set elements to adapter
        theListView.setAdapter(adapter);

        // set on click event listener to list view
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                // toggle clicked op_cell state
                ((FoldingCell) view).toggle(false);
                Log.d(">>>>>>>>CELLNO.", String.valueOf(pos));
                // register in adapter that state for selected op_cell is toggled
                adapter.registerToggle(pos);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }
}