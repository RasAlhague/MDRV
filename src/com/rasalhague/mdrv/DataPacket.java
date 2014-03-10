package com.rasalhague.mdrv;

import com.rasalhague.mdrv.RawDataProcessor.RawDataProcessor;

import java.util.ArrayList;
import java.util.Date;

/**
 * DataPacket means one pass for each channel. Class used for gson serializing.
 */
public class DataPacket
{
    //TODO maybe final?
    private String             rawDataPacket;
    private ArrayList<Integer> dataPacketValues;
    private long               packetCreationTimeMs;
    private int                pointsAmount;
    private boolean            isAnalyzable;
    private DeviceInfo         deviceInfo;

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

    public boolean isAnalyzable()
    {
        return isAnalyzable;
    }

    //Constructor
    public DataPacket(ArrayList<Byte> rawData, DeviceInfo deviceInfo)
    {
        this.rawDataPacket = rawData.toString();
        this.deviceInfo = deviceInfo;
        this.packetCreationTimeMs = new Date().getTime();
        this.dataPacketValues = RawDataProcessor.processData(rawData, deviceInfo);
        if (dataPacketValues != null)
        {
            this.pointsAmount = dataPacketValues.size();
            isAnalyzable = true;
        }
    }

    @Override
    public String toString()
    {
        return "DataPacket{" +
                "rawDataPacket='" + rawDataPacket + '\'' +
                ", dataPacketValues=" + dataPacketValues +
                ", deviceInfo=" + deviceInfo +
                ", packetCreationTimeMs=" + packetCreationTimeMs +
                ", pointsAmount=" + pointsAmount +
                '}';
    }
}
