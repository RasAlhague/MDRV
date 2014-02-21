package com.rasalhague.mdrv;

import java.util.ArrayList;
import java.util.Observable;

/**
 * receiveRawData(String rawData) take a input string from device and works like a buffer
 * for processReceivedChar(char receivedChar) that parsing char by char generate a RxRawDataPacket;
 */

public class RxRawDataReceiver extends Observable
{
    private ArrayList<RxRawDataPacket> rawDataPackets;
    private String rawDataPacketBuffer = "";

    private final static char END_PACKET_CHAR = '\n';

    private Boolean firstPacketTrigger = true;
    private DeviceInfo deviceInfo;

    public RxRawDataReceiver(DeviceInfo deviceInfo)
    {
        this.deviceInfo = deviceInfo;
        rawDataPackets = new ArrayList<RxRawDataPacket>();
    }

    private void saveRawDataPacket()
    {
        if (firstPacketTrigger)
        {
            firstPacketTrigger = false;
        }
        else
        {
            RxRawDataPacket rxRawDataPacket = new RxRawDataPacket(rawDataPacketBuffer, deviceInfo);
            rawDataPackets.add(rxRawDataPacket);
//            checkForLength_RxDataReceivedArray();

            //TODO setChanged(); Wont work
            setChanged();
            notifyObservers(rawDataPackets);
        }

        wipeRawDataPacketBuffer();
    }

    private void addDataToRawDataPacketBuffer(char data)
    {
        rawDataPacketBuffer += data;
    }

    private void wipeRawDataPacketBuffer()
    {
        rawDataPacketBuffer = "";
    }

    /**
     * Works like a buffer. Sends data char by char to processReceivedChar(char receivedChar);
     *
     * @param rawData string from device
     */
    public void receiveRawData(String rawData)
    {
        //for AirView2
//        CharSequence charSequence = "scan|0,";
//        rawData = rawData.replace(charSequence, "");

        char[] charsToProcess = rawData.toCharArray();

        for (char charToProcess : charsToProcess)
        {
            processReceivedChar(charToProcess);
        }
    }

    private void processReceivedChar(char receivedChar)
    {
        if (receivedChar != END_PACKET_CHAR)
        {
            addDataToRawDataPacketBuffer(receivedChar);
        }
        else
        {
            saveRawDataPacket();
        }
    }

    private boolean checkForLength_RxDataReceivedArray()
    {
        if (rawDataPackets.size() > 1)
        {
            for (RxRawDataPacket rxRawDataPacket : rawDataPackets)
            {
                for (RxRawDataPacket rawDataPacketToEq : rawDataPackets)
                {
                    if (rxRawDataPacket.getRawDataPacketValue().length() != rawDataPacketToEq.getRawDataPacketValue().length())
                    {
                        ApplicationLogger.LOGGER.warning("ERROR WITH LENGTH");
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
