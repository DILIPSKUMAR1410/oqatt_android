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
import com.dk.queue.Intialization;
import com.dk.queue.RemovePoll;
import com.dk.queue.TokenBalance;
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
import java.util.List;

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

    private static final String TAG = "Api calls ----->";
    private static String url = "http://api.oqatt.com/api/";

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
                            editor.putString("uid", response.getString("User"));
                            editor.apply();
                            if (response.has("token_bal")){
                                editor.putString("token_bal", response.getString("token_bal"));
                                EventBus.getDefault().post(new TokenBalance(response.getString("token_bal")));
                            }
                            else {
                                editor.putString("token_bal", null);
                                EventBus.getDefault().post(new TokenBalance("0"));
                            }
                            editor.apply();
                            ArrayList<String> contacts = new ArrayList<String>();
                            if (response.getBoolean("is_new_user")){
                                ApiCalls.syncContacts(context, 0, contacts);
                            }
                            else {
                                ApiCalls.syncContacts(context, 4, contacts);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    public static void syncContacts(final Context context, final int trigger, List<String> contacts) throws JSONException, InterruptedException {

        Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
        if (contacts.isEmpty() && trigger == 0) {
            for (User user : userBox.getAll()) {
                contacts.add(user.getContact());
            }
        }
        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);

        if (uid == null) {
            Utils.redirectToLogin((Activity) context);
            return;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uid", uid);
        jsonObject.put("trigger", trigger);
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
                        if (trigger == 0 || trigger == 4){
                            EventBus.getDefault().post(new Intialization("Intialization Completed!"));
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

    public static void publishPoll(final Context context, long poll_id, String hex) throws JSONException, InterruptedException {

        Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);
        Poll poll = pollBoxBox.get(poll_id);
        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("question", poll.getQuestion());
        jsonObject.put("poll_hash", hex);
        jsonObject.put("options", new JSONArray(poll.getOptionsList()));
        jsonObject.put("sub_contact", poll.subject.getTarget().getContact());

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
                        try {
                            SharedPreferences.Editor editor = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE).edit();
                            if (response.has("token_bal")){
                                editor.putString("token_bal", response.getString("token_bal"));
                                EventBus.getDefault().post(new TokenBalance(response.getString("token_bal")));
                            }
                            else {
                                editor.putString("token_bal", null);
                                EventBus.getDefault().post(new TokenBalance("0"));
                            }
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });

    }

    public static void publishOpenPoll(final Context context, long poll_id, String hex,List<String> selected_friends) throws JSONException, InterruptedException {
        Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);
        Poll poll = pollBoxBox.get(poll_id);
        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("question", poll.getQuestion());
        jsonObject.put("poll_hash", hex);
        jsonObject.put("options", new JSONArray(poll.getOptionsList()));
        jsonObject.put("selected_friends", new JSONArray(selected_friends));
        Rx2AndroidNetworking.post(url + "user/{uid}/poll/publish/open")
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
                        try {
                            SharedPreferences.Editor editor = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE).edit();
                            if (response.has("token_bal")){
                                editor.putString("token_bal", response.getString("token_bal"));
                                EventBus.getDefault().post(new TokenBalance(response.getString("token_bal")));
                            }
                            else {
                                editor.putString("token_bal", null);
                                EventBus.getDefault().post(new TokenBalance("0"));
                            }
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });

    }

    public static void votePoll(final Context context, long poll_id, int option) throws JSONException, InterruptedException {

        final Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);
        final Poll poll = pollBoxBox.get(poll_id);
        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("poll_hash", poll.getPollHash());
        jsonObject.put("chosen_option", option);
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
                        EventBus.getDefault().post(new RemovePoll(poll));
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
                        try {
                            SharedPreferences.Editor editor = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE).edit();
                            if (response.has("token_bal")){
                                editor.putString("token_bal", response.getString("token_bal"));
                                EventBus.getDefault().post(new TokenBalance(response.getString("token_bal")));
                            }
                            else {
                                editor.putString("token_bal", null);
                                EventBus.getDefault().post(new TokenBalance("0"));
                            }
                            editor.apply();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                    }
                });

    }


    public static void getTokenBalance(final Context context) throws JSONException, InterruptedException {

        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);

        Rx2AndroidNetworking.get(url + "user/{uid}/token/balance")
                .addPathParameter("uid", uid)
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onComplete() {
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
                        try {
                            SharedPreferences.Editor editor = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE).edit();
                            if (response.has("token_bal")){
                                editor.putString("token_bal", response.getString("token_bal"));
                                EventBus.getDefault().post(new TokenBalance(response.getString("token_bal")));
                            }
                            else {
                                editor.putString("token_bal", null);
                                EventBus.getDefault().post(new TokenBalance("0"));
                            }
                            editor.apply();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });

    }


    public static void updateFCMID(final Context context,String fcm_id) throws JSONException, InterruptedException {

        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fcm_id", fcm_id);
        Rx2AndroidNetworking.put(url + "user/{uid}/update/profile/fcm_id")
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
                        try {
                            Log.d(TAG, response.getString("msg"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }
}
