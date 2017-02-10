package matarata.ir.matachata;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketConnectionThread implements Runnable {

    public static Socket socket;
    private String SERVER_IP;
    private int SERVERPORT;

    public SocketConnectionThread(String serverIP,int serverPORT){
        this.SERVER_IP = serverIP;
        this.SERVERPORT = serverPORT;
    }

    @Override
    public void run() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(SERVER_IP, SERVERPORT));
        }catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
