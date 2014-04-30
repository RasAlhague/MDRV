package com.rasalhague.mdrv.connectionlistener;

import com.rasalhague.mdrv.DeviceInfo;

interface DeviceConnectionListenerI
{
    public void deviceConnectionEvent(DeviceInfo connectedDevice, DeviceConnectionStateEnum deviceConnectionStateEnum);
}
