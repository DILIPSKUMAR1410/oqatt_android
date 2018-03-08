package com.dk.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.dk.App;
import com.dk.SelectFriendsActivity;
import com.dk.graph.ApiCalls;
import com.dk.main.R;
import com.dk.main.RecyclerItemClickListener;
import com.dk.main.UsersMentionAdapter;
import com.dk.models.Mention;
import com.dk.models.Poll;
import com.dk.models.User;
import com.dk.models.User_;
import com.percolate.caffeine.ViewUtils;
import com.percolate.mentions.Mentions;
import com.percolate.mentions.QueryListener;
import com.percolate.mentions.SuggestionsListener;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

import static android.content.Context.MODE_PRIVATE;


public class CreatePollFragment extends Fragment implements QueryListener, SuggestionsListener {
    public EditText commentField;
    public Mentions mentions;
    public UsersMentionAdapter usersMentionAdapter;
    public LinearLayout parent_linear_layout;
    View rootView;
    Context context;
    long poll_id;
    View main_activity_view;
    LottieAnimationView animationView;
    FloatingActionButton publishButton;
    Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_create_poll, container, false);
        context = getActivity();


        main_activity_view = getActivity().findViewById(R.id.activity_main);
        animationView = (LottieAnimationView) main_activity_view.findViewById(R.id.send_animation);
        // Get a reference to your EditText
        init();
        setupMentionsList();
        parent_linear_layout = rootView.findViewById(R.id.parent_linear_layout);
        publishButton = rootView.findViewById(R.id.publish_button);

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishButton.setEnabled(false);
                SharedPreferences prefs = getActivity().getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
                String token_bal = prefs.getString("token_bal", null);
                if (token_bal == null || Integer.parseInt(token_bal) < 3) {
                    Toast.makeText(getActivity(), "Not enough gems!", Toast.LENGTH_LONG).show();
                    publishButton.setEnabled(true);
                    return;
                }

                Poll poll = new Poll();

                poll.setQuestion(String.valueOf(commentField.getText()));
                for (int i = 0; i < 4; i++) {
                    String EdittextID = "op" + i;
                    int EdittextresID = getResources().getIdentifier(EdittextID, "id", getContext().getPackageName());
                    EditText option = parent_linear_layout.findViewById(EdittextresID);
                    String option_string = option.getText().toString();
                    option.setText("");
                    poll.insertOption(option_string);
                }
                if (poll.getOptionsList().size() < 2) {
                    Toast.makeText(getActivity(), "Add Option!", Toast.LENGTH_LONG).show();
                    publishButton.setEnabled(true);
                    return;
                }

                if (mentions.getInsertedMentions().size() > 1) {
                    Toast.makeText(getActivity(), "Mention only one !", Toast.LENGTH_LONG).show();
                    publishButton.setEnabled(true);
                    return;
                }

                poll.setType(0);
                poll.setResultString("0,0,0,0");
                Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);
                MessageDigest digest = null;
                String uid = prefs.getString("uid", null);
                String poll_pkg;
                if (mentions.getInsertedMentions().size() == 1)
                {   Mention mention = (Mention) mentions.getInsertedMentions().get(0);
                    poll.subject.setTarget(mention.getMentionUser());
                    poll_pkg = poll.getQuestion() + poll.subject.getTarget().getContact() + poll.getOptionString() + uid;
                }
                else {
                    poll_pkg = poll.getQuestion() + poll.getOptionString() + uid;
                }


                String hex = "";
                try {
                    digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest(poll_pkg.getBytes(Charset.forName("UTF-8")));
                    hex = String.format("%064x", new BigInteger(1, hash));
                    poll.setPollHash(hex);

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }


                poll_id = pollBoxBox.put(poll);

                if (mentions.getInsertedMentions().size() < 1){
                    Intent intent = new Intent(getActivity(), SelectFriendsActivity.class);
                    intent.putExtra("hex",hex);
                    intent.putExtra("poll_id",poll_id);
                    startActivity(intent);
                }
                    else {
                    try {
                        ApiCalls.publishPoll(context, poll_id, hex);
                    } catch (JSONException | InterruptedException e) {
                        e.printStackTrace();
                    }



                    View view = parent_linear_layout.getChildAt(0);
                    if (view instanceof EditText) {
                        ((EditText) view).setText("");
                    }

                    main_activity_view.findViewById(R.id.Blurred).setVisibility(View.GONE);
                    main_activity_view.findViewById(R.id.spaceTabLayout).setVisibility(View.GONE);
                    animationView.setVisibility(View.VISIBLE);
                    animationView.setAnimation("send.json");
                    animationView.playAnimation();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 2000ms
                            animationView.cancelAnimation();
                            main_activity_view.findViewById(R.id.Blurred).setVisibility(View.VISIBLE);
                            main_activity_view.findViewById(R.id.spaceTabLayout).setVisibility(View.VISIBLE);
                            animationView.setVisibility(View.GONE);
                            publishButton.setEnabled(true);
                            Toast.makeText(getActivity(), "Add question published!", Toast.LENGTH_LONG).show();
                        }
                    }, 2000);
                }
            }
        });

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

//    public void onAddField() {
//        LayoutInflater inflater = getActivity().getLayoutInflater();
//        final View rowView = inflater.inflate(R.layout.field, null);
//        // Add the new row before the add field button.
//        Log.d(">>>>>>>>>>", String.valueOf(parent_linear_layout.getChildCount()));
//        parent_linear_layout.addView(rowView, parent_linear_layout.getChildCount() - 1);
//    }

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
                    mention.setMentionName("<"+user.name+">");
                    mention.setMentionUser(user);
                    mentions.insertMention(mention);
                }
                parent_linear_layout.setVisibility(View.VISIBLE);

            }
        }));
    }

    @Override
    public void onQueryReceived(final String query) {
        if (mentions.getInsertedMentions().size() > 1) {
            Toast.makeText(getActivity(), "Cant mention more than one right now!", Toast.LENGTH_LONG).show();
            return;

        }
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
            if (!userList.isEmpty()) {
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
//            parent_linear_layout.setVisibility(View.INVISIBLE);

        } else {
            rootView.findViewById(R.id.mentions_list).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.mentions_empty_view).setVisibility(View.VISIBLE);
//            parent_linear_layout.setVisibility(View.VISIBLE);
        }
    }

}
