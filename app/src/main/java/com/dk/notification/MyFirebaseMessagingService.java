package com.dk.notification;

/**
 * Created by dk on 20/11/17.
 */

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dk.App;
import com.dk.graph.ApiCalls;
import com.dk.main.MainActivity;
import com.dk.main.R;
import com.dk.models.Poll;
import com.dk.models.Poll_;
import com.dk.models.User;
import com.dk.models.User_;
import com.dk.queue.UpdatePoll;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

import static android.app.NotificationManager.IMPORTANCE_HIGH;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            int type = Integer.parseInt(remoteMessage.getData().get("type"));
            Bundle bundle = new Bundle();
            ;
            if (type == 0) {
                Poll incomingPoll = new Poll();
                String sub_contact = remoteMessage.getData().get("sub_contact");
                Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
                incomingPoll.subject.setTarget(userBox.query().equal(User_.contact, sub_contact).build().findFirst());
                String question = remoteMessage.getData().get("question").replaceAll("<.*>", incomingPoll.subject.getTarget().name);
                incomingPoll.setQuestion(question);
                incomingPoll.setType(1);
                incomingPoll.setPollHash(remoteMessage.getData().get("poll_hash"));
                String[] options = remoteMessage.getData().get("options").replace("[", "").replace("]", "").split(",");
                for (String option : options) {
                    incomingPoll.insertOption(option);
                }
                Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);
                pollBoxBox.put(incomingPoll);
                EventBus.getDefault().post(new UpdatePoll("You got new poll "));
                sendNotification("New question asked to you");
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Q-rcv");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Question received");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification");

            }
            if (type == 1) {
                Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);

                final List<Poll> outgoingPolls = pollBoxBox.query().equal(Poll_.pollHash, remoteMessage.getData().get("poll_hash")).build().find();
                Poll outgoingPoll = outgoingPolls.get(0);
                outgoingPoll.setResultString(remoteMessage.getData().get("option_count").replace("[", "").replace("]", ""));
                outgoingPoll.setArchive(false);
                pollBoxBox.put(outgoingPoll);
                EventBus.getDefault().post(new UpdatePoll("Got an upvote"));
                sendNotification("Someone anwsered your question");
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "A-rcv");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Answer received");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification");

            }
            if (type == 2) {
                String contact = remoteMessage.getData().get("user_contact");
                Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
                if (!userBox.find(User_.contact, contact).isEmpty()) {
                    User user = userBox.find(User_.contact, contact).get(0);
                    if (!user.getKnows_me()) {
                        ArrayList<String> contacts = new ArrayList<String>();
                        contacts.add(contact);
                        try {
                            ApiCalls.syncContacts(this, 1, contacts);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                sendNotification(contact + " added you in his network");
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "F-added");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Friend request");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification");

            }
            if (type == 3) {
                String contact = remoteMessage.getData().get("user_contact");
                Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
                if (!userBox.find(User_.contact, contact).isEmpty()) {
                    User user = userBox.find(User_.contact, contact).get(0);
                    user.setKnows_me(true);
                    userBox.put(user);
                }
                sendNotification(contact + " accepted your request");
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "FR-accept");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Friend request accept");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification");
            }

            if (type == 4) {
                Poll incomingOpenPoll = new Poll();
                String question = remoteMessage.getData().get("question");
                incomingOpenPoll.setQuestion(question);
                incomingOpenPoll.setType(1);
                incomingOpenPoll.setPollHash(remoteMessage.getData().get("poll_hash"));
                String[] options = remoteMessage.getData().get("options").replace("[", "").replace("]", "").split(",");
                for (String option : options) {
                    incomingOpenPoll.insertOption(option);
                }
                Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);
                pollBoxBox.put(incomingOpenPoll);
                EventBus.getDefault().post(new UpdatePoll("You got new open poll "));
                sendNotification("New question asked to you");
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "OQ-rcv");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "open question received");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification");
            }

//            if (/* Check if data needs to be processed by long running job */ false) {
//                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob();
//            } else {
//                // Handle message within 10 seconds
//                handleNow();
//            }

            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getTitle());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Log.d(TAG, messageBody);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "oqatt",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("oqatt_updates");
            channel.setImportance(IMPORTANCE_HIGH);
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(channel);
        }
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
                .setSmallIcon(R.mipmap.oqatt_logo) // notification icon
                .setContentTitle(messageBody) // title for notification
                .setSound(defaultSoundUri)  // sound notification
                .setAutoCancel(true); // clear notification after click

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        assert mNotificationManager != null;
        mNotificationManager.notify(0, mBuilder.build());
    }
}