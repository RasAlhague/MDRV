package com.rasalhague.mdrv;

import com.codeminders.hidapi.HIDDeviceInfo;
import com.rasalhague.mdrv.constants.DeviceConstants;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import org.apache.commons.lang3.SystemUtils;

import java.util.Arrays;
import java.util.HashMap;

/**
 * productID Must be Hex value exclude 0x. Example: 0241 vendorID the same as productID
 */
public class DeviceInfo
{
    //TODO переделать ПИД ВИД в числа, что бы нивилировать чувствительность к геристру
    private final String     vendorID;
    private final String     productID;
    private final String     name;
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

        name = hidDeviceInfo.getProduct_string();
        productID = Integer.toHexString(hidDeviceInfo.getProduct_id()).toUpperCase();
        vendorID = Integer.toHexString(hidDeviceInfo.getVendor_id()).toUpperCase();

        //TODO Hardcoded
        if (productID.equals(DeviceConstants.UnigenISMSniffer.PID))
        {
            endPacketSequence = DeviceConstants.UnigenISMSniffer.END_PACKET_SEQUENCE;
            //            System.out.println("endPacketSequence");
        }
        else if (productID.equals(DeviceConstants.MetaGeek_WiSpy24x2.PID))
        {
            endPacketSequence = DeviceConstants.MetaGeek_WiSpy24x2.END_PACKET_SEQUENCE;
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
        name = devInfMap.get("devName");
        productID = devInfMap.get("pid").toUpperCase();
        vendorID = devInfMap.get("vid").toUpperCase();

        //TODO Hardcoded
        if (productID.equals(DeviceConstants.AirView2.PID))
        {
            endPacketSequence = DeviceConstants.AirView2.END_PACKET_SEQUENCE;
        }
        else if (productID.equals(DeviceConstants.ez430RF2500.PID))
        {
            endPacketSequence = DeviceConstants.ez430RF2500.END_PACKET_SEQUENCE;
        }
        else
        {
            endPacketSequence = null;
        }
    }

    public String getVendorID()
    {
        return vendorID;
    }

    public String getProductID()
    {
        return productID;
    }

    public String getName()
    {
        return name;
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
        return pId.equals(productID) && vId.equals(vendorID);
    }

    @Override
    public String toString()
    {
        return "DeviceInfo{" +
                "vendorID='" + vendorID + '\'' +
                ", productID='" + productID + '\'' +
                ", name='" + name + '\'' +
                ", devicePortName='" + devicePortName + '\'' +
                ", deviceType=" + deviceType +
                ", endPacketSequence=" + Arrays.toString(endPacketSequence) +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceInfo that = (DeviceInfo) o;

        if (!productID.equals(that.productID)) return false;
        if (!vendorID.equals(that.vendorID)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = vendorID.hashCode();
        result = 31 * result + productID.hashCode();
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
