package com.dk.fragments;

/**
 * Created by dk on 09/12/17.
 */

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dk.App;
import com.dk.graph.ApiCalls;
import com.dk.main.R;
import com.dk.models.Message;
import com.dk.models.Poll;
import com.dk.models.Thread;
import com.dk.queue.RemovePoll;
import com.dk.queue.UpdateThread;
import com.dk.utils.Utils;
import com.ramotion.foldingcell.FoldingCell;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import io.objectbox.Box;

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
            viewHolder.action_button = cell.findViewById(R.id.action_button);
            viewHolder.thread_invite = cell.findViewById(R.id.thread_invite);

            if (poll.isThread()) {
                viewHolder.thread_invite.setVisibility(View.VISIBLE);
                viewHolder.action_button.setText("Join");
            } else {
                viewHolder.radioGroup.setVisibility(View.VISIBLE);
                viewHolder.action_button.setText("Anwser");
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
        viewHolder.action_button.setOnClickListener(v -> {
            Log.d(">>>>>>>", "Join button");
            if (poll.isThread()) {
                finalCell.toggle(false);
                Box<Poll> pollBox = App.getInstance().getBoxStore().boxFor(Poll.class);
                Box<Thread> threadBox = App.getInstance().getBoxStore().boxFor(Thread.class);

                Thread thread = new Thread(poll.getQuestion(),
                        new Message("Hi!", poll.getSender()),
                        String.valueOf(UUID.randomUUID()));
                thread.setThreadHash(poll.getPollHash());

                if (!poll.subject.isNull()) {
                    thread.subject.setTarget(poll.subject.getTarget());
                    thread.setNameMentioned(true);
                }

                threadBox.put(thread);
                pollBox.remove(poll);
                EventBus.getDefault().post(new RemovePoll(poll));
                EventBus.getDefault().post(new UpdateThread(0, thread));

                registerToggle(position);
            } else {
                // toggle clicked op_cell state
                if (viewHolder.radioGroup.getCheckedRadioButtonId() == -1) {
                    // no radio buttons are checked
                    return;
                }
                finalCell.toggle(false);
                String s = getContext().getResources().getResourceEntryName(viewHolder.radioGroup.getCheckedRadioButtonId());
                int result = Integer.parseInt(s.substring(2));
                try {
                    ApiCalls.votePoll(getContext(), poll.getId(), result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // register in adapter that state for selected op_cell is toggled
                registerToggle(position);

                Utils.redirectToAnim((Activity) getContext(), 1);
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
        TextView thread_invite;
        Button fold;
        RadioGroup radioGroup;
        Button action_button;
    }
}