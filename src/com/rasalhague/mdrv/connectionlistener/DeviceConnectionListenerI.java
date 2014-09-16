package com.rasalhague.mdrv.connectionlistener;

import com.rasalhague.mdrv.device.core.DeviceInfo;

/**
 * The interface Device connection listener.
 */
public interface DeviceConnectionListenerI
{
    /**
     * Device connection event.
     *
     * @param connectedDevice
     *         the connected device
     * @param deviceConnectionStateEnum
     *         the device connection state enum
     */
    public void deviceConnectionEvent(DeviceInfo connectedDevice, DeviceConnectionStateEnum deviceConnectionStateEnum);
}
