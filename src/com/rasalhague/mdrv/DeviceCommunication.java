package com.rasalhague.mdrv;

public abstract class DeviceCommunication implements Runnable
{
    DeviceInfo        deviceInfo;
    RxRawDataReceiver rxRawDataReceiver;

    DeviceCommunication(DeviceInfo devInfo)
    {
        deviceInfo = devInfo;
        rxRawDataReceiver = new RxRawDataReceiver(deviceInfo);
    }

    //Factory method
    public static DeviceCommunication getInstance(DeviceInfo deviceInfo)
    {
        String devPid = deviceInfo.getDevicePid();
        //AirView2 -- USB\VID_1F9B&PID_0241&REV_8888
        if (devPid.equals("0241"))
        {
            return new AirView2(deviceInfo);
        }

        //eZ430 -- USB\VID_0451&PID_F432&MI_00
        if (devPid.equals("F432"))
        {
            return new ez430RF2500(deviceInfo);
        }

        ApplicationLogger.warning("Device not specified. Getting COMDeviceCommunication to try to work with device.");
        return new COMDeviceCommunication(deviceInfo);
    }

    abstract void initializeDevice();
}