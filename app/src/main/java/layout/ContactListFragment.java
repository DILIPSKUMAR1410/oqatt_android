package layout;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dk.androidclient.ChooseContactActivity;
import com.dk.androidclient.R;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactListFragment extends Fragment {


    ListView listView;
    ArrayList<String> StoreContacts;
    ArrayAdapter<String> arrayAdapter;
    Cursor cursor;
    String name, phonenumber;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public ContactListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_contacts, container,
                false);

        listView = rootView.findViewById(R.id.listview);

        StoreContacts = new ArrayList<>();
        GetContactsIntoArrayList();

        arrayAdapter = new ArrayAdapter<>(
                rootView.getContext(),
                R.layout.text, StoreContacts
        );

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
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
        while (cursor.moveToNext()) {

            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (!phonenumber.contentEquals(lastphonenumber)) {
                StoreContacts.add(name + " " + phonenumber.replace("+91", "").replace(" ", "").replace("+1", "").replace("(", "").replace(")", "").replace("-", ""));
            }

            lastphonenumber = phonenumber;

        }

        cursor.close();
        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        set.addAll(StoreContacts);
        StoreContacts = new ArrayList<>(set);
    }







}
