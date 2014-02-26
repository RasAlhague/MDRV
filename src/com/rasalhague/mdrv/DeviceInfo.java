package com.rasalhague.mdrv;

import org.apache.commons.lang3.SystemUtils;

import java.util.ArrayList;
import java.util.HashMap;

class DeviceInfo
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

        if (!getDeviceName().equals(that.getDeviceName())) return false;
        if (!getDevicePid().equals(that.getDevicePid())) return false;
        if (!getDevicePortName().equals(that.getDevicePortName())) return false;
        if (getDeviceType() != that.getDeviceType()) return false;
        if (!getDeviceVid().equals(that.getDeviceVid())) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = getDeviceVid().hashCode();
        result = 31 * result + getDevicePid().hashCode();
        result = 31 * result + getDeviceName().hashCode();
        result = 31 * result + getDevicePortName().hashCode();
        result = 31 * result + getDeviceType().hashCode();
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
