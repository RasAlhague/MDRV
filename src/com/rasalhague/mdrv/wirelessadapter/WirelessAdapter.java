package com.rasalhague.mdrv.wirelessadapter;

import com.rasalhague.mdrv.Utility.Utils;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WirelessAdapter
{
    private String networkName;
    private String macAddress;
    private String adapterName;
    private BidiMap<Byte, Short> channelToFrequencyMap = new TreeBidiMap<>();
    private RoundVar channelRoundSwitcher;

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

    public BidiMap<Byte, Short> getChannelToFrequencyMap()
    {
        return channelToFrequencyMap;
    }

    public RoundVar getChannelRoundSwitcher()
    {
        return channelRoundSwitcher;
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

        setUpChannelToFrequencyMap();

        TreeMap<Byte, Short> byteShortTreeMap = new TreeMap<>(getChannelToFrequencyMap());
        Byte firstKey = byteShortTreeMap.firstKey();
        Byte lastKey = byteShortTreeMap.lastKey();
        channelRoundSwitcher = new RoundVar(firstKey, lastKey);
    }

    private void setUpChannelToFrequencyMap()
    {
        ArrayList<String> iwlistChannelOut = Utils.runShellScript("iwlist " +
                                                                          networkName +
                                                                          " channel");

        Matcher matcher = Pattern.compile("Channel (?<channelNumber>\\d{2}) : (?<ChannelFrequency>\\d\\.\\d{3})")
                                 .matcher(iwlistChannelOut.toString());

        while (matcher.find())
        {
            short channelFrequency = (short) (Float.valueOf(matcher.group("ChannelFrequency")) * 1000);
            byte channelNumber = Byte.valueOf(matcher.group("channelNumber"));

            channelToFrequencyMap.put(channelNumber, channelFrequency);
        }
    }

    public int nextChannel()
    {
        String channelSwitchingCommand = "iwconfig " +
                getNetworkName() +
                " channel " +
                channelRoundSwitcher.nextValue();

        Utils.runShellScript(channelSwitchingCommand);

        return channelRoundSwitcher.getCurrentValue();
    }
}
