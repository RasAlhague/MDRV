package com.rasalhague.mdrv.device.devices;

import com.rasalhague.mdrv.device.core.Device;

import java.util.ArrayList;

/**
 * The Device class template. Use it for add new device support.
 */
public class Dummy extends Device
{
    /**
     * Use in GUI Labels for identify device.
     */
    public final static String FRIENDLY_NAME = "Dummy";

    /**
     * Must be upper case in 16 base. F.e. "1FFB".
     */
    public final static String VENDOR_ID = "1111";

    /**
     * Must be upper case in 16 base.
     */
    public final static String PRODUCT_ID = "1111";

    /**
     * Minimal frequency that device can see. F.e. 2400f.
     */
    public final static float INITIAL_FREQUENCY = 2400f;

    /**
     * Device channel spacing. F.e. 327.450980f.
     */
    public final static float CHANNEL_SPACING = 500f;

    /**
     * Byte or sequence of byte that identify end of packet. "Packet" means RSSI set from INITIAL_FREQUENCY to end of
     * device vision.
     */
    public final static byte[] END_PACKET_SEQUENCE = new byte[]{10};

    /**
     * Set this to TRUE only if you want to control device manually In this case the program will not try to open device
     * and read from customReadMethod() becomes active For example check MetaGeekWiSpyGen1.java class file Works only
     * for HIDUSB devices
     */
    public final static boolean MANUAL_DEVICE_CONTROL = false;

    /**
     * Use this method for initialize your device. If device does not need initialization - leave this blank
     */
    @Override
    public void initializeDevice()
    {

    }

    /**
     * Use this method for parse data which you device out. Return format - Byte array. Every item - RSSI in format
     * "-100".
     */
    @Override
    public ArrayList<Byte> parse(ArrayList<Byte> dataToParse)
    {
        ArrayList<Byte> finalArray = new ArrayList<>(dataToParse);
        finalArray.remove(0);
        return finalArray;
    }

    /**
     * Use this method for override default HIDUSB / COM read behavior. In most cases its usable for HIDUSB devices,
     * when default com.codeminders.hidapi library read method does not work.
     * <p>
     * !!! IMPORTANT !!! If you want to use this method you need to set MANUAL_DEVICE_CONTROL field to TRUE
     */
    @Override
    public byte[] customReadMethod()
    {
        return new byte[0];
    }
}
