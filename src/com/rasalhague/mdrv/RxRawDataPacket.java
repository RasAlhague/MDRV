package com.rasalhague.mdrv;

import java.util.Date;

/**
 * Class used for gson serializing
 * Contain data about received data that encapsulate in packet
 */
public class RxRawDataPacket
{
    private String rawDataPacket;
    private DeviceInfo deviceInfo;
    private Date packetCreationDate;
    private long packetCreationTimeMs;
    private int packetLength;
    private int pointsAmount;

    //Getter
    public String getRawDataPacketValue()
    {
        return rawDataPacket;
    }

    //Constructor
    public RxRawDataPacket(String rawData, DeviceInfo deviceInfo)
    {
        this.rawDataPacket = rawData;
        this.deviceInfo = deviceInfo;

        this.packetCreationDate = new Date();
        this.packetCreationTimeMs = this.packetCreationDate.getTime();
        this.packetLength = rawData.length();

        //TODO device switching
        if (deviceInfo.deviceType == DeviceInfo.DeviceTypeEnum.COM)
        {
            char[] chars = rawData.toCharArray();
            int cCount = 0;
            for (char aChar : chars)
            {
                if (aChar == '-')
                {
                    cCount++;
                }
            }
            pointsAmount = cCount;
        }

    }

    @Override
    public String toString()
    {
        return "RxRawDataPacket{" +
                "rawDataPacket='" + rawDataPacket + '\'' +
                '}' + "\n";
    }
}
