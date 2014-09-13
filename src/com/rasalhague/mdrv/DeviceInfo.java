package com.rasalhague.mdrv;

import com.codeminders.hidapi.HIDDeviceInfo;
import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.configuration.ConfigurationHolder;
import com.rasalhague.mdrv.configuration.ConfigurationLoader;
import com.rasalhague.mdrv.devices.Device;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import org.apache.commons.lang3.SystemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * productID Must be Hex value exclude 0x. Example: 0241 vendorID the same as productID
 */
public class DeviceInfo
{
    private String friendlyName;
    private String     vendorID;
    private String     productID;
    private String     name;
    private String portName;
    private DeviceType deviceType;
    private byte[]     endPacketSequence;
    private float      initialFrequency;
    private float      channelSpacing;

    private Device device;

    public enum DeviceType
    {
        HID,
        COM,
        WIRELESS_ADAPTER,
        DUMMY
    }

    public DeviceInfo(HIDDeviceInfo hidDeviceInfo)
    {
        portName = hidDeviceInfo.getPath();
        deviceType = DeviceType.HID;

        name = hidDeviceInfo.getProduct_string();
        productID = Integer.toString(hidDeviceInfo.getProduct_id(), 16).toUpperCase();
        vendorID = Integer.toString(hidDeviceInfo.getVendor_id(), 16).toUpperCase();

        //        setSomeFieldsFromConfig(productID, vendorID);
    }

    public DeviceInfo(String devPortName)
    {
        portName = devPortName;
        deviceType = DeviceType.COM;

        HashMap<String, String> devInfMap = takeCOMDeviceInformation(portName);
        name = devInfMap.get("devName");
        productID = devInfMap.get("pid").toUpperCase();
        vendorID = devInfMap.get("vid").toUpperCase();

        //        setSomeFieldsFromConfig(productID, vendorID);
    }

    public DeviceInfo(String netName, DeviceType wirelessAdapter)
    {
        portName = netName;
        deviceType = wirelessAdapter;

        //TODO del inxi
        ArrayList<String> inxi = Utils.runShellScript("inxi -n -c 0 -Z");
        Matcher matcher = Pattern.compile(
                "Card-\\d: (?<netCardName>.*?) d.*?IF: (?<netName>.*?) s.*?mac: (?<netCardMac>(..:?){6})")
                                 .matcher(inxi.toString());

        while (matcher.find())
        {
            if (matcher.group("netName").equals(netName))
            {
                name = matcher.group("netCardName");
                productID = matcher.group("netCardName").toUpperCase();
                vendorID = productID;

                break;
            }
        }
    }

    public DeviceInfo(String vendorID,
                      String productID,
                      String name, String portName,
                      DeviceType deviceType,
                      byte[] endPacketSequence,
                      float initialFrequency,
                      float channelSpacing)
    {
        this.vendorID = vendorID;
        this.productID = productID;
        this.name = name;
        this.portName = portName;
        this.deviceType = deviceType;
        this.endPacketSequence = endPacketSequence;
        this.initialFrequency = initialFrequency;
        this.channelSpacing = channelSpacing;
    }

    public void setSomeFields(String friendlyName,
                              byte[] endPacketSequence,
                              float initialFrequency,
                              float channelSpacing,
                              Device device)
    {
        this.friendlyName = friendlyName;
        this.endPacketSequence = endPacketSequence;
        this.initialFrequency = initialFrequency;
        this.channelSpacing = channelSpacing;
        this.device = device;
    }

    //TODO Dedicated
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

    public String getPortName()
    {
        return portName;
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

    public String getFriendlyName()
    {
        return friendlyName;
    }

    public Device getDevice()
    {
        return device;
    }

    public void setChannelSpacing(float channelSpacing)
    {
        this.channelSpacing = channelSpacing;
    }

    public boolean equalsPidVid(String pId, String vId)
    {
        return pId.equals(productID) && vId.equals(vendorID);
    }

    @Override
    public String toString()
    {
        return "DeviceInfo{" +
                "friendlyName='" + friendlyName + '\'' +
                ", vendorID='" + vendorID + '\'' +
                ", productID='" + productID + '\'' +
                ", name='" + name + '\'' +
                ", portName='" + portName + '\'' +
                ", deviceType=" + deviceType +
                ", endPacketSequence=" + Arrays.toString(endPacketSequence) +
                ", initialFrequency=" + initialFrequency +
                ", channelSpacing=" + channelSpacing +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceInfo that = (DeviceInfo) o;

        if (!portName.equals(that.portName)) return false;
        if (!productID.equals(that.productID)) return false;
        if (!vendorID.equals(that.vendorID)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = vendorID.hashCode();
        result = 31 * result + productID.hashCode();
        result = 31 * result + portName.hashCode();
        return result;
    }

    private HashMap<String, String> takeCOMDeviceInformation(String devicePortName)
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            return Utils.searchRegistry("HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Enum\\USB", devicePortName);
        }
        else if (SystemUtils.IS_OS_LINUX)
        {
            String output = "";

            ArrayList<String> strings = Utils.runShellScript("dmesg | grep -i usb");
            for (String string : strings)
            {
                output += string + "\n";
            }

            Pattern pattern = Pattern.compile(
                    "New USB device found.*?idVendor=(?<vid>.{4}), idProduct=(?<pid>.{4}).*?\\n.*?\\n.*?Product: (?<devName>.*)((\\n.*?){0,6}(?<portName>tty.*):)?");

            Matcher matcher = pattern.matcher(output);
            HashMap<String, String> devInfMap = new HashMap<>();
            while (matcher.find())
            {
                String portName = matcher.group("portName");
                if (portName != null && devicePortName.contains(portName))
                {
                    devInfMap.put("vid", matcher.group("vid").trim());
                    devInfMap.put("pid", matcher.group("pid").trim());
                    devInfMap.put("devName", matcher.group("devName").trim());
                }
            }

            return devInfMap;
        }

        ApplicationLogger.LOGGER.severe("OS does not support");

        return null;
    }
}
