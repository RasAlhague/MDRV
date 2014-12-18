package com.rasalhague.mdrv.device.devices;

import com.rasalhague.mdrv.device.core.Device;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The Device class template. Use it for add new device support.
 */
public class MetaGeekWiSpy24x2 extends Device
{
    public final static String FRIENDLY_NAME       = "MetaGeek WiSpy 24x2";
    public final static String VENDOR_ID           = "1DD5";
    public final static String PRODUCT_ID          = "2410";
    public final static float  INITIAL_FREQUENCY   = 2400f;
    public final static float  CHANNEL_SPACING     = 327.586f;
    public final static byte[] END_PACKET_SEQUENCE = new byte[]{74, 0, 0, 0};
    public final static boolean MANUAL_DEVICE_CONTROL = false;

    /**
     * Use this method for initialize your device.
     */
    @Override
    public void initializeDevice()
    {
        byte[] dataToWrite = new byte[]{0x53, 0x10, 0x11,
                0x00, (byte) 0x9F, 0x24, 0x00, (byte) 0xC4, 0x15, 0x05, 0x00,
                0x6C,
                (byte) 0xDC,
                0x02,
                0x00,
                0x1E,
                0x01,
                0x64,
                0x01,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00};

        try
        {
            if (SystemUtils.IS_OS_LINUX)
            {
                deviceCommunication.hidDevice.write(dataToWrite);
                ApplicationLogger.LOGGER.info("MetaGeek_WiSpy24x2 has been initialized");
            }
            else if (SystemUtils.IS_OS_WINDOWS)
            {
                ApplicationLogger.LOGGER.info("MetaGeek_WiSpy24x2 has not been initialized due to OS");
            }
        }
        catch (IOException e)
        {
            ApplicationLogger.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Use this method for parse data which you device out. Return format - Byte array. Every item - RSSI in format
     * "-100".
     */
    @Override
    public ArrayList<Byte> parse(ArrayList<Byte> dataToParse)
    {
        final int devPassLength = 64;
        final int serviceBytesCount = 5;

        ArrayList<Byte> clearPointArray = new ArrayList<>();
        for (int i = 0, dataToProcessSize = dataToParse.size(); i < dataToProcessSize; i++)
        {
            Byte data = dataToParse.get(i);
            if ((i % devPassLength) == 0)
            {
                i += serviceBytesCount;
            }
            else
            {
                clearPointArray.add((byte) (data - 170));
            }
        }

        return clearPointArray;
    }

    /**
     * Use this method for override default HIDUSB / COM read behavior. In most cases its usable for HIDUSB devices,
     * when default com.codeminders.hidapi library read method does not work.
     * <p>
     * !!! IMPORTANT !!! If you want to use this method you need to set USE_CUSTOM_READ_METHOD field to TRUE
     */
    @Override
    public byte[] customReadMethod()
    {
        return new byte[0];
    }
}
