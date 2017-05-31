package de.seemoo.nexmon.jammer.global;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by stathis on 5/14/17.
 */

public final class Variables {

    public static double[] amps;
    public static double[] phases;
    public static double[] freqs;
    public static int idft_size;
    public static int bandwidth;
    public static int jammingSignalRepetitions;
    public static int jammingPower;
    public static int app;
    public static JammingType jammerType;
    public static int channel;
    public static int jammerStart;
    public static int jammingPort;
    public static double samplingRate = 0;

    public enum JammingType {
        DISABLED_JAMMER("Disabled Jammer", 0), SIMPLE_REACTIVE_JAMMER("Simple Reactive Jammer", 1), ACKNOWLEDGING_JAMMER("Acknowledging Jammer", 2), ADAPTIVE_POWER_CONTROL_JAMMER("Adaptive Power Control Jammer", 3);

        private final String label;
        private final int value;

        private JammingType(String label, int value) {
            this.label = label;
            this.value = value;
        }

        public static JammingType getJammingTypeFromString(String modulation) {
            switch (modulation) {
                case "Disabled Jammer":
                    return JammingType.DISABLED_JAMMER;
                case "Simple Reactive Jammer":
                    return JammingType.SIMPLE_REACTIVE_JAMMER;
                case "Acknowledging Jammer":
                    return JammingType.ACKNOWLEDGING_JAMMER;
                case "Adaptive Power Control Jammer":
                    return JammingType.ADAPTIVE_POWER_CONTROL_JAMMER;
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

    public Variables(double[] amps, double[] phases, double[] freqs, int idft_size, int bandwidth) {
        Variables.amps = amps;
        Variables.phases = phases;
        Variables.freqs = freqs;
        Variables.idft_size = idft_size;
        Variables.bandwidth = bandwidth;
    }

    /**
     * Creates a byte array according to the structure required to start jamming in the firmware:
     * struct jamming_settings {
     *     uint16 idftSize;
     *     uint16 port;
     *     int16 numActiveSubcarriers;
     *     uint8 jammingType;
     *     uint16 jammingSignalRepetitions;
     *     cint16ap freqDomSamps[];
     * }
     *
     * @return Return byte array
     */
    public static byte[] getBytes() {
        int numActiveSubcarriers = amps.length;
        ByteBuffer buf = ByteBuffer.allocate(2 + 2 + 2 + 1 + 2 + numActiveSubcarriers * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        buf.putShort((short) (idft_size & 0xffff));
        buf.putShort((short) (jammingPort & 0xffff));
        buf.putShort((short) numActiveSubcarriers);
        buf.put((byte) (jammerType.getInt()));
        buf.putShort((short) (jammingSignalRepetitions & 0xffff));

        for (int i = 0; i < numActiveSubcarriers; i++) {
            buf.putShort((short) (amps[i] * 512));
            buf.putShort((short) (phases[i] / Math.PI * 180));
        }

        return buf.array();
    }
}
