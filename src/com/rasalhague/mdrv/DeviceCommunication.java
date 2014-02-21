package com.rasalhague.mdrv;

public abstract class DeviceCommunication implements Runnable
{
    DeviceInfo deviceInfo;

    DeviceCommunication(DeviceInfo deviceInfo)
    {
        this.deviceInfo = deviceInfo;
    }

}