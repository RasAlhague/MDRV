package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;

/**
 * Result of the run method must be call to RxRawDataReceiver
 */
public abstract class DeviceCommunication implements Runnable
{
    final DeviceInfo        deviceInfo;
    final RxRawDataReceiver rxRawDataReceiver;

    /**
     * Instantiates a new Device communication.
     *
     * @param devInfo
     *         the dev info
     */
    DeviceCommunication(DeviceInfo devInfo)
    {
        deviceInfo = devInfo;
        rxRawDataReceiver = new RxRawDataReceiver(deviceInfo);
    }

    /**
     * Gets rx raw data receiver.
     *
     * @return the rx raw data receiver
     */
    public RxRawDataReceiver getRxRawDataReceiver()
    {
        return rxRawDataReceiver;
    }

    /**
     * Factory method. Choose device.
     *
     * @param deviceInfo
     *         the device info
     *
     * @param initializationMethod
     *@param parseMethod @return the instance
     */
    public static DeviceCommunication getInstance(DeviceInfo deviceInfo)
    {
        if (deviceInfo.getDeviceType() == DeviceInfo.DeviceType.COM)
        {
            return new COMDeviceCommunication(deviceInfo);
        }
        if (deviceInfo.getDeviceType() == DeviceInfo.DeviceType.HID)
        {
            return new HIDDeviceCommunication(deviceInfo);
        }
        if (deviceInfo.getDeviceType() == DeviceInfo.DeviceType.DUMMY)
        {
            return new DummyDeviceCommunication(deviceInfo);
        }

        return null;
    }

    /**
     * Initialize device.
     */
    void initializeDevice()
    {
        deviceInfo.getDevice().initializeDevice();

        ApplicationLogger.LOGGER.warning(deviceInfo.getName() + " has initialized.");
    }
}