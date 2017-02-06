package matarata.ir.matachata;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Button;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Timer;
import java.util.TimerTask;

public class RegistrationActivity extends AppCompatActivity {

    AVLoadingIndicatorView myIndicator;
    Button goBtn;
    RelativeLayout registrationRl;
    MaterialEditText usernameET,passwordET;
    private Timer tm;
    private int counterSecond=0;
    public static final int SERVERPORT = 30000;
    public static final String SERVER_IP = "192.168.1.3";
    public static String socketResult ="";
    private DatabaseHandler db = new DatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        myIndicator = (AVLoadingIndicatorView) findViewById(R.id.registration_indicator);
        goBtn = (Button) findViewById(R.id.registration_go);
        registrationRl = (RelativeLayout) findViewById(R.id.registration_rl);
        usernameET = (MaterialEditText) findViewById(R.id.registration_username);
        passwordET = (MaterialEditText) findViewById(R.id.registration_password);
        new Thread(new SocketConnectionThread(SERVER_IP,SERVERPORT)).start();

        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = usernameET.getText().toString();
                String password = passwordET.getText().toString();
                if(username.equals("") | password.equals("")){
                    Toast.makeText(getApplicationContext(),"Please enter username and password!",Toast.LENGTH_LONG).show();
                }else{
                    new RegistrationServer(RegistrationActivity.this).execute(username,password);
                    goBtn.setVisibility(View.INVISIBLE);
                    myIndicator.smoothToShow();
                    tm =new Timer();
                    tm.scheduleAtFixedRate(new TimerTask() {
                        public void run() {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    ++counterSecond;
                                    if(socketResult.equals("inserted") | socketResult.equals("identified")){
                                        db.open();
                                        db.Update(username,1,"username");
                                        db.Update("yes",1,"registered");
                                        db.close();
                                        ObjectAnimator colorFade = ObjectAnimator.ofObject(registrationRl, "backgroundColor", new ArgbEvaluator(), Color.parseColor("#00bcd4"), 0xff8bc34a);
                                        colorFade.setDuration(2500);
                                        colorFade.start();
                                        myIndicator.smoothToHide();
                                        counterSecond = 0;
                                        socketResult = "";
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
                                    }else if(socketResult.equals("identify_failed")){
                                        Toast.makeText(getApplicationContext(),"Username exist. Choose another username or enter correct password!" ,Toast.LENGTH_LONG).show();
                                        myIndicator.hide();
                                        goBtn.setVisibility(View.VISIBLE);
                                        goBtn.setText("Go again!");
                                        counterSecond = 0;
                                        socketResult = "";
                                        tm.cancel();
                                    }else if(socketResult.contains("Insert failed") | counterSecond == 5){
                                        Toast.makeText(getApplicationContext(),"Failure. Please try again!" ,Toast.LENGTH_LONG).show();
                                        myIndicator.hide();
                                        goBtn.setVisibility(View.VISIBLE);
                                        goBtn.setText("Go again!");
                                        counterSecond = 0;
                                        socketResult = "";
                                        tm.cancel();
                                    }
                                }
                            });
                        }
                    }, 0, 1000);
                }
            }
        });

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
