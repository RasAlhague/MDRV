package com.rasalhague.mdrv.connectionlistener;

import com.rasalhague.mdrv.DeviceInfo;

public interface DeviceConnectionListenerI
{
    public void deviceConnectionEvent(DeviceInfo connectedDevice, DeviceConnectionStateEnum deviceConnectionStateEnum);
}
