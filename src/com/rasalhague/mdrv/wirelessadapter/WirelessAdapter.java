package com.rasalhague.mdrv.wirelessadapter;

import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.configuration.ConfigurationLoader;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WirelessAdapter
{
    private String associatedName;
    private String macAddress;
    private String busAddress;
    private String phy;
    private String adapterName;
    private final BidiMap<Byte, Short> channelToFrequencyMap = new TreeBidiMap<>();
    private volatile RoundVar channelRoundSwitcher;

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

    public void setChannelRoundSwitcher(RoundVar channelRoundSwitcher)
    {
        this.channelRoundSwitcher = channelRoundSwitcher;
    }

    @Override
    public String toString()
    {
        return associatedName + " - " + adapterName;
    }

    public WirelessAdapter(String netName, String phy)
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
        this.phy = phy;

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
        ArrayList<String> iwlistChannelOut = Utils.runShellScript("iw " + phy + " info");

        String iwlistChannelOutString = "";
        for (String s : iwlistChannelOut)
        {
            iwlistChannelOutString += s + "\n";
        }
        ApplicationLogger.LOGGER.warning(iwlistChannelOutString);

        Matcher matcher = Pattern.compile(
                "( (?<ChannelFrequency>\\d{4}) MHz)(.*\\[(?<channelNumber>\\d{1,2})\\](?! \\(d))")
                                 .matcher(iwlistChannelOutString);

        while (matcher.find())
        {
            short channelFrequency = Short.valueOf(matcher.group("ChannelFrequency"));
            byte channelNumber = Byte.valueOf(matcher.group("channelNumber"));

            channelToFrequencyMap.put(channelNumber, channelFrequency);
        }
        ApplicationLogger.LOGGER.warning(String.valueOf(channelToFrequencyMap));
    }

    private ArrayList<Integer> generateArrayToRound()
    {
        ArrayList<Integer> arrayToRound = new ArrayList<>();

        String channelsToScan = ConfigurationLoader.getConfiguration()
                                                   .getApplicationConfiguration()
                                                   .getChannelsToScan();

        if (channelsToScan == null)
        {
            channelsToScan = "1-14";

            ApplicationLogger.LOGGER.warning("channelsToScan == null, channelsToScan = \"1-14\"");
        }

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

        return arrayToRound;
    }

    public int nextChannel()
    {
        //        String channelSwitchingCommand = "iwconfig " +
        //                getAssociatedName() +
        //                " channel " +
        //                channelRoundSwitcher.nextValue();

        String channelSwitchingCommand = "iw " +
                getAssociatedName() +
                " set channel " +
                channelRoundSwitcher.nextValue();

        Utils.runShellScript(channelSwitchingCommand);

        return channelRoundSwitcher.getCurrentValue();
    }

}
