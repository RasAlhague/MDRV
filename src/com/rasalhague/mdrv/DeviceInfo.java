package com.rasalhague.mdrv;

import com.codeminders.hidapi.HIDDeviceInfo;
import com.rasalhague.mdrv.configuration.ConfigurationHolder;
import com.rasalhague.mdrv.configuration.ConfigurationLoader;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import org.apache.commons.lang3.SystemUtils;

import java.util.Arrays;
import java.util.HashMap;

/**
 * productID Must be Hex value exclude 0x. Example: 0241 vendorID the same as productID
 */
public class DeviceInfo
{
    private final String     vendorID;
    private final String     productID;
    private final String     name;
    private final String     devicePortName;
    private final DeviceType deviceType;
    private byte[] endPacketSequence;
    private float  initialFrequency;
    private float  channelSpacing;

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
        productID = Integer.toString(hidDeviceInfo.getProduct_id(), 16).toUpperCase();
        vendorID = Integer.toString(hidDeviceInfo.getVendor_id(), 16).toUpperCase();

        setSomeFieldsFromConfig(productID, vendorID);
    }

    DeviceInfo(String devPortName, DeviceType devTypeEnum)
    {
        devicePortName = devPortName;
        deviceType = DeviceType.COM;

        HashMap<String, String> devInfMap = takeCOMDeviceInformation();
        name = devInfMap.get("devName");
        productID = devInfMap.get("pid");
        vendorID = devInfMap.get("vid");

        setSomeFieldsFromConfig(productID, vendorID);
    }

    private void setSomeFieldsFromConfig(String pID, String vID)
    {
        ConfigurationHolder configuration = ConfigurationLoader.getConfiguration();
        ConfigurationHolder.DeviceConfigurationHolder deviceConfiguration = configuration.getDeviceConfiguration(pID,
                                                                                                                 vID);

        endPacketSequence = deviceConfiguration.getEndPacketSequence();
        initialFrequency = deviceConfiguration.getInitialFrequency();
        channelSpacing = deviceConfiguration.getChannelSpacing();

        //        System.out.println(pID + "\t" + initialFrequency + "\t" + channelSpacing);
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

    public float getInitialFrequency()
    {
        return initialFrequency;
    }

    public float getChannelSpacing()
    {
        return channelSpacing;
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

    private HashMap<String, String> takeCOMDeviceInformation()
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            return Utils.searchRegistry("HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Enum\\USB", getDevicePortName());
        }
        else if (SystemUtils.IS_OS_LINUX)
        {
            //TODO IS_OS_LINUX get device names impl
        }

        ApplicationLogger.LOGGER.severe("OS does not support");

        return null;
    }
}
