package com.dk.tagging;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.dk.App;
import com.dk.main.R;
import com.dk.models.Bucket;
import com.dk.models.Bucket_;
import com.dk.models.Me;
import com.dk.models.Tag;
import com.dk.models.Tag_;
import com.dk.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.github.privacystreams.communication.Contact;
import io.github.privacystreams.core.PStream;
import io.github.privacystreams.core.UQI;
import io.github.privacystreams.core.exceptions.PSException;
import io.github.privacystreams.core.purposes.Purpose;
import io.objectbox.Box;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactListFragment extends Fragment {
    JSONArray Users;
    UsersAdapter adapter;
    ArrayList<User> StoreContacts = new ArrayList<>();
    PStream uqi;
    JSONObject jsonObject = new JSONObject();


    public ContactListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_contacts, container,
                false);
        AndroidNetworking.initialize(rootView.getContext());

        StoreContacts = new ArrayList<>();
        try {
            GetContactsIntoArrayList();
        } catch (JSONException | PSException | InterruptedException e) {
            e.printStackTrace();
        }


        ArrayList<User> arrayOfUsers = new ArrayList<>();
        adapter = new UsersAdapter(this.getContext(), arrayOfUsers);

        final ListView listView = rootView.findViewById(R.id.listview);
        listView.setAdapter(adapter);



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), TagActivity.class);
                User user_item = (User) listView.getItemAtPosition(position);
                intent.putExtra("UserId", user_item.getId());

                startActivity(intent);
            }
        });

        return rootView;
    }

    public void GetContactsIntoArrayList() throws JSONException, InterruptedException, PSException {

        uqi = new UQI(this.getContext())
                .getData(Contact.getAll(), Purpose.SOCIAL("Gets friends"))
                .sortBy("name").unGroup("phones", "phone").reuse(3);

        List<ArrayList> contacts = uqi.asList("phone");

        Set<ArrayList> hs = new HashSet<>();
        hs.addAll(contacts);
        contacts.clear();
        contacts.addAll(hs);
        jsonObject.put("uid", Me.getOurInstance().getUid());
        jsonObject.put("contact_list", new JSONArray(contacts));

        AndroidNetworking.post("http://192.168.1.3:8000/api/user/sync_contacts")
                .addJSONObjectBody(jsonObject) // posting json
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        if (!response.has("Users")) {
                            return;
                        }
                        try {
                            Users = response.getJSONArray("Users");

                            Box<User> userBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(User.class);
                            Box<Bucket> bucketBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Bucket.class);
                            Box<Tag> tagBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Tag.class);
                            User user = new User();
                            for (int i = 0; i < Users.length(); i++) {
                                JSONObject resp = (JSONObject) Users.get(i);
                                if (userBox.find("contact", resp.getString("contact")).isEmpty()) {
                                    user.setName(uqi.filter("phone", resp.getString("contact"))
                                            .getFirst().getAsString("name"));
                                    user.setContact(resp.getString("contact"));
                                } else {
                                    user = userBox.find("contact", resp.getString("contact")).get(0);

                                }
                                user.setKnows_me(resp.getBoolean("knows_me"));

                                JSONObject tags = resp.getJSONObject("tags");

                                Iterator<?> keys = tags.keys();

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    Bucket bucket = new Bucket();
                                    if (user.buckets.isEmpty()) {
                                        bucket.setName(key);
                                        user.buckets.add(bucket);
                                    } else {
                                        for (int k = 0; k < user.buckets.size(); k++) {
                                            if (user.buckets.get(k).getName().equalsIgnoreCase(key)) {
                                                bucket = user.buckets.get(k);
                                                break;
                                            }
                                            if (k == user.buckets.size() - 1) {
                                                bucket.setName(key);
                                                user.buckets.add(bucket);
                                                break;
                                            }
                                        }
                                    }
                                    if (!tags.isNull(key)) {
                                        List<Tag> tagsList;
                                        for (int j = 0; j < tags.getJSONArray(key).length(); j++) {
                                            tagsList = tagBox.query().equal(Bucket_.id, bucket.getId())
                                                    .and()
                                                    .equal(Tag_.name, tags.getJSONArray(key).getString(j))
                                                    .build()
                                                    .find();
                                            if (tagsList.isEmpty()) {
                                                Tag tag = new Tag();
                                                tag.setName(tags.getJSONArray(key).getString(j));
                                                bucket.tags.add(tag);
                                                bucketBox.put(bucket);
                                            }
                                        }
                                    }

                                }
                                userBox.put(user);
                                StoreContacts.add(user);

                            }
                            adapter.clear();
                            adapter.addAll(StoreContacts);
                            adapter.notifyDataSetChanged();

                        } catch (
                                JSONException | PSException e) {
                            e.printStackTrace();
                        }


                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d(">>>>>>>>>>", "onError errorCode : " + error.getErrorCode());
                        Log.d(">>>>>>>>>>", "onError errorBody : " + error.getErrorBody());
                        Log.d(">>>>>>>>>>", "onError errorDetail : " + error.getErrorDetail());


                    }
                });


    }


}
