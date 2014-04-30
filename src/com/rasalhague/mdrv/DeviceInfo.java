package com.rasalhague.mdrv;

import com.codeminders.hidapi.HIDDeviceInfo;
import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.configuration.ConfigurationHolder;
import com.rasalhague.mdrv.configuration.ConfigurationLoader;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
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
    private String     vendorID;
    private String     productID;
    private String     name;
    private String     devicePortName;
    private DeviceType deviceType;
    private byte[]     endPacketSequence;
    private float      initialFrequency;
    private float      channelSpacing;

    public enum DeviceType
    {
        HID,
        COM,
        WIRELESS_ADAPTER
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

    public DeviceInfo(String devPortName)
    {
        devicePortName = devPortName;
        deviceType = DeviceType.COM;

        HashMap<String, String> devInfMap = takeCOMDeviceInformation(devicePortName);
        name = devInfMap.get("devName");
        productID = devInfMap.get("pid").toUpperCase();
        vendorID = devInfMap.get("vid").toUpperCase();

        setSomeFieldsFromConfig(productID, vendorID);
    }

    public DeviceInfo(String netName, DeviceType wirelessAdapter)
    {
        devicePortName = netName;
        deviceType = wirelessAdapter;

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

    private HashMap<String, String> takeCOMDeviceInformation(String devicePortName)
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            return Utils.searchRegistry("HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Enum\\USB", devicePortName);
        }
        else if (SystemUtils.IS_OS_LINUX)
        {
            String[] request = new String[]{"dmesg", "grep -i usb"};
            String output = "";
            try
            {
                Process process = Runtime.getRuntime().exec(request);
                Utils.StreamReader reader = new Utils.StreamReader(process.getInputStream());
                reader.start();
                process.waitFor();
                reader.join();
                output = reader.getResult();
            }
            catch (IOException | InterruptedException e)
            {
                ApplicationLogger.LOGGER.severe(e.getMessage());
                e.printStackTrace();
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
