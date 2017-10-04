package com.dk.tagging;


import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dk.SplashActivity;
import com.dk.main.R;
import com.dk.models.User;
import com.turingtechnologies.materialscrollbar.ICustomAdapter;
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> implements INameableAdapter, ICustomAdapter {

    private Activity act;
    private ArrayList<User> userList = new ArrayList<>();

    public UsersAdapter(Activity a) {
        act = a;
        setHasStableIds(true);
    }

    public void setUserList(ArrayList<User> userList) {

        this.userList = userList;

    }

    @Override
    public Character getCharacterForElement(int element) {
        Character c = userList.get(element).name.charAt(0);
        if (Character.isDigit(c)) {
            c = '#';
        }
        return c;
    }


    @Override
    public String getCustomStringForElement(int element) {
        return userList.get(element).name;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Glide.with(act).load("https://ausdroid.net/wp-content/uploads/2017/05/contacts.png").into(holder.avatar);
        holder.label.setText(userList.get(position).name);

        if (!userList.get(position).getKnows_me()) {
            Glide.with(act).load("https://cdn0.iconfinder.com/data/icons/round-ui-icons/512/add_blue.png").into(holder.add);
            holder.add.setVisibility(View.VISIBLE);
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Access the row position here to get the correct data item
                    User user = userList.get(position);
                    // Do what you want here...
                    System.out.println("Inviting" + user.name);

                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(act, ProfileActivity.class);
                    User user_item = userList.get(position);
                    intent.putExtra("UserId", user_item.getId());
                    act.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        try {
            return userList.size();
        } catch (NullPointerException e) {
            Intent i = new Intent(act, SplashActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            act.startActivity(i);
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView label;
        ImageView add;
        ImageView avatar;

        ViewHolder(View v) {
            super(v);
            label = v.findViewById(R.id.textView);
            add = v.findViewById(R.id.add);
            avatar = v.findViewById(R.id.imageView);

        }
    }

}