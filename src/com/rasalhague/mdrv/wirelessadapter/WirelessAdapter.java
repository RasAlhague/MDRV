package com.rasalhague.mdrv.wirelessadapter;

import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.configuration.ConfigurationLoader;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WirelessAdapter
{
    private String associatedName;
    private String macAddress;
    private String busAddress;
    private String adapterName;
    private final BidiMap<Byte, Short> channelToFrequencyMap = new TreeBidiMap<>();
    private final RoundVar channelRoundSwitcher;

    public String getAssociatedName()
    {
        return associatedName;
    }

    public String getMacAddress()
    {
        return macAddress;
    }

    public String getAdapterName()
    {
        return adapterName;
    }

    public String getBusAddress()
    {
        return busAddress;
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
        return associatedName + " - " + adapterName;
    }

    public WirelessAdapter(String netName)
    {
        //        ArrayList<String> inxi = Utils.runShellScript("inxi -n -c 0 -Z");
        //        Matcher inxiMatcher = Pattern.compile(
        //                "Card-\\d: (?<netCardName>.*?) d.*?IF: (?<netName>.*?) s.*?mac: (?<netCardMac>(..:?){6})")
        //                                     .matcher(inxi.toString());
        //
        //        while (inxiMatcher.find())
        //        {
        //            if (inxiMatcher.group("netName").equals(netName))
        //            {
        //                associatedName = netName;
        //                macAddress = inxiMatcher.group("netCardMac");
        //                adapterName = inxiMatcher.group("netCardName");
        //
        //                break;
        //            }
        //        }

        associatedName = netName;

        ArrayList<String> lsOut = Utils.runShellScript("ls -l /sys/class/net/ | grep " + netName);
        //lrwxrwxrwx 1 root root 0 тра 27 19:09 wlan0 -> ../../devices/pci0000:00/0000:00:1c.6/0000:07:00.0/net/wlan0

        Matcher matcher = Pattern.compile("(?<devBus>(\\d){1,5}:(\\d){1,5}:(\\d){1,5}.(\\d){1,5})")
                                 .matcher(lsOut.toString());
        String devBus = null;
        boolean isUsb = lsOut.toString().contains("usb");

        while (matcher.find())
        {
            devBus = matcher.group("devBus");
        }

        if (devBus != null)
        {
            //TODO Usb wireless adapter
            ArrayList<String> out = new ArrayList<>();
            if (isUsb)
            {
                //                out = Utils.runShellScript("lsusb -s " + devBus);

                adapterName = "USB that was connected on " + devBus;
            }
            else
            {
                out = Utils.runShellScript("lspci -s " + devBus);
                //07:00.0 Network controller: Qualcomm Atheros AR9485 Wireless Network Adapter (rev 01)

                matcher = Pattern.compile("(.*: (?<netCardName>.*))").matcher(out.toString());

                while (matcher.find())
                {
                    adapterName = matcher.group("netCardName");
                }
            }

            busAddress = devBus;
        }

        setUpChannelToFrequencyMap();

        channelRoundSwitcher = new RoundVar(generateArrayToRound());
    }

    private void setUpChannelToFrequencyMap()
    {
        ArrayList<String> iwlistChannelOut = Utils.runShellScript("iwlist " +
                                                                          associatedName +
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

    private ArrayList<Integer> generateArrayToRound()
    {
        ArrayList<Integer> arrayToRound = new ArrayList<>();

        String channelsToScan = ConfigurationLoader.getConfiguration()
                                                   .getApplicationConfiguration()
                                                   .getChannelsToScan();

        if (channelsToScan != null)
        {
            Matcher matcher = Pattern.compile("(?<channelStart>\\d+)(-(?<channelEnd>\\d+))?").matcher(channelsToScan);
            while (matcher.find())
            {
                int channelStartInt = Integer.parseInt(matcher.group("channelStart"));
                String channelEnd = matcher.group("channelEnd");

                if (channelEnd != null)
                {
                    int channelEndInt = Integer.parseInt(channelEnd);

                    //else - when user put 14-1 unless 1-14
                    if (channelStartInt <= channelEndInt)
                    {
                        for (int i = channelStartInt; i <= channelEndInt; i++)
                        {
                            arrayToRound.add(i);
                        }
                    }
                    else
                    {
                        for (int i = channelStartInt; i >= channelEndInt; i--)
                        {
                            arrayToRound.add(i);
                        }
                    }
                }
                else
                {
                    arrayToRound.add(channelStartInt);
                }
            }
        }

        //secure
        if (channelsToScan == null || arrayToRound.isEmpty())
        {
            //new for ordering
            TreeMap<Byte, Short> byteShortTreeMap = new TreeMap<>(getChannelToFrequencyMap());
            Byte firstKey = byteShortTreeMap.firstKey();
            Byte lastKey = byteShortTreeMap.lastKey();

            if (arrayToRound.isEmpty())
            {
                for (int i = firstKey; i <= lastKey; i++)
                {
                    arrayToRound.add(i);
                }
            }
        }

        return arrayToRound;
    }

    public int nextChannel()
    {
        String channelSwitchingCommand = "iwconfig " +
                getAssociatedName() +
                " channel " +
                channelRoundSwitcher.nextValue();

        Utils.runShellScript(channelSwitchingCommand);

        return channelRoundSwitcher.getCurrentValue();
    }

}
