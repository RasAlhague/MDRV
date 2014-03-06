package com.rasalhague.mdrv;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * receiveRawData(String rawData) take a input string from device and works like a buffer for processReceivedChar(char
 * receivedChar) that parsing char by char generate a DataPacket;
 */
public class RxRawDataReceiver extends Observable
{
    private ArrayList<DataPacket> rawDataPackets;
    private String rawDataPacketBuffer = "";

    private final static char END_PACKET_CHAR_N = '\n';
    private final static char END_PACKET_CHAR_R = '\r';

    private Boolean firstPacketTrigger = true;
    private DeviceInfo deviceInfo;

    public RxRawDataReceiver(DeviceInfo deviceInfo)
    {
        this.deviceInfo = deviceInfo;
        rawDataPackets = new ArrayList<DataPacket>();
    }

    private void saveRawDataPacket()
    {
        if (firstPacketTrigger)
        {
            firstPacketTrigger = false;
        }
        else
        {
            DataPacket dataPacket = new DataPacket(rawDataPacketBuffer, deviceInfo);
            rawDataPackets.add(dataPacket);

            notifySubscribers(dataPacket, rawDataPackets);
        }

        wipeRawDataPacketBuffer();
    }

    private void notifySubscribers(DataPacket dataPacket, List rawDataPackets)
    {
        //TODO setChanged(); Wont work
        setChanged();
        notifyObservers(rawDataPackets);

        notifyDataPacketListeners(dataPacket, deviceInfo);
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
     * @param rawData
     *         string from device
     */
    public void receiveRawData(String rawData)
    {
        char[] charsToProcess = rawData.toCharArray();

        for (char charToProcess : charsToProcess)
        {
            processReceivedChar(charToProcess);
        }
    }

    private void processReceivedChar(char receivedChar)
    {
        if (receivedChar == END_PACKET_CHAR_N || receivedChar == END_PACKET_CHAR_R)
        {
            saveRawDataPacket();
        }
        else
        {
            addDataToRawDataPacketBuffer(receivedChar);
        }
    }

    //region Observer implementation (DataPacketListener)

    private List<DataPacketListener> dataPacketListeners = new ArrayList<DataPacketListener>();

    public void addListener(DataPacketListener toAdd)
    {
        dataPacketListeners.add(toAdd);
    }

    private void notifyDataPacketListeners(DataPacket dataPacket, DeviceInfo deviceInfo)
    {
        for (DataPacketListener listener : dataPacketListeners)
        {
            listener.dataPacketEvent(dataPacket);
        }
    }

    //endregion
}
