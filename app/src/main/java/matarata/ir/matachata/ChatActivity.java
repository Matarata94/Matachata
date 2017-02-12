package matarata.ir.matachata;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity {

    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    private DatabaseHandler db = new DatabaseHandler(this);
    private Timer tm;
    private int counterSecond=0;
    public static String socketResultChat="", socketRequestType = "";
    public static String[] socketUsernamesHistory ={}, socketMessagesHistory ={}, socketDatasHistory ={};
    private String queryUsername="",queryOpponentUsername="",registered="",messageText="";
    private AVLoadingIndicatorView myIndicator;
    private ConnectivityManager connectivityManager;
    private NetworkInfo activeNetworkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initiate();
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }
                if(!isNetworkAvailable()){
                    Toast.makeText(getApplicationContext(),"Please check your connection!",Toast.LENGTH_LONG).show();
                    return;
                }
                sendMsgToServer();
            }
        });

    }

    private void initiate(){
        myIndicator = (AVLoadingIndicatorView) findViewById(R.id.chatActivity_indicator);
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (Button) findViewById(R.id.chatSendButton);
        TextView meLabel = (TextView) findViewById(R.id.meLbl);
        TextView friendLabel = (TextView) findViewById(R.id.friendLabel);
        db.databasecreate();
        db.open();
        queryUsername = db.Query(1,1);
        queryOpponentUsername = db.Query(1,2);
        registered = db.Query(1,3);
        db.close();
        meLabel.setText(queryUsername);
        friendLabel.setText(queryOpponentUsername);
        if(registered.equals("no")){
            Intent in = new Intent(ChatActivity.this,RegistrationActivity.class);
            startActivity(in);
            finish();
        }else{
            new Thread(new SocketConnectionThread(RegistrationActivity.SERVER_IP,RegistrationActivity.SERVERPORT)).start();
            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    if(SocketConnectionThread.socket.isConnected()){
                        socketRequestType = "chatsHistory";
                        new ChatServer(ChatActivity.this).execute("chatHistoryData",queryUsername,queryOpponentUsername,"dumpText","dumpDate");
                        receiveServerData();
                        this.cancel();
                    }
                }
                public void onFinish() {
                    if(!SocketConnectionThread.socket.isConnected()){
                        try{
                            SocketConnectionThread.socket.close();
                        }catch (Exception e){}
                        initiate();
                    }
                }
            }.start();
        }
    }

    private void receiveServerData(){
        tm =new Timer();
        tm.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        ++counterSecond;
                        if(socketResultChat.equals("fetchHistoryChatDone")){
                            showChatsHistory(socketUsernamesHistory,socketMessagesHistory,socketDatasHistory);
                            counterSecond = 0;
                            socketResultChat = "";
                            tm.cancel();
                        }else if(counterSecond == 7){
                            Toast.makeText(getApplicationContext(),"Failure. Please try again!"+"\n"+socketResultChat ,Toast.LENGTH_LONG).show();
                            try{
                                SocketConnectionThread.socket.close();
                            }catch (Exception e){}
                            counterSecond = 0;
                            socketResultChat = "";
                            tm.cancel();
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    private void showChatsHistory(String[] usernames, String[] messages, String[] dates){
        chatHistory = new ArrayList<ChatMessage>();
        db.open();
        String meUsername = db.Query(1,1);
        String opponentUsername = db.Query(1,2);
        for(int i=0;usernames.length > i;i++){
            ChatMessage msg = new ChatMessage();
            msg.setId(i+1);
            if(usernames[i].equals(meUsername)){
                msg.setMe(true);
            }else if(usernames[i].equals(opponentUsername)){
                msg.setMe(false);
            }
            msg.setMessage(messages[i]);
            msg.setDate(dates[i]);
            chatHistory.add(msg);
        }
        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);
        db.Update(String.valueOf(usernames.length+1),1,"lastChatId");
        db.close();
        for(int i=0; i<chatHistory.size(); i++) {
            ChatMessage message = chatHistory.get(i);
            displayMessage(message);
        }
        Arrays.fill(socketUsernamesHistory, null);
        Arrays.fill(socketMessagesHistory, null);
        Arrays.fill(socketDatasHistory, null);
        myIndicator.smoothToHide();
    }

    private void sendMsgToServer(){
        socketRequestType = "sendMsg";
        db.open();
        String tempUsername = db.Query(1,1);
        String tempOpponentUsername = db.Query(1,2);
        String tempDate = DateFormat.getDateTimeInstance().format(new Date());
        db.close();
        if(SocketConnectionThread.socket.isConnected()){
            new ChatServer(ChatActivity.this).execute("msgSend",tempUsername,tempOpponentUsername,messageText,tempDate);
            tm =new Timer();
            tm.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ++counterSecond;
                            if(socketResultChat.equals("recieved")){
                                ChatMessage chatMessage = new ChatMessage();
                                chatMessage.setId(122);
                                chatMessage.setMessage(messageText);
                                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                                chatMessage.setMe(true);
                                messageET.setText("");
                                displayMessage(chatMessage);
                                counterSecond = 0;
                                socketResultChat = "";
                                tm.cancel();
                            }else if(socketResultChat.contains("insert chat failed")){
                                Toast.makeText(getApplicationContext(),"Failure. Please try again!" ,Toast.LENGTH_LONG).show();
                                counterSecond = 0;
                                socketResultChat = "";
                                tm.cancel();
                            }else if(counterSecond == 4){
                                try{
                                    SocketConnectionThread.socket.close();
                                }catch (Exception e){}
                                counterSecond = 0;
                                socketResultChat = "";
                                tm.cancel();
                                new Thread(new SocketConnectionThread(RegistrationActivity.SERVER_IP,RegistrationActivity.SERVERPORT)).start();
                                new CountDownTimer(4000, 1000) {
                                    public void onTick(long millisUntilFinished) {
                                        if(SocketConnectionThread.socket.isConnected()){
                                            this.cancel();
                                            sendMsgToServer();
                                        }
                                    }
                                    public void onFinish() {}
                                }.start();
                            }
                        }
                    });
                }
            }, 0, 1000);
        }else{
            new Thread(new SocketConnectionThread(RegistrationActivity.SERVER_IP,RegistrationActivity.SERVERPORT)).start();
            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    if(SocketConnectionThread.socket.isConnected()){
                        this.cancel();
                        sendMsgToServer();
                    }
                }
                public void onFinish() {}
            }.start();
        }
    }

    private boolean isNetworkAvailable() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
