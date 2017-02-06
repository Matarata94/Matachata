package matarata.ir.matachata;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;

public class ChatActivity extends AppCompatActivity {

    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    private DatabaseHandler db = new DatabaseHandler(this);
    private Timer tm;
    private int counterSecond=0;
    public static String socketResultChat ="";
    private String queryUsername="";

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
        queryUsername = db.Query(1,1);
        if(registered.equals("no")){
            Intent in = new Intent(ChatActivity.this,RegistrationActivity.class);
            startActivity(in);
            finish();
        }else{
            new Thread(new SocketConnectionThread(RegistrationActivity.SERVER_IP,RegistrationActivity.SERVERPORT)).start();
            new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    if(SocketConnectionThread.socket.isConnected()){
                        new ChatServer(ChatActivity.this).execute("chatData",queryUsername,"dumpText","dateDump");
                        this.cancel();
                    }
                }
                public void onFinish() {
                }
            }.start();
        }
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (Button) findViewById(R.id.chatSendButton);
        TextView meLabel = (TextView) findViewById(R.id.meLbl);
        TextView friendLabel = (TextView) findViewById(R.id.friendLabel);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        showChats("First Test");
        db.close();
    }

    /*private void recieveServerData(){
        tm =new Timer();
        tm.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        ++counterSecond;
                        if(socketResultChat.equals("inserted") | socketResultChat.equals("identified")){
                            db.open();
                            db.Update(username,1,"username");
                            db.Update("yes",1,"registered");
                            db.close();
                            ObjectAnimator colorFade = ObjectAnimator.ofObject(registrationRl, "backgroundColor", new ArgbEvaluator(), Color.parseColor("#00bcd4"), 0xff8bc34a);
                            colorFade.setDuration(2500);
                            colorFade.start();
                            myIndicator.smoothToHide();
                            counterSecond = 0;
                            socketResultChat = "";
                            tm.cancel();
                            Thread closeActivity = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(2000);
                                        Intent in = new Intent(RegistrationActivity.this,ChatActivity.class);
                                        startActivity(in);
                                        finish();
                                    } catch (Exception e) {}
                                }
                            });
                            closeActivity.start();
                        }else if(socketResultChat.equals("identify_failed")){
                            Toast.makeText(getApplicationContext(),"Username exist. Choose another username or enter correct password!" ,Toast.LENGTH_LONG).show();
                            myIndicator.hide();
                            goBtn.setVisibility(View.VISIBLE);
                            goBtn.setText("Go again!");
                            counterSecond = 0;
                            socketResultChat = "";
                            tm.cancel();
                        }else if(socketResultChat.contains("Insert failed") | counterSecond == 5){
                            Toast.makeText(getApplicationContext(),"Failure. Please try again!" ,Toast.LENGTH_LONG).show();
                            myIndicator.hide();
                            goBtn.setVisibility(View.VISIBLE);
                            goBtn.setText("Go again!");
                            counterSecond = 0;
                            socketResultChat = "";
                            tm.cancel();
                        }
                    }
                });
            }
        }, 0, 1000);
    }*/

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    private void showChats(String textMessage){
        chatHistory = new ArrayList<ChatMessage>();
        db.open();
        int lastChatID = Integer.parseInt(db.Query(1,3));
        ChatMessage msg = new ChatMessage();
        msg.setId(lastChatID);
        msg.setMe(true);
        msg.setMessage(textMessage);
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg);
        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);
        db.Update(String.valueOf(lastChatID+1),1,"lastChatId");
        db.close();
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
            }catch (Exception e){}
        }
    }
}
