package de.seemoo.nexmon.jammer;

import android.app.Activity;
import android.support.v7.app.AlertDialog;

/**
 * Created by Stathis on 05-May-17.
 */

public class UDPStream {

    int id;
    int destPort;
    boolean running;
    Activity act;
    int power;
    String modulation;
    int rate;
    int bandwidth;
    boolean ldpc;
    int numbFrames;
    AlertDialog alertDialog;


    public UDPStream(int id, int port, int power, String modulation, int rate, int bandwidth, boolean ldpc, int numbFrames, Activity act) {
        this.id = id;
        this.destPort = port;
        this.act = act;
        this.power = power;
        this.modulation = modulation;
        this.rate = rate;
        this.bandwidth = bandwidth;
        this.ldpc = ldpc;
        this.numbFrames = numbFrames;
        running = false;

    }
}