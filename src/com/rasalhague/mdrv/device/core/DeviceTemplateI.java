package com.rasalhague.mdrv.device.core;

import java.util.ArrayList;

public interface DeviceTemplateI
{
    /**
     * Use this method for initialize your device. If device does not need initialization - leave this blank
     */
    public void initializeDevice();

    /**
     * Use this method for parse data which you device out. Return format - Byte array. Every item - RSSI in format
     * "-100".
     */
    public ArrayList<Byte> parse(ArrayList<Byte> dataToParse);

    /**
     * Use this method for override default HIDUSB / COM read behavior. In most cases its usable for HIDUSB devices,
     * when default com.codeminders.hidapi library read method does not work.
     * <p>
     * !!! IMPORTANT !!! If you want to use this method you need to set USE_CUSTOM_READ_METHOD field to TRUE
     */
    public abstract byte[] customReadMethod();
}
