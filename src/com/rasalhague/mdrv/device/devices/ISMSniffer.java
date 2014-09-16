package com.rasalhague.mdrv.device.devices;

import com.rasalhague.mdrv.device.core.Device;

import java.util.ArrayList;

/**
 * The Device class template. Use it for add new device support.
 */
public class ISMSniffer extends Device
{
    /**
     * Use in GUI Labels for identify device.
     */
    public final static String FRIENDLY_NAME = "Unigen ISM Sniffer";

    /**
     * Must be upper case in 16 base. F.e. "1FFB".
     */
    public final static String VENDOR_ID = "1C79";

    /**
     * Must be upper case in 16 base.
     */
    public final static String PRODUCT_ID = "2001";

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

    }

    /**
     * Use this method for parse data which you device out. Return format - Byte array. Every item - RSSI in format
     * "-100".
     */
    @Override
    public ArrayList<Byte> parse(ArrayList<Byte> dataToParse)
    {
        ArrayList<Byte> arrayList = new ArrayList<>();
        byte byteCounter = 0;
        for (int i = 0; i < dataToParse.size(); )
        {
            if (dataToParse.get(i) == byteCounter)
            {
                //                System.out.println(dataToProcess.size());
                //                System.out.println(dataToProcess.size());
                if (dataToParse.get(i + 1) == byteCounter)
                {
                    arrayList.add(dataToParse.get(i + 2));
                    byteCounter++;
                    i += 3;
                }
                else
                {
                    i++;
                }
            }
            else
            {
                i++;
            }
        }

        ArrayList<Byte> finalArrayList = new ArrayList<>();
        for (Byte aByte : arrayList)
        {
            //$frequency[$i] = ((hexdec($frequency_temp[3*$i]) - 254) + 82) * 1.428 - 97;
            //            finalArrayList.add((aByte - 135));
            finalArrayList.add((byte) ((((aByte - 135) + 100) * 1.428) - 100));
        }

        return finalArrayList;
    }
}
