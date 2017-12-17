package com.dk.tagging;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.dk.App;
import com.dk.main.R;
import com.dk.main.RecyclerItemClickListener;
import com.dk.main.UsersMentionAdapter;
import com.dk.models.Mention;
import com.dk.models.User;
import com.dk.models.User_;
import com.percolate.caffeine.ViewUtils;
import com.percolate.mentions.Mentions;
import com.percolate.mentions.QueryListener;
import com.percolate.mentions.SuggestionsListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;


public class CreatePollFragment extends Fragment implements QueryListener, SuggestionsListener {
    public EditText commentField;
    public Mentions mentions;
    public UsersMentionAdapter usersMentionAdapter;
    public LinearLayout parent_linear_layout;
    public LinearLayout sub_parent_linear_layout;

    View rootView;
    Context context;
    Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_create_poll, container, false);
        context = getActivity();
        // Get a reference to your EditText
        init();
        setupMentionsList();
        EditText edittTxt = new EditText(getContext());
        parent_linear_layout = rootView.findViewById(R.id.parent_linear_layout);

//        publishButton = rootView.findViewById(R.id.publish_button);
//        publishButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                // do something
//                Poll poll = new Poll();
//                poll.setQuestion(String.valueOf(commentField.getText()));
//                int count = sub_parent_linear_layout.getChildCount();
//                EditText x = null;
//                for(int i=0; i<count; i++) {
//                    x = (EditText) sub_parent_linear_layout.getChildAt(i);
//                    poll.insertOption(String.valueOf(x.getText()));
//                }
//                Mention mention = (Mention) mentions.getInsertedMentions().get(0);
//                poll.subject.setTarget(mention.getMentionUser());
//                Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);
//                long poll_id = pollBoxBox.put(poll);
//                try {
//                    ApiCalls.publishPoll(context,poll_id);
//                } catch (JSONException | InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });

        // Inflate the layout for this fragment
        return rootView;
    }

    /**
     * Initialize views and utility objects.
     */
    private void init() {
        commentField = rootView.findViewById(R.id.comment_field);
        mentions = new Mentions.Builder(getActivity(), commentField)
                .suggestionsListener(this)
                .queryListener(this)
                .build();

    }

    public void onAddField() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View rowView = inflater.inflate(R.layout.field, null);
        // Add the new row before the add field button.
        Log.d(">>>>>>>>>>", String.valueOf(parent_linear_layout.getChildCount()));
        parent_linear_layout.addView(rowView, parent_linear_layout.getChildCount() - 1);
    }

    public void onDelete(View v) {
        parent_linear_layout.removeView((View) v.getParent());
    }


    /**
     * Setups the mentions suggestions list. Creates and sets and adapter for
     * the mentions list and sets the on item click listener.
     */
    private void setupMentionsList() {
        final RecyclerView mentionsList = ViewUtils.findViewById(rootView, R.id.mentions_list);
        mentionsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        usersMentionAdapter = new UsersMentionAdapter(getActivity());
        mentionsList.setAdapter(usersMentionAdapter);

        // set on item click listener
        mentionsList.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(final View view, final int position) {
                final User user = usersMentionAdapter.getItem(position);
                /*
                 * We are creating a mentions object which implements the
                 * <code>Mentionable</code> interface this allows the library to set the offset
                 * and length of the mention.
                 */
                if (user != null) {
                    final Mention mention = new Mention();
                    mention.setMentionName(user.name);
                    mention.setMentionUser(user);
                    mentions.insertMention(mention);
                }
                parent_linear_layout.setVisibility(View.VISIBLE);

            }
        }));
    }

    @Override
    public void onQueryReceived(final String query) {
        final List<User> users = searchUsers(query);
        if (users != null && !users.isEmpty()) {
            usersMentionAdapter.clear();
            usersMentionAdapter.setCurrentQuery(query);
            usersMentionAdapter.addAll(users);
            showMentionsList(true);
        } else {
            showMentionsList(false);
        }
    }

    @Override
    public void displaySuggestions(boolean display) {
        if (display) {
            ViewUtils.showView(getActivity(), R.id.mentions_list_layout);
        } else {
            ViewUtils.hideView(getActivity(), R.id.mentions_list_layout);
        }
    }

    /**
     * Search for user with name matching {@code query}.
     *
     * @return a list of users that matched {@code query}.
     */
    public List<User> searchUsers(String query) {
        final List<User> searchResults = new ArrayList<>();
        if (StringUtils.isNotBlank(query)) {
            query = query.toLowerCase();
            List<User> userList = userBox.query().equal(User_.knows_me, true).build().find();
            if (userList != null && !userList.isEmpty()) {
                for (User user : userList) {
                    final String firstName = user.name.toLowerCase();
                    if (firstName.startsWith(query)) {
                        searchResults.add(user);
                    }
                }
            }

        }
        return searchResults;
    }


    private void showMentionsList(boolean display) {
        rootView.findViewById(R.id.mentions_list_layout).setVisibility(View.VISIBLE);
        if (display) {
            rootView.findViewById(R.id.mentions_list).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.mentions_empty_view).setVisibility(View.INVISIBLE);
            parent_linear_layout.setVisibility(View.INVISIBLE);

        } else {
            rootView.findViewById(R.id.mentions_list).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.mentions_empty_view).setVisibility(View.VISIBLE);
            parent_linear_layout.setVisibility(View.VISIBLE);
        }
    }

}
