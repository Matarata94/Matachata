package matarata.ir.matachata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    private DatabaseHandler db = new DatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initiate();
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122);//dummy
                chatMessage.setMessage(messageText);
                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatMessage.setMe(true);
                messageET.setText("");
                displayMessage(chatMessage);
            }
        });

    }

    private void initiate(){
        db.databasecreate();
        db.open();
        String registered = db.Query(1,2);
        if(registered.equals("no")){
            Intent in = new Intent(ChatActivity.this,RegistrationActivity.class);
            startActivity(in);
            finish();
        }else{
            new Thread(new SocketConnectionThread(RegistrationActivity.SERVER_IP,RegistrationActivity.SERVERPORT)).start();
            messagesContainer = (ListView) findViewById(R.id.messagesContainer);
            messageET = (EditText) findViewById(R.id.messageEdit);
            sendBtn = (Button) findViewById(R.id.chatSendButton);
            TextView meLabel = (TextView) findViewById(R.id.meLbl);
            TextView friendLabel = (TextView) findViewById(R.id.friendLabel);
            RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
            loadDummyHistory();
        }
        db.close();
    }

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    private void loadDummyHistory(){

        chatHistory = new ArrayList<ChatMessage>();

        ChatMessage msg = new ChatMessage();
        msg.setId(1);
        msg.setMe(false);
        msg.setMessage("Hi");
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg);
        ChatMessage msg1 = new ChatMessage();
        msg1.setId(2);
        msg1.setMe(false);
        msg1.setMessage("How r u doing???");
        msg1.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg1);

        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

        for(int i=0; i<chatHistory.size(); i++) {
            ChatMessage message = chatHistory.get(i);
            displayMessage(message);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(SocketConnectionThread.socket.isConnected()){
            try{
                SocketConnectionThread.socket.close();
                Toast.makeText(getApplicationContext(),"Socket Closed!",Toast.LENGTH_LONG).show();
            }catch (Exception e){}
        }
    }
}
