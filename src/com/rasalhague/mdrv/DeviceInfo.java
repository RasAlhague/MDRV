package com.rasalhague.mdrv;

import java.util.ArrayList;

class DeviceInfo
{
    public String deviceName;
    public String devicePortName;
    public DeviceTypeEnum deviceType;

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

    @Override
    public String toString()
    {
        return "DeviceInfo{" +
                "devicePortName='" + devicePortName + '\'' +
                ", deviceType=" + deviceType +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceInfo that = (DeviceInfo) o;

        if (devicePortName != null ? !devicePortName.equals(that.devicePortName) : that.devicePortName != null)
        {
            return false;
        }
        if (deviceType != that.deviceType) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = devicePortName != null ? devicePortName.hashCode() : 0;
        result = 31 * result + (deviceType != null ? deviceType.hashCode() : 0);
        return result;
    }

    DeviceInfo(String devicePortName, DeviceTypeEnum deviceTypeEnum)
    {
        this.devicePortName = devicePortName;
        deviceType = deviceTypeEnum;
    }
}
