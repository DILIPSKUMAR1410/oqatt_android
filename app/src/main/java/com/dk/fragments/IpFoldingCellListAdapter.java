package com.dk.fragments;

/**
 * Created by dk on 09/12/17.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dk.graph.ApiCalls;
import com.dk.main.R;
import com.dk.models.Poll;
import com.ramotion.foldingcell.FoldingCell;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Simple example of ListAdapter for using with Folding Cell
 * Adapter holds indexes of unfolded elements for correct work with default reusable views behavior
 */
public class IpFoldingCellListAdapter extends ArrayAdapter<Poll> {

    private HashSet<Integer> unfoldedIndexes = new HashSet<>();

    public IpFoldingCellListAdapter(Context context, List<Poll> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // get item for selected view
        final Poll poll = getItem(position);
        // if op_cell is exists - reuse it, if not - create the new one from resource
        FoldingCell cell = (FoldingCell) convertView;

        final ViewHolder viewHolder;
        if (cell == null) {
            viewHolder = new ViewHolder();
            LayoutInflater vi = LayoutInflater.from(getContext());
            cell = (FoldingCell) vi.inflate(R.layout.ip_cell, parent, false);
            // binding view parts to view holder
            viewHolder.question = cell.findViewById(R.id.question);
            viewHolder.fold = cell.findViewById(R.id.button2);
            viewHolder.radioGroup = cell.findViewById(R.id.rg);
            viewHolder.vote = cell.findViewById(R.id.vote_button);
            ArrayList<String> options = poll.getOptionsList();
            for (int i = 0; i < options.size(); i++) {
                String RadioButtonID = "op" + i;
//                String CardID = "cop" + i;
                int RadioButtonresID = getContext().getResources().getIdentifier(RadioButtonID, "id", getContext().getPackageName());
//                int cardresID = getContext().getResources().getIdentifier(CardID, "id", getContext().getPackageName());
                RadioButton radioButton = cell.findViewById(RadioButtonresID);
//                CardView card = cell.findViewById(cardresID);
//                card.setVisibility(View.VISIBLE);
                radioButton.setText(options.get(i));
                radioButton.setVisibility(View.VISIBLE);
            }


            cell.setTag(viewHolder);
        } else {
            // for existing op_cell set valid valid state(without animation)
            if (unfoldedIndexes.contains(position)) {
                cell.unfold(true);
            } else {
                cell.fold(true);
            }
            viewHolder = (ViewHolder) cell.getTag();
        }

        viewHolder.question.setText(poll.getQuestion());
        final FoldingCell finalCell = cell;
        viewHolder.fold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // toggle clicked op_cell state
                finalCell.toggle(false);
                // register in adapter that state for selected op_cell is toggled
                registerToggle(position);
            }
        });

        viewHolder.vote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // toggle clicked op_cell state
                if (viewHolder.radioGroup.getCheckedRadioButtonId() == -1)
                {
                    // no radio buttons are checked
                    return;
                }
                finalCell.toggle(false);
                String s = getContext().getResources().getResourceEntryName(viewHolder.radioGroup.getCheckedRadioButtonId());
                int result = Integer.parseInt(s.substring(2));
                try {
                    ApiCalls.votePoll(getContext(), poll.getId(), result);
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }

                // register in adapter that state for selected op_cell is toggled
                registerToggle(position);
            }
        });
        return cell;
    }

    // simple methods for register op_cell state changes
    public void registerToggle(int position) {
        if (unfoldedIndexes.contains(position)) {
            registerFold(position);
        } else
            registerUnfold(position);
    }

    public void registerFold(int position) {
        unfoldedIndexes.remove(position);
    }

    public void registerUnfold(int position) {
        unfoldedIndexes.add(position);
    }


    // View lookup cache
    private static class ViewHolder {
        TextView question;
        Button fold;
        RadioGroup radioGroup;
        Button vote;
    }
}