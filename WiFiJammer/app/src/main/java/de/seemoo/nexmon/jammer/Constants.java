package de.seemoo.nexmon.jammer;

/**
 * Created by matthias on 13.05.17.
 */

public final class Constants {
    static final int MAX_IDFT_SIZE = 512;
    static final int MIN_IDFT_SIZE = 1;
    static final int OVERSAMPLING_RATE = 2;
    static final int OUT_OF_BAND_SUBCARRIER_PERCENTAGE = 10;
    static final int MAX_SLIDERS_COUNT = (100 + OUT_OF_BAND_SUBCARRIER_PERCENTAGE) * MAX_IDFT_SIZE / OVERSAMPLING_RATE / 100;

    static int getSlidersCount(int ifft_size) {
        return (int) Math.ceil((100 + OUT_OF_BAND_SUBCARRIER_PERCENTAGE) * (double) ifft_size / OVERSAMPLING_RATE / 100);
    }
}
