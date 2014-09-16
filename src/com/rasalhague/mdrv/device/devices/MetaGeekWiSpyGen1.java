package com.rasalhague.mdrv.device.devices;

import com.rasalhague.mdrv.device.core.Device;
import com.rasalhague.mdrv.logging.ApplicationLogger;

import java.util.ArrayList;

/**
 * The Device class template. Use it for add new device support.
 */
public class MetaGeekWiSpyGen1 extends Device
{
    /**
     * Use in GUI Labels for identify device.
     */
    public final static String FRIENDLY_NAME = "MetaGeek Wi-Spy";

    /**
     * Must be upper case in 16 base. F.e. "1FFB".
     */
    public final static String VENDOR_ID = "1781";

    /**
     * Must be upper case in 16 base.
     */
    public final static String PRODUCT_ID = "083E";

    /**
     * Minimal frequency that device can see. F.e. 2400f.
     */
    public final static float INITIAL_FREQUENCY = 2399f;

    /**
     * Device channel spacing. F.e. 327.450980f.
     */
    public final static float CHANNEL_SPACING = 989f;

    /**
     * Byte or sequence of byte that identify end of packet. "Packet" means RSSI set from INITIAL_FREQUENCY to end of
     * device vision.
     */
    public final static byte[] END_PACKET_SEQUENCE = new byte[]{0, 0};

    /**
     * Use this method for initialize your device.
     */
    @Override
    public void initializeDevice()
    {
        ApplicationLogger.LOGGER.warning("MetaGeekWiSpyGen1 Do not work on linux without kernel module detaching.");
    }

    /**
     * Use this method for parse data which you device out. Return format - Byte array. Every item - RSSI in format
     * "-100".
     */
    @Override
    public ArrayList<Byte> parse(ArrayList<Byte> dataToParse)
    {
        System.out.println(dataToParse);

        return null;
    }
}
