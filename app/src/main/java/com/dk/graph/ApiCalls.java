package com.dk.graph;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.dk.App;
import com.dk.models.Message;
import com.dk.models.Poll;
import com.dk.models.Thread;
import com.dk.models.User;
import com.dk.models.User_;
import com.dk.queue.AddParticipants;
import com.dk.queue.AppUpdateVersion;
import com.dk.queue.Intialization;
import com.dk.queue.MessageList;
import com.dk.queue.RemovePoll;
import com.dk.queue.TokenBalance;
import com.dk.queue.UpdateFriendList;
import com.dk.queue.UpdateThread;
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

/**
 * Created by dk on 27/09/17.
 */

public class ApiCalls {

    private static final String TAG = "Api calls ----->";
    private static String url = "http://192.168.0.102:8000/api/";

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
                            if (response.has("token_bal")) {
                                editor.putString("token_bal", response.getString("token_bal"));
                                EventBus.getDefault().post(new TokenBalance(response.getString("token_bal")));
                            } else {
                                editor.putString("token_bal", null);
                                EventBus.getDefault().post(new TokenBalance("0"));
                            }
                            editor.apply();
                            ArrayList<String> contacts = new ArrayList<String>();
                            if (response.getBoolean("is_new_user")) {
                                ApiCalls.syncContacts(context, 0, contacts);
                            } else {
                                ApiCalls.syncContacts(context, 4, contacts);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public static void syncContacts(final Context context, final int trigger, List<String> contacts) throws JSONException {

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
                        if (trigger == 0 || trigger == 4) {
                            EventBus.getDefault().post(new Intialization("Intialization Completed!"));
                        } else if (trigger == 2 || trigger == 3) {
                            EventBus.getDefault().post(new UpdateFriendList("Refresh Completed!"));
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

    public static void publishMentionedPoll(final Context context, Poll poll, String hex, List<String> selected_friends) throws JSONException {

        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("question", poll.getQuestion());
        jsonObject.put("poll_hash", hex);
        jsonObject.put("options", new JSONArray(poll.getOptionsList()));
        jsonObject.put("sub_contact", poll.subject.getTarget().getContact());
        jsonObject.put("selected_friends", new JSONArray(selected_friends));
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
                            if (response.has("token_bal")) {
                                editor.putString("token_bal", response.getString("token_bal"));
                                EventBus.getDefault().post(new TokenBalance(response.getString("token_bal")));
                            } else {
                                editor.putString("token_bal", null);
                                EventBus.getDefault().post(new TokenBalance("0"));
                            }
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Thread thread = new Thread(poll.getQuestion(), new Message("Poll sent to your friends", "0"));
                        thread.setThreadHash(poll.getPollHash());
                        thread.setResultString(poll.getResultString());
                        thread.setOptionString(poll.getOptionString());
                        thread.subject.setTarget(poll.subject.getTarget());
                        thread.setNameMentioned(true);
                        Box<Thread> threadBox = App.getInstance().getBoxStore().boxFor(Thread.class);
                        threadBox.put(thread);
                        EventBus.getDefault().post(new UpdateThread(0, thread));

                    }
                });

    }

    public static void publishOpenPoll(final Context context, Poll poll, String hex, List<String> selected_friends) throws JSONException {
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
                            if (response.has("token_bal")) {
                                editor.putString("token_bal", response.getString("token_bal"));
                                EventBus.getDefault().post(new TokenBalance(response.getString("token_bal")));
                            } else {
                                editor.putString("token_bal", null);
                                EventBus.getDefault().post(new TokenBalance("0"));
                            }
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Thread thread = new Thread(poll.getQuestion(), new Message("Poll sent to your friends", "0"));
                        thread.setThreadHash(poll.getPollHash());
                        thread.setResultString(poll.getResultString());
                        thread.setOptionString(poll.getOptionString());
                        Box<Thread> threadBox = App.getInstance().getBoxStore().boxFor(Thread.class);
                        threadBox.put(thread);
                        EventBus.getDefault().post(new UpdateThread(0, thread));

                    }
                });

    }

    public static void votePoll(final Context context, long poll_id, int option) throws JSONException {

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
                            if (response.has("token_bal")) {
                                editor.putString("token_bal", response.getString("token_bal"));
                                EventBus.getDefault().post(new TokenBalance(response.getString("token_bal")));
                            } else {
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


    public static void getTokenBalance(final Context context) throws JSONException {

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
                            if (response.has("token_bal")) {
                                editor.putString("token_bal", response.getString("token_bal"));
                                EventBus.getDefault().post(new TokenBalance(response.getString("token_bal")));
                            } else {
                                editor.putString("token_bal", null);
                                EventBus.getDefault().post(new TokenBalance("0"));
                            }
                            editor.apply();
                            if (response.has("app_version")) {
                                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                                int version = pInfo.versionCode;
                                Log.d(">>>>>", String.valueOf(version));
                                if (response.getInt("app_version") > version) {
                                    EventBus.getDefault().post(new AppUpdateVersion(response.getInt("app_version")));

                                }
                            }

                        } catch (PackageManager.NameNotFoundException e1) {
                            e1.printStackTrace();
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
                });

    }


    public static void updateFCMID(final Context context, String fcm_id) throws JSONException {

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


    public static void getFriendsConnections(final Context context, String sub_contact) throws JSONException {

        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sub_contact", sub_contact);
        Rx2AndroidNetworking.post(url + "user/{uid}/friend/connections")
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
                            EventBus.getDefault().post(new AddParticipants(response.getJSONArray("mutual"), response.getInt("unknown")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    public static void publishMentionedThread(final Context context, Thread thread, String hex, List<String> selected_friends) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("question", thread.getDialogName());
        jsonObject.put("thread_hash", hex);
        jsonObject.put("passkey", thread.getPasskey());
        jsonObject.put("sub_contact", thread.subject.getTarget().getContact());
        jsonObject.put("selected_friends", new JSONArray(selected_friends));
        Rx2AndroidNetworking.post(url + "user/{uid}/thread/publish")
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
                            if (response.has("token_bal")) {
                                editor.putString("token_bal", response.getString("token_bal"));
                                EventBus.getDefault().post(new TokenBalance(response.getString("token_bal")));
                            } else {
                                editor.putString("token_bal", null);
                                EventBus.getDefault().post(new TokenBalance("0"));
                            }
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Box<Thread> threadBox = App.getInstance().getBoxStore().boxFor(Thread.class);
                        threadBox.put(thread);

                        Log.d(TAG, String.valueOf(thread.getLastMessage().getCreatedAt()));
                        EventBus.getDefault().post(new UpdateThread(0, thread));


                    }
                });

    }

    public static void publishOpenThread(final Context context, Thread thread, String hex, List<String> selected_friends) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("question", thread.getDialogName());
        jsonObject.put("thread_hash", hex);
        jsonObject.put("passkey", thread.getPasskey());
        jsonObject.put("selected_friends", new JSONArray(selected_friends));
        Rx2AndroidNetworking.post(url + "user/{uid}/thread/publish/open")
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
                            if (response.has("token_bal")) {
                                editor.putString("token_bal", response.getString("token_bal"));
                                EventBus.getDefault().post(new TokenBalance(response.getString("token_bal")));
                            } else {
                                editor.putString("token_bal", null);
                                EventBus.getDefault().post(new TokenBalance("0"));
                            }
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Box<Thread> threadBox = App.getInstance().getBoxStore().boxFor(Thread.class);
                        threadBox.put(thread);
                        EventBus.getDefault().post(new UpdateThread(0, thread));
                    }
                });

    }

    public static void sendMessage(final Context context, Thread thread, Message message) throws JSONException {
        SharedPreferences prefs = context.getSharedPreferences("my_oqatt_prefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("thread_hash", thread.getThreadHash());
        jsonObject.put("passkey", thread.getPasskey());
        jsonObject.put("message", message.getText());
        Rx2AndroidNetworking.post(url + "user/{uid}/thread/message/send")
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
                        Box<Thread> threadBox = App.getInstance().getBoxStore().boxFor(Thread.class);
                        thread.setLastMessage(message);
                        threadBox.put(thread);
                        EventBus.getDefault().post(new MessageList(message));
                        EventBus.getDefault().post(new UpdateThread(1, thread));
                    }
                });

    }
}
