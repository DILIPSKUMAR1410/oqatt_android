package com.dk.notification;

/**
 * Created by dk on 20/11/17.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dk.App;
import com.dk.graph.ApiCalls;
import com.dk.main.MainActivity;
import com.dk.models.Poll;
import com.dk.models.Poll_;
import com.dk.models.User;
import com.dk.models.User_;
import com.dk.queue.UpdatePoll;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

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

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            int type = Integer.parseInt(remoteMessage.getData().get("type"));
            try {
                Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + App.getInstance().getApplicationContext().getPackageName() + "/raw/notification");
                Ringtone r = RingtoneManager.getRingtone(App.getInstance().getApplicationContext(), alarmSound);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (type == 0) {
                Poll incomingPoll = new Poll();
                incomingPoll.setQuestion(remoteMessage.getData().get("question"));
                incomingPoll.setType(1);
                incomingPoll.setPollHash(remoteMessage.getData().get("poll_hash"));
                String[] options = remoteMessage.getData().get("options").replace("[", "").replace("]", "").split(",");
                for (String option : options) {
                    incomingPoll.insertOption(option);
                }
                Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);
                pollBoxBox.put(incomingPoll);
                EventBus.getDefault().post(new UpdatePoll("You got new poll "));

            }
            if (type == 1) {
                Box<Poll> pollBoxBox = App.getInstance().getBoxStore().boxFor(Poll.class);

                final List<Poll> outgoingPolls = pollBoxBox.query().equal(Poll_.pollHash, remoteMessage.getData().get("poll_hash")).build().find();
                Poll outgoingPoll = outgoingPolls.get(0);
                outgoingPoll.setResultString(remoteMessage.getData().get("option_count").replace("[", "").replace("]", ""));
                pollBoxBox.put(outgoingPoll);
                EventBus.getDefault().post(new UpdatePoll("Got an upvote"));

            }
            if (type == 2) {
                String contact = remoteMessage.getData().get("user_contact");
                Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
                if (!userBox.find(User_.contact,contact).isEmpty())
                {
                    User user = userBox.find(User_.contact, contact).get(0);
                    if (!user.getKnows_me()){
                        ArrayList<String> contacts = new ArrayList<String>();
                        contacts.add(contact);
                        try {
                            ApiCalls.syncContacts(this,1,contacts);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            if (type == 3) {
                String contact = remoteMessage.getData().get("user_contact");
                Box<User> userBox = App.getInstance().getBoxStore().boxFor(User.class);
                if (!userBox.find(User_.contact,contact).isEmpty()) {
                    User user = userBox.find(User_.contact, contact).get(0);
                    user.setKnows_me(true);
                    userBox.put(user);
                }
            }

//            if (/* Check if data needs to be processed by long running job */ false) {
//                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob();
//            } else {
//                // Handle message within 10 seconds
//                handleNow();
//            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "New Poll";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle("FCM Message")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}