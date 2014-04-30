package com.rasalhague.mdrv.wirelessadapter;

import com.rasalhague.mdrv.Utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WirelessAdapter
{
    private String networkName;
    private String macAddress;
    private String adapterName;

    public String getNetworkName()
    {
        return networkName;
    }

    public String getMacAddress()
    {
        return macAddress;
    }

    public String getAdapterName()
    {
        return adapterName;
    }

    @Override
    public String toString()
    {
        return networkName + " - " + adapterName;
    }

    public WirelessAdapter(String netName)
    {
        ArrayList<String> inxi = Utils.runShellScript("inxi -n -c 0 -Z");
        Matcher inxiMatcher = Pattern.compile(
                "Card-\\d: (?<netCardName>.*?) d.*?IF: (?<netName>.*?) s.*?mac: (?<netCardMac>(..:?){6})")
                                     .matcher(inxi.toString());

        while (inxiMatcher.find())
        {
            if (inxiMatcher.group("netName").equals(netName))
            {
                networkName = netName;
                macAddress = inxiMatcher.group("netCardMac");
                adapterName = inxiMatcher.group("netCardName");

                break;
            }
        }
    }
}
