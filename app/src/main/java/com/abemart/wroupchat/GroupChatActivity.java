package com.abemart.wroupchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.abemart.wroup.client.WroupClient;
import com.abemart.wroup.common.WiFiP2PInstance;
import com.abemart.wroup.common.WroupDevice;
import com.abemart.wroup.common.listeners.DataReceivedListener;
import com.abemart.wroup.common.messages.MessageWrapper;
import com.abemart.wroup.service.WroupService;

import java.util.ArrayList;
import java.util.List;


public class GroupChatActivity extends AppCompatActivity implements DataReceivedListener {

    public static final String EXTRA_GROUP_NAME = "groupNameExtra";
    public static final String EXTRA_IS_GROUP_OWNER = "isGroupOwnerExtra";

    private ListView listViewChat;
    private List<MessageWrapper> messages;
    private ChatAdapter chatAdapter;

    private String groupName;
    private boolean isGroupOwner = false;

    private WroupService wroupService;
    private WroupClient wroupClient;
    private WroupDevice currentDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        messages = new ArrayList<>();
        currentDevice = WiFiP2PInstance.getInstance(getApplicationContext()).getThisDevice();
        chatAdapter = new ChatAdapter(getApplicationContext(), messages, currentDevice);

        Intent startIntent = getIntent();
        groupName = startIntent.getStringExtra(EXTRA_GROUP_NAME);
        isGroupOwner = startIntent.getBooleanExtra(EXTRA_IS_GROUP_OWNER, false);

        if (isGroupOwner) {
            wroupService = WroupService.getInstance(getApplicationContext());
            wroupService.setDataReceivedListener(this);
        } else {
            wroupClient = WroupClient.getInstance(getApplicationContext());
            wroupClient.setDataReceivedListener(this);
        }

        listViewChat = (ListView) findViewById(R.id.list_view_group_chat);
        Button btnSend = (Button) findViewById(R.id.button_send_message);
        final EditText editTextMessage = (EditText) findViewById(R.id.edit_text_chat_message);

        listViewChat.setAdapter(chatAdapter);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageWrapper normalMessage = new MessageWrapper();
                normalMessage.setMessage(editTextMessage.getText().toString());
                normalMessage.setMessageType(MessageWrapper.MessageType.NORMAL);

                if (isGroupOwner) {
                    wroupService.sendMessageToAllClients(normalMessage);
                } else {
                    wroupClient.sendMessageToAllClients(normalMessage);
                }

                chatAdapter.add(normalMessage);
                editTextMessage.setText("");
            }
        });

        setActionBarTitle(groupName);
    }

    private void setActionBarTitle(String title) {
        if (getActionBar() != null) {
            getActionBar().setTitle(title);
        }
    }

    @Override
    public void onDataReceived(final MessageWrapper messageWrapper) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatAdapter.add(messageWrapper);
            }
        });
    }

}
