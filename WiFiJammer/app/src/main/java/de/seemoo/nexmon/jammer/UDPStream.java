package de.seemoo.nexmon.jammer;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Stathis on 05-May-17.
 */

public class UDPStream {
    private final int UDPSTREAM_FIRMWARE_IO_STRUCT_SIZE = 10;

    public enum Modulation {
        IEEE80211b("802.11b", 0), IEEE80211ag("802.11a/g", 1), IEEE80211n("802.11n", 2), IEEE80211ac("802.11ac", 3);

        private final String label;
        private final int value;

        private Modulation(String label, int value) {
            this.label = label;
            this.value = value;
        }

        public static Modulation getModulationFromString(String modulation) {
            switch (modulation) {
                case "802.11b":
                    return Modulation.IEEE80211b;
                case "802.11a/g":
                    return Modulation.IEEE80211ag;
                case "802.11n":
                    return Modulation.IEEE80211n;
                case "802.11ac":
                    return Modulation.IEEE80211ac;
                default:
                    return null;
            }
        }

        public int getInt() {
            return value;
        }

        public String toString() {
            return label;
        }
    }

    int id;
    int destPort;
    boolean running;
    Activity act;
    int power;
    Modulation modulation;
    int rate;
    int bandwidth;
    boolean ldpc;
    int fps;
    AlertDialog alertDialog;


    public UDPStream(int id, int port, int power, Modulation modulation, int rate, int bandwidth, boolean ldpc, int fps, Activity act) {
        this.id = id;
        this.destPort = port;
        this.act = act;
        this.power = power;
        this.modulation = modulation;
        this.rate = rate;
        this.bandwidth = bandwidth;
        this.ldpc = ldpc;
        this.fps = fps;
        running = false;
    }

    public String toString() {
        return "UDPStream: id=" + id + ", power=" + power + ", fps=" + fps + ", destPort=" +
                destPort + ", modulation=" + modulation + ", rate=" + rate + ", bandwidth=" +
                bandwidth + ", ldpc=" + ldpc;
    }

    /**
     * Creates a byte array according to the structure required to create a new udpstream in the
     * firmware:
     * struct udpstream {
     *     uint8 id;
     *     uint8 power;
     *     uint16 fps;
     *     uint16 destPort;
     *     uint8 modulation;
     *     uint8 rate;
     *     uint8 bandwidth;
     *     uint8 ldpc;
     * }
     *
     * @return Return byte array
     */
    public byte[] getBytes() {
        ByteBuffer buf = ByteBuffer.allocate(UDPSTREAM_FIRMWARE_IO_STRUCT_SIZE);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        buf.put((byte) (id & 0xff));
        buf.put((byte) (power & 0xff));
        buf.putShort((short) (fps & 0xffff));
        buf.putShort((short) (destPort & 0xffff));
        buf.put((byte) (modulation.getInt() & 0xff));
        buf.put((byte) (rate & 0xff));
        buf.put((byte) (bandwidth & 0xff));
        buf.put((byte) (ldpc ? (char) 1 : (char) 0));

        return buf.array();
    }
}