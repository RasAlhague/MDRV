package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DataPacket;
import com.rasalhague.mdrv.DataPacketListener;
import com.rasalhague.mdrv.DeviceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * receiveRawData(String rawData) take a input string from device and works like a buffer for processReceivedByte(char
 * receivedChar) that parsing char by char generate a DataPacket;
 */
public class RxRawDataReceiver extends Observable
{
    private ArrayList<DataPacket> rawDataPackets;
    private ArrayList<Byte> rawDataBuffer = new ArrayList<Byte>();

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
            DataPacket dataPacket = new DataPacket(rawDataBuffer, deviceInfo);
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

    private void addDataToRawDataPacketBuffer(byte data)
    {
        rawDataBuffer.add(data);
    }

    private void wipeRawDataPacketBuffer()
    {
        //        rawDataBuffer = "";
        rawDataBuffer.clear();
    }

    /**
     * Works like a buffer. Sends data char by char to processReceivedByte(char receivedChar);
     *
     * @param rawData
     *         string from device
     */
    public void receiveRawData(byte[] rawData)
    {
        for (byte byteProcess : rawData)
        {
            processReceivedByte(byteProcess);
        }
    }

    private void processReceivedByte(byte receivedByte)
    {
        addDataToRawDataPacketBuffer(receivedByte);

        /**
         * Check last bytes in rawDataBuffer with last bytes in deviceInfo.getEndPacketSequence
         */
        byte[] endPacketSequence = deviceInfo.getEndPacketSequence();
        if (rawDataBuffer.size() >= endPacketSequence.length)
        {
            boolean needToSave = true;

            for (int i = 0; i < endPacketSequence.length; i++)
            {
                byte b = endPacketSequence[i];

                if (rawDataBuffer.get(rawDataBuffer.size() - endPacketSequence.length + i) != b)
                {
                    needToSave = false;
                    break;
                }
            }

            if (needToSave)
            {
                saveRawDataPacket();
            }
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
