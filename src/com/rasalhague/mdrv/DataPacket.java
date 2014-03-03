package com.rasalhague.mdrv;

import com.rasalhague.mdrv.RawDataProcessor.RawDataProcessor;

import java.util.ArrayList;
import java.util.Date;

/**
 * Class used for gson serializing Contain data about received data that encapsulate in packet
 */
public class DataPacket
{
    private String             rawDataPacket;
    private ArrayList<Integer> dataPacketValues;
    private DeviceInfo         deviceInfo;
    private long               packetCreationTimeMs;
    private int                packetLength;
    private int                pointsAmount;

    //Getter
    public String getRawDataPacketValue()
    {
        return rawDataPacket;
    }

    public ArrayList<Integer> getDataPacketValues()
    {
        return dataPacketValues;
    }

    public DeviceInfo getDeviceInfo()
    {
        return deviceInfo;
    }

    //Constructor
    public DataPacket(String rawData, DeviceInfo deviceInfo)
    {
        this.rawDataPacket = rawData;
        this.deviceInfo = deviceInfo;
        this.packetCreationTimeMs = new Date().getTime();
        this.packetLength = rawData.length();
        this.dataPacketValues = RawDataProcessor.processData(rawData, deviceInfo);
        this.pointsAmount = dataPacketValues.size();
    }

    @Override
    public String toString()
    {
        return "DataPacket{" +
                "rawDataPacket='" + rawDataPacket + '\'' +
                ", dataPacketValues=" + dataPacketValues +
                ", deviceInfo=" + deviceInfo +
                ", packetCreationTimeMs=" + packetCreationTimeMs +
                ", packetLength=" + packetLength +
                ", pointsAmount=" + pointsAmount +
                '}';
    }
}
