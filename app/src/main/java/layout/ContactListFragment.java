package layout;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.dk.androidclient.ChooseContactActivity;
import com.dk.androidclient.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import adapter.UsersAdapter;
import models.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactListFragment extends Fragment {

    JSONArray Users;
    UsersAdapter adapter;
    ListView listView;
    ArrayList<User> StoreContacts;
    Cursor cursor;
    String name, phonenumber;

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
        GetContactsIntoArrayList();

        ArrayList<User> arrayOfUsers = new ArrayList<User>();
        adapter = new UsersAdapter(this.getContext(), arrayOfUsers);

        ListView listView = rootView.findViewById(R.id.listview);
        listView.setAdapter(adapter);

        adapter.addAll(StoreContacts);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ChooseContactActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public void GetContactsIntoArrayList() {

        cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        String lastphonenumber = "";


        assert cursor != null;


        ArrayList<JSONObject> contact_list = new ArrayList<>();
        while (cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (!phonenumber.contentEquals(lastphonenumber)) {
                phonenumber = phonenumber.replace(" ", "").replace("(", "").replace(")", "").replace("-", "");


                try {
                    JSONObject y = new JSONObject();
                    y.put("contact", phonenumber);
                    y.put("name", name);
                    contact_list.add(y);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            lastphonenumber = phonenumber;

        }

        cursor.close();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", "8cf19d917d0d4c9f9d273314fe37794e");
            jsonObject.put("contact_list", new JSONArray(contact_list));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post("http://192.168.0.103:8000/api/user/sync_contacts")
                .addJSONObjectBody(jsonObject) // posting json
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        try {
                            Users = response.getJSONArray("Users");
                            for (int i = 0; i < Users.length(); i++) {
                                JSONObject temp = (JSONObject) Users.get(i);
                                StoreContacts.add(new User(temp.getString("name"),
                                        temp.getBoolean("claimed"),
                                        temp.getBoolean("Bi-directional")));

                            }
                            adapter.clear();
                            adapter.addAll(StoreContacts);
                            adapter.notifyDataSetChanged();

                        } catch (
                                JSONException e) {
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
