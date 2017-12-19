package com.dk.graph;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.dk.App;
import com.dk.models.Poll;
import com.dk.models.User;
import com.dk.models.User_;
import com.dk.queue.RefreshEvent;
import com.dk.queue.RemovePoll;
import com.dk.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rx2androidnetworking.Rx2AndroidNetworking;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.objectbox.Box;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

//import com.dk.models.User_;


/**
 * Created by dk on 27/09/17.
 */

public class ApiCalls {

    private static final String TAG = ">>>>>>>>>>>>.";
    private static String url = "http://192.168.0.106:8000/api/";

    public static void createUser(final Context context) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        JSONObject jsonObject = new JSONObject();
        try {
            assert firebaseUser != null;
            jsonObject.put("contact", firebaseUser.getPhoneNumber());
            jsonObject.put("fcm_id", FirebaseInstanceId.getInstance().getToken());
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
                            ApiCalls.syncContacts(context);
                        } catch (JSONException | InterruptedException e) {
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


    public static void syncContacts(final Context context) throws JSONException, InterruptedException {

        Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
        ArrayList<String> contacts = new ArrayList<>();

        for (User user : userBox.getAll()) {
            contacts.add(user.getContact());
        }

        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);

        if (uid == null) {
            new Utils().redirectToLogin((Activity) context);
        }
        Log.d(TAG, "uid sync is here : " + uid);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uid", uid);
        jsonObject.put("contact_list", new JSONArray(contacts));

        Log.d(TAG, String.valueOf(jsonObject));

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

                        EventBus.getDefault().post(new RefreshEvent("Refreashed!"));
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
                                User user;
                                ArrayList<User> users = new ArrayList<>();
                                Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
                                for (int i = 0; i < Users.length(); i++) {
                                    String resp = (String) Users.get(i);
                                    Log.d(TAG, resp);
                                    user = userBox.find(User_.contact, resp).get(0);
                                    user.setKnows_me(true);
                                    users.add(user);
                                }
                                userBox.put(users);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });

    }


    public static void publishPoll(Context context, long poll_id,String hex) throws JSONException, InterruptedException {

        Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);
        Poll poll = pollBoxBox.get(poll_id);
        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("question", poll.getQuestion());
        jsonObject.put("poll_hash", hex);
        jsonObject.put("sub_contact", poll.subject.getTarget().getContact());
        jsonObject.put("options", new JSONArray(poll.getOptionsList()));

        Rx2AndroidNetworking.post(url + "user/{uid}/poll/publish")
                .addPathParameter("uid", uid)
                .addJSONObjectBody(jsonObject) // posting json
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete Detail : Question published");
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


                    }
                });

    }

    public static void votePoll(Context context, long poll_id,int option) throws JSONException, InterruptedException {

        final Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);
        final Poll poll = pollBoxBox.get(poll_id);
        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("poll_hash", poll.getPollHash());
        jsonObject.put("chosen_option",option);
        Log.d(TAG, String.valueOf(jsonObject));
        Rx2AndroidNetworking.post(url + "user/{uid}/poll/vote")
                .addPathParameter("uid", uid)
                .addJSONObjectBody(jsonObject) // posting json
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete Detail : Voted ");
                        pollBoxBox.remove(poll);
                        EventBus.getDefault().post(new RemovePoll("Clear current Poll!"));
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


                    }
                });

    }

}
