package seemo.wifijammer;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;

import static android.content.ContentValues.TAG;

/**
 * Created by Stathis on 05-May-17.
 */

public class UDPStream extends Thread{

    int serverPort;
    DatagramSocket socket;
    InetAddress address;
    int port;
    boolean running;
    Activity act;// mode 0: transmitter, mode 1: receiver


    public UDPStream(int serverPort, InetAddress address, int port, Activity act) {
        super();
        this.serverPort = serverPort;
        this.address = address;
        this.port = port;
        this.act = act;
    }

    public void stopThread(){
        running = false;
    }


    @Override
    public void run() {
        running = true;
        try {
            Log.e(TAG,"Starting UDP Socket");

            socket = new DatagramSocket(serverPort);

            Log.e(TAG, "UDP Socket is running");


                Log.e(TAG,"New Transmitter Thread");

                while(running){
                    byte[] buf = new byte[1472];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
                    socket.send(packet);
                    System.out.println("sent packet");
                    sleep(100);
                }


        } catch (Exception e) {
            act.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(act.getApplicationContext(), "UDP Stream could not be started, please check your internet connection and try again.", Toast.LENGTH_LONG).show();

                }
            });
            e.printStackTrace();
        }  finally {
            running = false;
            if(socket != null){
                socket.close();
                Log.e(TAG, "UDP Socket is closed");

            }
        }
    }
}