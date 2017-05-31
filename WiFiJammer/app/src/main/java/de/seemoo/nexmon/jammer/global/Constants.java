package de.seemoo.nexmon.jammer.global;

/**
 * Created by matthias on 13.05.17.
 */

public final class Constants {
    public static final int MAX_IDFT_SIZE = 512;
    public static final int MIN_IDFT_SIZE = 1;
    public static final int OVERSAMPLING_RATE = 2;
    public static final int OUT_OF_BAND_SUBCARRIER_PERCENTAGE = 10;
    public static final int MAX_SLIDERS_COUNT = (int) Math.ceil((100 + OUT_OF_BAND_SUBCARRIER_PERCENTAGE) * (double) MAX_IDFT_SIZE / OVERSAMPLING_RATE / 100);
    public static final int FREQ_PLOT_OVERSAMPLING_RATE = 7;

    public static int getSlidersCount(int ifft_size) {
        return (int) Math.ceil((100 + OUT_OF_BAND_SUBCARRIER_PERCENTAGE) * (double) ifft_size / OVERSAMPLING_RATE / 100);
    }
}
