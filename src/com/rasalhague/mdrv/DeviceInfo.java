package com.rasalhague.mdrv;

import com.codeminders.hidapi.HIDDeviceInfo;
import com.rasalhague.mdrv.constants.DeviceConstants;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import org.apache.commons.lang3.SystemUtils;

import java.util.HashMap;

/**
 * devicePid Must be Hex value exclude 0x. Example: 0241 deviceVid the same as devicePid
 */
public class DeviceInfo
{
    //TODO переделать ПИД ВИД в числа, что бы нивилировать чувствительность к геристру
    private final String     deviceVid;
    private final String     devicePid;
    private final String     deviceName;
    private final String     devicePortName;
    private final DeviceType deviceType;
    private final byte[]     endPacketSequence;

    public enum DeviceType
    {
        HID,
        COM
    }

    public DeviceInfo(HIDDeviceInfo hidDeviceInfo)
    {
        devicePortName = hidDeviceInfo.getPath();
        deviceType = DeviceType.HID;

        deviceName = hidDeviceInfo.getProduct_string();
        devicePid = Integer.toHexString(hidDeviceInfo.getProduct_id()).toUpperCase();
        deviceVid = Integer.toHexString(hidDeviceInfo.getVendor_id()).toUpperCase();

        //TODO Hardcoded
        if (devicePid.equals(DeviceConstants.UnigenISMSniffer.PID))
        {
            endPacketSequence = DeviceConstants.UnigenISMSniffer.END_PACKET_SEQUENCE;
            //            System.out.println("endPacketSequence");
        }
        else
        {
            endPacketSequence = null;
        }
    }

    DeviceInfo(String devPortName, DeviceType devTypeEnum)
    {
        devicePortName = devPortName;
        deviceType = devTypeEnum;

        HashMap<String, String> devInfMap = takeDeviceName();
        deviceName = devInfMap.get("devName");
        devicePid = devInfMap.get("pid").toUpperCase();
        deviceVid = devInfMap.get("vid").toUpperCase();

        //TODO Hardcoded
        if (devicePid.equals(DeviceConstants.AirView2.PID))
        {
            endPacketSequence = DeviceConstants.AirView2.END_PACKET_SEQUENCE;
        }
        else if (devicePid.equals(DeviceConstants.ez430RF2500.PID))
        {
            endPacketSequence = DeviceConstants.ez430RF2500.END_PACKET_SEQUENCE;
        }
        else
        {
            endPacketSequence = null;
        }
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

    public DeviceType getDeviceType()
    {
        return deviceType;
    }

    public byte[] getEndPacketSequence()
    {
        return endPacketSequence;
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

        ApplicationLogger.LOGGER.severe("OS does not support");

        return null;
    }
}
