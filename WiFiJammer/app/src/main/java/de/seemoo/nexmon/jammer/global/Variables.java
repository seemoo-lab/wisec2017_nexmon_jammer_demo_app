package de.seemoo.nexmon.jammer.global;

/**
 * Created by stathis on 5/14/17.
 */

public final class Variables {

    public static double[] amps;
    public static double[] phases;
    public static double[] freqs;
    public static int idft_size;
    public static int bandwidth;
    public static double jamSignalLength;
    public static int jammingPower;
    public static int app;
    public static String jammerType;
    public static int channel;
    public static int jammerStart;
    public static int jammingPort;
    public static double samplingRate = 0;

    public Variables(double[] amps, double[] phases, double[] freqs, int idft_size, int bandwidth) {
        Variables.amps = amps;
        Variables.phases = phases;
        Variables.freqs = freqs;
        Variables.idft_size = idft_size;
        Variables.bandwidth = bandwidth;
    }


}
