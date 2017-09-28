package com.dk.graph;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.dk.App;
import com.dk.models.Bucket;
import com.dk.models.Bucket_;
import com.dk.models.Tag;
import com.dk.models.Tag_;
import com.dk.models.User;
import com.dk.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rx2androidnetworking.Rx2AndroidNetworking;

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
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by dk on 27/09/17.
 */

public class ApiCalls {

    private static final String TAG = ">>>>>>>>>>>>.";
    private static String url = "http://192.168.1.2:8000/api/";

    public static void createUser(final Context context) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        JSONObject jsonObject = new JSONObject();
        try {
            assert firebaseUser != null;
            jsonObject.put("contact", firebaseUser.getPhoneNumber().replace("+", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Rx2AndroidNetworking.post(url + "user/create")
                .addJSONObjectBody(jsonObject) // posting json
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete Detail : createAnUser completed");
                        try {
                            syncObjectBox(context);
                        } catch (JSONException | InterruptedException | PSException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof ANError) {
                            ANError anError = (ANError) e;
                            if (anError.getErrorCode() != 0) {
                                // received ANError from server
                                // error.getErrorCode() - the ANError code from server
                                // error.getErrorBody() - the ANError body from server
                                // error.getErrorDetail() - just a ANError detail
                                Log.d(TAG, "onError errorCode : " + anError.getErrorCode());
                                Log.d(TAG, "onError errorBody : " + anError.getErrorBody());
                                Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                            } else {
                                // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                                Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                            }
                        } else {
                            Log.d(TAG, "onError errorMessage : " + e.getMessage());
                        }
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(JSONObject response) {
//                         do anything with response

                        SharedPreferences.Editor editor = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE).edit();
                        try {
                            Log.d(TAG, "uid is here : " + response.getString("User"));
                            editor.putString("uid", response.getString("User"));
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    public static void syncObjectBox(final Context context) throws JSONException, InterruptedException, PSException {
        final PStream uqi;

        uqi = new UQI(context)
                .getData(Contact.getAll(), Purpose.SOCIAL("Gets friends"))
                .sortBy("name").unGroup("phones", "phone").reuse(3);

        final List<ArrayList> contacts = uqi.asList("phone");
        Set<ArrayList> hs = new HashSet<>();
        hs.addAll(contacts);
        contacts.clear();
        contacts.addAll(hs);
        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);

        if (uid == null) {

            new Utils().redirectToLogin((Activity) context);
        }
        Log.d(TAG, "uid sync is here : " + uid);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uid", uid);
        jsonObject.put("contact_list", new JSONArray(contacts));

        Rx2AndroidNetworking.post(url + "user/sync_contacts")
                .addJSONObjectBody(jsonObject) // posting json
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete Detail : Sync completed");
                        new Utils().redirectToMain((Activity) context);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof ANError) {
                            ANError anError = (ANError) e;
                            if (anError.getErrorCode() != 0) {
                                // received ANError from server
                                // error.getErrorCode() - the ANError code from server
                                // error.getErrorBody() - the ANError body from server
                                // error.getErrorDetail() - just a ANError detail
                                Log.d(TAG, "onError errorCode : " + anError.getErrorCode());
                                Log.d(TAG, "onError errorBody : " + anError.getErrorBody());
                                Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                            } else {
                                // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                                Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                            }
                        } else {
                            Log.d(TAG, "onError errorMessage : " + e.getMessage());
                        }
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(JSONObject response) {
                        // do anything with response
                        if (response.has("Users")) {
                            Log.d(TAG, "got resp  is here ");

                            try {
                                JSONArray Users = response.getJSONArray("Users");

                                Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
                                Box<Bucket> bucketBox = App.getInstance().getBoxStore().boxFor(Bucket.class);
                                Box<Tag> tagBox = App.getInstance().getBoxStore().boxFor(Tag.class);
                                User user = new User();
                                for (int i = 0; i < Users.length(); i++) {
                                    JSONObject resp = (JSONObject) Users.get(i);
                                    if (userBox.find("contact", resp.getString("contact")).isEmpty()) {
                                        Log.d(TAG, uqi.filter("phone", resp.getString("contact"))
                                                .getFirst().getAsString("name"));
                                        user.setName(uqi.filter("phone", resp.getString("contact"))
                                                .getFirst().getAsString("name"));
                                        user.setContact(resp.getString("contact"));
                                    } else {
                                        user = userBox.find("contact", resp.getString("contact")).get(0);
                                        Log.d(TAG, user.name);

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

                                }

                            } catch (
                                    JSONException | PSException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });

    }
}
