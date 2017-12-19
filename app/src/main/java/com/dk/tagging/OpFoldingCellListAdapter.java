package com.dk.tagging;

/**
 * Created by dk on 09/12/17.
 */

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dk.main.R;
import com.dk.models.Poll;
import com.ramotion.foldingcell.FoldingCell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Simple example of ListAdapter for using with Folding Cell
 * Adapter holds indexes of unfolded elements for correct work with default reusable views behavior
 */
public class OpFoldingCellListAdapter extends ArrayAdapter<Poll> {

    private HashSet<Integer> unfoldedIndexes = new HashSet<>();

    public OpFoldingCellListAdapter(Context context, List<Poll> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // get item for selected view
        Poll poll = getItem(position);
        // if op_cell is exists - reuse it, if not - create the new one from resource
        FoldingCell cell = (FoldingCell) convertView;

        ViewHolder viewHolder;
        if (cell == null) {
            viewHolder = new ViewHolder();
            LayoutInflater vi = LayoutInflater.from(getContext());
            cell = (FoldingCell) vi.inflate(R.layout.op_cell, parent, false);
            // binding view parts to view holder
            viewHolder.question = cell.findViewById(R.id.question);
            viewHolder.fold = cell.findViewById(R.id.button2);
//            viewHolder.contentLayout = op_cell.findViewById(R.id.contentLayout);
            cell.setTag(viewHolder);

            ArrayList<String> options = poll.getOptionsList();
            for (int i = 0; i < options.size(); i++) {
                String TextID = "op" + i;
                String CardID = "cop" + i;
                String RTextID = "countop" + i;
                int textresID = getContext().getResources().getIdentifier(TextID, "id", getContext().getPackageName());
                int RtextresID = getContext().getResources().getIdentifier(RTextID, "id", getContext().getPackageName());
                int cardresID = getContext().getResources().getIdentifier(CardID, "id", getContext().getPackageName());
                TextView rtext = cell.findViewById(RtextresID);
                TextView text = cell.findViewById(textresID);
                CardView card = cell.findViewById(cardresID);
                card.setVisibility(View.VISIBLE);
                text.setText(options.get(i));
                rtext.setText(poll.getResultString().split(",")[i]);
                Log.d(">>>>>>>>results.",poll.getId()+">>"+poll.getResultString().split(",")[i]);

            }

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

                Log.d(">>>>>>>>CELLNO.", String.valueOf(position));
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
        LinearLayout contentLayout;
    }
}