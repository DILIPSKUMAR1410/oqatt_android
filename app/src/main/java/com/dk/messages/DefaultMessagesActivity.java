package com.dk.messages;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.dk.graph.ApiCalls;
import com.dk.main.R;
import com.dk.models.Message;
import com.dk.queue.MessageList;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

public class DefaultMessagesActivity extends DemoMessagesActivity
        implements MessageInput.InputListener,
        MessageInput.AttachmentsListener {
    private MessagesList messagesList;

    public static void open(Context context) {
        context.startActivity(new Intent(context, DefaultMessagesActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_messages);
        this.messagesList = findViewById(R.id.messagesList);
        initAdapter();
        MessageInput input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(this);
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        Message message = new Message(input.toString().trim(), "0");
        try {
            ApiCalls.sendMessage(this, thread, message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onAddAttachments() {
//        super.messagesAdapter.addToStart(
//                MessagesFixtures.getImageMessage(), true);
    }

    private void initAdapter() {
        super.messagesAdapter = new MessagesListAdapter<>(super.senderId, super.imageLoader);
        super.messagesAdapter.enableSelectionMode(this);
        super.messagesAdapter.setLoadMoreListener(this);

        super.messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
                (view, message) -> Toast.makeText(DefaultMessagesActivity.this, " avatar click", Toast.LENGTH_LONG).show());
        this.messagesList.setAdapter(super.messagesAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageList(MessageList event) {
        if (event.message.thread.getTargetId() == thread.getT_id())
            super.messagesAdapter.addToStart(event.message, true);
    }

}