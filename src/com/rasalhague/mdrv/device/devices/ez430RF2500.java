package com.rasalhague.mdrv.device.devices;

import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.device.core.Device;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import org.apache.commons.lang3.SystemUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Device class template. Use it for add new device support.
 */
public class ez430RF2500 extends Device
{
    /**
     * Use in GUI Labels for identify device.
     */
    public final static String FRIENDLY_NAME = "Texas Instruments ez430RF2500";

    /**
     * Must be upper case in 16 base. F.e. "1FFB".
     */
    public final static String VENDOR_ID = "0451";

    /**
     * Must be upper case in 16 base.
     */
    public final static String PRODUCT_ID = "F432";

    /**
     * Minimal frequency that device can see. F.e. 2400f.
     */
    public final static float INITIAL_FREQUENCY = 2400f;

    /**
     * Device channel spacing. F.e. 327.450980f.
     */
    public final static float CHANNEL_SPACING = 249.938965f;

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
        if (SystemUtils.IS_OS_WINDOWS)
        {
            final int initRetryingDelay = 500;

            try
            {
                int initRetryingCounter = 1;
                while (true)
                {
                    ApplicationLogger.LOGGER.info(initRetryingCounter++ + " try to init ez430RF2500");

                    deviceCommunication.serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8,
                                                             SerialPort.STOPBITS_1,
                                                             SerialPort.PARITY_NONE);

                    try
                    {
                        deviceCommunication.serialPort.readString(1, initRetryingDelay);

                        break;
                    }
                    catch (SerialPortTimeoutException ignored)
                    {
                        //                    System.out.println(ignored.getMessage());
                    }
                }
            }
            catch (SerialPortException e)
            {
                ApplicationLogger.LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }
        }
        else if (SystemUtils.IS_OS_LINUX)
        {
            ApplicationLogger.LOGGER.info("Executing \n" +
                                                  "stty 9600 -icrnl -opost -onlcr -isig -icanon -iexten -echo -crterase -echok -echoctl -echoke -F " +
                                                  deviceInfo.getPortName());

            Utils.runShellScript(
                    "stty 9600 -icrnl -opost -onlcr -isig -icanon -iexten -echo -crterase -echok -echoctl -echoke -F " +
                            deviceInfo.getPortName());
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
