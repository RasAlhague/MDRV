package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;

/**
 * Result of the run method must be call to RxRawDataReceiver
 */
public abstract class DeviceCommunication implements Runnable
{
    DeviceInfo        deviceInfo;
    RxRawDataReceiver rxRawDataReceiver;

    DeviceCommunication(DeviceInfo devInfo)
    {
        deviceInfo = devInfo;
        rxRawDataReceiver = new RxRawDataReceiver(deviceInfo);
    }

    public RxRawDataReceiver getRxRawDataReceiver()
    {
        return rxRawDataReceiver;
    }

    /**
     * Factory method. Choose device. TODO config file with init sequence and etc.
     */
    public static DeviceCommunication getInstance(DeviceInfo deviceInfo)
    {
        String AirView2PID = "0241";
        String eZ430PID = "F432";
        String ISMSnifferPID = "2001";

        String devPid = deviceInfo.getDevicePid();

        if (deviceInfo.getDeviceType() == DeviceInfo.DeviceType.COM)
        {
            //AirView2 -- USB\VID_1F9B&PID_0241&REV_8888
            if (devPid.equals(AirView2PID))
            {
                return new AirView2(deviceInfo);
            }

            //eZ430 -- USB\VID_0451&PID_F432&MI_00
            if (devPid.equals(eZ430PID))
            {
                return new ez430RF2500(deviceInfo);
            }

            ApplicationLogger.LOGGER.warning(
                    "Device not specified. Getting COMDeviceCommunication to try to work with COM device.");

            return new COMDeviceCommunication(deviceInfo);
        }
        else if (deviceInfo.getDeviceType() == DeviceInfo.DeviceType.HID)
        {
            if (devPid.equals(ISMSnifferPID))
            {
                return new ISMSniffer(deviceInfo);
            }

            //            ApplicationLogger.LOGGER.warning(
            //                    "Device not specified. Getting HIDDeviceCommunication to try to work with HID device.");
            //
            //            return new HIDDeviceCommunication(deviceInfo);
        }

        return null;
    }

    abstract void initializeDevice();
}