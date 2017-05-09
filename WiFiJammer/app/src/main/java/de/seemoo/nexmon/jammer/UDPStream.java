package de.seemoo.nexmon.jammer;

import android.app.Activity;

import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Stathis on 05-May-17.
 */

public class UDPStream {

    int serverPort;
    DatagramSocket socket;
    InetAddress address;
    int port;
    boolean running;
    Activity act;


    public UDPStream(int serverPort, InetAddress address, int port, Activity act) {
        super();
        this.serverPort = serverPort;
        this.address = address;
        this.port = port;
        this.act = act;
    }

    public void stopThread() {
        running = false;
    }


}