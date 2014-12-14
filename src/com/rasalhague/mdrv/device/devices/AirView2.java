package com.rasalhague.mdrv.device.devices;

import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.device.core.Device;
import jssc.SerialPortException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Device class template. Use it for add new device support.
 */
public class AirView2 extends Device
{
    /**
     * Use in GUI Labels for identify device.
     */
    public final static String FRIENDLY_NAME = "Air View 2";

    /**
     * Must be upper case in 16 base. F.e. "1FFB".
     */
    public final static String VENDOR_ID = "1F9B";

    /**
     * Must be upper case in 16 base.
     */
    public final static String PRODUCT_ID = "0241";

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
     * Set this to TRUE only if you want to control device manually
     * In this case the program will not try to open device and read from
     * customReadMethod() becomes active
     * For example check MetaGeekWiSpyGen1.java class file
     */
    public final static boolean MANUAL_DEVICE_CONTROL = false;

    /**
     * Use this method for initialize your device.
     */
    @Override
    public void initializeDevice()
    {
        byte[] intByte = new byte[]{0x69, 0x6E, 0x74}; //int
        //        byte[] gdiByte = new byte[]{0x0A, 0x67, 0x64, 0x69, 0x0A}; //.gdi.
        byte[] bsByte = new byte[]{0x0A, 0x62, 0x73, 0x0A}; //.bs.

        try
        {
            //            Thread.sleep(5000);
            deviceCommunication.serialPort.writeBytes(intByte);
            //            serialPort.writeBytes(gdiByte);
            deviceCommunication.serialPort.writeBytes(bsByte);

            //sleep for 5 sec? why?
            Thread.sleep(5000);
        }
        catch (SerialPortException | InterruptedException e)
        {
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
        String strToProcess = Utils.byteArrayListToCharToString(dataToParse);

        Pattern pattern = Pattern.compile("(?<data>-\\d{2,3})");
        Matcher matcher = pattern.matcher(strToProcess);
        ArrayList<Byte> list = new ArrayList<>();
        while (matcher.find())
        {
            Byte itemToAdd = Byte.parseByte(matcher.group("data"));
            list.add(itemToAdd);
        }

        return list;
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
