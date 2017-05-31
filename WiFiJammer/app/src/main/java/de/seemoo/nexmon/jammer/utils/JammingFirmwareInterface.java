package de.seemoo.nexmon.jammer.utils;

/**
 * Created by matthias on 31.05.17.
 */

public class JammingFirmwareInterface {
    protected static JammingFirmwareInterface instance;

    protected JammingFirmwareInterface() {

    }

    public static JammingFirmwareInterface getInstance() {
        return instance == null ? new JammingFirmwareInterface() : instance;
    }


}
