package com.dk.tagging;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.dk.main.R;
import com.dk.models.User;

import java.util.ArrayList;

/**
 * Created by dk on 21/08/17.
 */

public class UsersAdapter extends ArrayAdapter<User> {
    public UsersAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_contact, parent, false);
        }
        // Lookup view for data population
        TextView tvName = convertView.findViewById(R.id.name);
        // Populate the data into the template view using the data object
        tvName.setText(user.name);
        if (!user.getKnows_me()) {
            Button btButton = convertView.findViewById(R.id.claimed);
            btButton.setTag(position);
            btButton.setText("Share you contact");
            btButton.setVisibility(View.VISIBLE);
            btButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    // Access the row position here to get the correct data item
                    User user = getItem(position);
                    // Do what you want here...
                    System.out.println("Inviting");

                }
            });
        }


        // Return the completed view to render on screen
        return convertView;
    }
}