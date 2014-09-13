package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DataPacket;
import com.rasalhague.mdrv.DataPacketListener;
import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * receiveRawData(String rawData) take a input string from device and works like a buffer for processReceivedByte(char
 * receivedChar) that parsing char by char generate a DataPacket;
 */
public class RxRawDataReceiver
{
    private final ArrayList<DataPacket> rawDataPackets;
    private final ArrayList<Byte> rawDataBuffer = new ArrayList<>();
    private final DeviceInfo deviceInfo;
    private final List<DataPacketListener> dataPacketListeners = new ArrayList<>();
    private       int                      packetCounter       = 0;

    private HashMap<Integer, Integer> filterHelper = new HashMap<>();

    /**
     * Instantiates a new Rx raw data receiver.
     *
     * @param deviceInfo
     *         the device info
     */
    public RxRawDataReceiver(DeviceInfo deviceInfo)
    {
        this.deviceInfo = deviceInfo;
        rawDataPackets = new ArrayList<>();
    }

    /**
     * RawData entry point. Works like a buffer. Sends data char by char to processReceivedByte(char receivedChar);
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

    /**
     * Add listener.
     *
     * @param toAdd
     *         the to add
     */
    public void addListener(DataPacketListener toAdd)
    {
        dataPacketListeners.add(toAdd);
    }

    private void saveRawDataPacket()
    {
        if (filter())
        {
            //skip first packets coz it can be not full
            if (packetCounter > 5)
            {
                DataPacket dataPacket = new DataPacket(rawDataBuffer, deviceInfo);
                rawDataPackets.add(dataPacket);

                notifySubscribers(dataPacket, rawDataPackets);
            }
        }
        else
        {
            ApplicationLogger.LOGGER.warning("Different packets length detected!");
        }
        packetCounter++;

        wipeRawDataPacketBuffer();
    }

    private boolean filter()
    {
        int rawDataBufferSize = rawDataBuffer.size();

        if (filterHelper.containsKey(rawDataBufferSize))
        {
            filterHelper.put(rawDataBufferSize, filterHelper.get(rawDataBufferSize) + 1);
        }
        else
        {
            filterHelper.put(rawDataBufferSize, 1);
        }

        ArrayList<Integer> packetsCounts = new ArrayList<>(filterHelper.values());
        //        System.out.println(packetsCounts);
        Collections.sort(packetsCounts);
        //        System.out.println(packetsCounts);

        Integer biggestPacketCount = packetsCounts.get(packetsCounts.size() - 1);
        int biggestPacketKey = 0;
        for (Integer packetSize : filterHelper.keySet())
        {
            if (filterHelper.get(packetSize).equals(biggestPacketCount))
            {
                biggestPacketKey = packetSize;
            }
        }
        //        System.out.println(filterHelper);
        //        System.out.println(biggestPacketKey);
        //        System.out.println(rawDataBufferSize);
        //        System.out.println(filterHelper.get(biggestPacketKey));

        return biggestPacketKey == rawDataBufferSize;
    }

    private void notifySubscribers(DataPacket dataPacket, ArrayList<DataPacket> rawDataPackets)
    {
        notifyDataPacketListeners(dataPacket, rawDataPackets);
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

    private void processReceivedByte(byte receivedByte)
    {
        addDataToRawDataPacketBuffer(receivedByte);

        /**
         * Check last bytes in rawDataBuffer with last bytes in deviceInfo.getEndPacketSequence
         */
        byte[] endPacketSequence = deviceInfo.getEndPacketSequence();
        if (rawDataBuffer.size() > endPacketSequence.length)
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
                //Transfer endPacketSequence to the start of packet
                List<Byte> subList = rawDataBuffer.subList(rawDataBuffer.size() - endPacketSequence.length,
                                                           rawDataBuffer.size());
                List<Byte> tempList = new ArrayList<>(subList);
                subList.clear();

                saveRawDataPacket();

                rawDataBuffer.addAll(tempList);
            }
        }
    }

    private void notifyDataPacketListeners(DataPacket dataPacket, ArrayList<DataPacket> rawDataPackets)
    {
        for (DataPacketListener listener : dataPacketListeners)
        {
            listener.dataPacketEvent(dataPacket);
        }
    }
}
