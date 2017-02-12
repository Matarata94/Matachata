package matarata.ir.matachata;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import static matarata.ir.matachata.SocketConnectionThread.socket;

public class RegistrationServer extends AsyncTask<String, Void, String> {

    private Context context;
    public PrintWriter output;
    public OutputStream out;
    public InputStream input;

    public RegistrationServer(Context context) {
        this.context = context;
    }

    protected void onPreExecute() {

    }

    @Override
    protected String doInBackground(String... arg0) {
        String requestType = arg0[0];
        String username = arg0[1];
        String password = arg0[2];
        String opponentUsername = arg0[3];
        String result = "";

        try {
            ///Socket Connection
            JSONObject myJsonObject = new JSONObject();
            myJsonObject.put("requestType", requestType);
            myJsonObject.put("username", username);
            myJsonObject.put("opponentUsername", opponentUsername);
            myJsonObject.put("password", password);
            out = socket.getOutputStream();
            output = new PrintWriter(out);
            output.println(myJsonObject);
            output.flush();
            input = socket.getInputStream();
            int lockSeconds = 10*1000;
            long lockThreadCheckpoint = System.currentTimeMillis();
            int availableBytes = input.available();
            while(availableBytes <= 0 && (System.currentTimeMillis() < lockThreadCheckpoint + lockSeconds)){
                try{
                    Thread.sleep(10);
                }catch(InterruptedException ie){}
                availableBytes = input.available();
            }
            byte[] buffer = new byte[availableBytes];
            input.read(buffer, 0, availableBytes);
            result = new String(buffer);

            return result;
        } catch (Exception e) {
            return new String("Exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(String result) {
        String jsonStr = result;
        if (jsonStr != "") {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                String jsonTempResult = jsonObj.getString("serverJsonResult");
                RegistrationActivity.socketResultRegister = jsonTempResult;
            } catch (JSONException e) {
                //Toast.makeText(context, jsonStr + "\nJson Error: " + e.toString(), Toast.LENGTH_LONG).show();
            }
        } else {
            //Toast.makeText(context, "Couldn't get any JSON data.", Toast.LENGTH_LONG).show();
        }
    }
}