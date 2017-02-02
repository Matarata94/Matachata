package matarata.ir.matachata;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static matarata.ir.matachata.RegistrationActivity.SERVERPORT;
import static matarata.ir.matachata.RegistrationActivity.SERVER_IP;

class SocketConnectionThread implements Runnable {

    public static Socket socket;

    @Override
    public void run() {
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            socket = new Socket(serverAddr, SERVERPORT);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
