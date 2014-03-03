package com.rasalhague.mdrv;

import com.rasalhague.mdrv.logging.ApplicationLogger;
import org.apache.commons.lang3.SystemUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class DeviceInfo
{
    private final String         deviceVid;
    private final String         devicePid;
    private final String         deviceName;
    private final String         devicePortName;
    private final DeviceTypeEnum deviceType;

    public static ArrayList<DeviceInfo> createArrayListFromNames(String[] portNames, DeviceTypeEnum deviceTypeEnum)
    {
        ArrayList<DeviceInfo> deviceInfoList = new ArrayList<DeviceInfo>();

        for (String portName : portNames)
        {
            deviceInfoList.add(new DeviceInfo(portName, deviceTypeEnum));
        }

        return deviceInfoList;
    }

    enum DeviceTypeEnum
    {
        USB, COM
    }

    public String getDeviceVid()
    {
        return deviceVid;
    }

    public String getDevicePid()
    {
        return devicePid;
    }

    public String getDeviceName()
    {
        return deviceName;
    }

    public String getDevicePortName()
    {
        return devicePortName;
    }

    public DeviceTypeEnum getDeviceType()
    {
        return deviceType;
    }

    public boolean equalsPidVid(String pId, String vId)
    {
        return pId.equals(devicePid) && vId.equals(deviceVid);
    }

    @Override
    public String toString()
    {
        return "DeviceInfo{" +
                "deviceVid='" + getDeviceVid() + '\'' +
                ", devicePid='" + getDevicePid() + '\'' +
                ", deviceName='" + getDeviceName() + '\'' +
                ", devicePortName='" + getDevicePortName() + '\'' +
                ", deviceType=" + getDeviceType() +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceInfo that = (DeviceInfo) o;

        if (!devicePid.equals(that.devicePid)) return false;
        if (!deviceVid.equals(that.deviceVid)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = deviceVid.hashCode();
        result = 31 * result + devicePid.hashCode();
        return result;
    }

    DeviceInfo(String devPortName, DeviceTypeEnum devTypeEnum)
    {
        devicePortName = devPortName;
        deviceType = devTypeEnum;

        HashMap<String, String> devInfMap = takeDeviceName();
        deviceName = devInfMap.get("devName");
        devicePid = devInfMap.get("pid");
        deviceVid = devInfMap.get("vid");
    }

    private HashMap<String, String> takeDeviceName()
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            return Utils.getDeviceNameFromWinRegistry(getDevicePortName());
        }
        else if (SystemUtils.IS_OS_LINUX)
        {
            //TODO IS_OS_LINUX get device names impl
        }

        ApplicationLogger.severe("OS does not support");

        return null;
    }
}
