package com.rasalhague.mdrv;

import java.util.Date;

public class RxRawDataPacket
{
    private String rawDataPacket;
    private DeviceInfo deviceInfo;
    private Date packetCreationDate;
    private long packetCreationTimeMs;

    //Getter
    public String getRawDataPacketValue()
    {
        return rawDataPacket;
    }

    //Constructor
    public RxRawDataPacket(String rawDataPacket, DeviceInfo deviceInfo)
    {
        this.rawDataPacket = rawDataPacket;
        this.deviceInfo = deviceInfo;

        this.packetCreationDate = new Date();
        this.packetCreationTimeMs = this.packetCreationDate.getTime();
    }

    @Override
    public String toString()
    {
        return "RxRawDataPacket{" +
                "rawDataPacket='" + rawDataPacket + '\'' +
                '}' + "\n";
    }
}
