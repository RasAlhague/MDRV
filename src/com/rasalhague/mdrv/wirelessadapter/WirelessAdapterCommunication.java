package com.rasalhague.mdrv.wirelessadapter;

import com.rasalhague.mdrv.Utility.FXUtilities;
import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.configuration.ConfigurationLoader;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import javafx.stage.Stage;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.dialog.Dialogs;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Wireless adapter communication.
 * <p>
 * Class that switches wireless adapter into monitor mode and starts listening tcpdump!
 * <p>
 * http://sparebits.wikispaces.com/tcpdump+wireless+filters
 */
public class WirelessAdapterCommunication implements Runnable
{
    private       int                    channelSwitchingRateMs = 1000;
    private final HashMap<Float, String> bpsToStandart          = new HashMap<>();

    /**
     * Instantiates a new Wireless adapter communication.
     */
    public WirelessAdapterCommunication()
    {
        //b
        bpsToStandart.put(1f, "b");
        bpsToStandart.put(2f, "b");
        bpsToStandart.put(5.5f, "b");
        bpsToStandart.put(11f, "b");

        //g
        bpsToStandart.put(6f, "g");
        bpsToStandart.put(9f, "g");
        bpsToStandart.put(12f, "g");
        bpsToStandart.put(18f, "g");
        bpsToStandart.put(24f, "g");
        bpsToStandart.put(36f, "g");
        bpsToStandart.put(48f, "g");
        bpsToStandart.put(54f, "g");

        //n
        bpsToStandart.put(7f, "n");
        bpsToStandart.put(13f, "n");
        bpsToStandart.put(21f, "n");
        bpsToStandart.put(26f, "n");
        bpsToStandart.put(39f, "n");
        bpsToStandart.put(52f, "n");
        bpsToStandart.put(65f, "n");
        bpsToStandart.put(72f, "n");
        bpsToStandart.put(78f, "n");
        bpsToStandart.put(104f, "n");
        bpsToStandart.put(117f, "n");
        bpsToStandart.put(130f, "n");

        //MIMO 1; 20 MHz
        bpsToStandart.put(6.5f, "n");
        bpsToStandart.put(13f, "n");
        bpsToStandart.put(19.5f, "n");
        bpsToStandart.put(26f, "n");
        bpsToStandart.put(39f, "n");
        bpsToStandart.put(52f, "n");
        bpsToStandart.put(58.5f, "n");
        bpsToStandart.put(65f, "n");

        //MIMO 1; 40 MHz
        bpsToStandart.put(13.5f, "n");
        bpsToStandart.put(13f, "n");
        bpsToStandart.put(40.5f, "n");
        //        bpsToStandart.put(54f, "n");
        bpsToStandart.put(81f, "n");
        bpsToStandart.put(108f, "n");
        bpsToStandart.put(121.5f, "n");
        bpsToStandart.put(135f, "n");

        //MIMO 2; 20 MHz
        bpsToStandart.put(13f, "n");
        bpsToStandart.put(26f, "n");
        bpsToStandart.put(39f, "n");
        bpsToStandart.put(52f, "n");
        bpsToStandart.put(78f, "n");
        bpsToStandart.put(104f, "n");
        bpsToStandart.put(117f, "n");
        bpsToStandart.put(130f, "n");

        //MIMO 2; 40 MHz
        bpsToStandart.put(27f, "n");
        bpsToStandart.put(81f, "n");
        bpsToStandart.put(108f, "n");
        bpsToStandart.put(162f, "n");
        bpsToStandart.put(216f, "n");
        bpsToStandart.put(243f, "n");
        bpsToStandart.put(270f, "n");

        //MIMO 3; 20 MHz
        bpsToStandart.put(19.5f, "n");
        bpsToStandart.put(39f, "n");
        bpsToStandart.put(58.5f, "n");
        bpsToStandart.put(78f, "n");
        bpsToStandart.put(117f, "n");
        bpsToStandart.put(156f, "n");
        bpsToStandart.put(175.5f, "n");
        bpsToStandart.put(195f, "n");

        //MIMO 3; 40 MHz
        bpsToStandart.put(40.5f, "n");
        bpsToStandart.put(81f, "n");
        bpsToStandart.put(121.5f, "n");
        bpsToStandart.put(162f, "n");
        bpsToStandart.put(243f, "n");
        bpsToStandart.put(324f, "n");
        bpsToStandart.put(364.5f, "n");
        bpsToStandart.put(405.0f, "n");

        bpsToStandart.put(39f, "n");
        bpsToStandart.put(52f, "n");
        bpsToStandart.put(43.3f, "n");
        bpsToStandart.put(7.2f, "n");
        bpsToStandart.put(15f, "n");
        bpsToStandart.put(14.4f, "n");
        bpsToStandart.put(30f, "n");
        bpsToStandart.put(45f, "n");
        bpsToStandart.put(43.3f, "n");
        bpsToStandart.put(57.8f, "n");
        bpsToStandart.put(28.9f, "n");
        bpsToStandart.put(43.3f, "n");
        bpsToStandart.put(21.7f, "n");
        bpsToStandart.put(43.3f, "n");
        bpsToStandart.put(28.9f, "n");
        bpsToStandart.put(26f, "n");
        bpsToStandart.put(52f, "n");
    }

    @Override
    public void run()
    {
        if (SystemUtils.IS_OS_LINUX)
        {
            if (Utils.checkInxiExist())
            {
                ArrayList<WirelessAdapter> wirelessAdapters = searchWirelessAdapters();

                if (wirelessAdapters.size() != 0)
                {
                    WirelessAdapter wirelessAdapter = chooseWirelessAdapter(wirelessAdapters);
                    switchToMonitorMode(wirelessAdapter);
                    startChannelSwitching(wirelessAdapter);
                    String tcpDumpCommand = chooseTcpDumpCommand(wirelessAdapter);
                    startListening(tcpDumpCommand, wirelessAdapter);
                }
                else
                {
                    ApplicationLogger.LOGGER.warning("No adapters have been found.");
                }
            }
            else
            {
                ApplicationLogger.LOGGER.warning("Install Inxi for get able to connect wireless adapter.");
                ApplicationLogger.LOGGER.warning("Trying to install Inxi...");

                if (Utils.installInxi())
                {
                    run();
                }
                else
                {
                    ApplicationLogger.LOGGER.warning("Can not install Inxi automatically.");
                }
            }
        }
        else
        {
            ApplicationLogger.LOGGER.warning("Only linux based OS can work with WirelessAdapter");
        }
    }

    private ArrayList<WirelessAdapter> searchWirelessAdapters()
    {
        ArrayList<WirelessAdapter> wirelessAdaptersList = new ArrayList<>();

        String toolForSearchWirelessAdapters = ConfigurationLoader.getConfiguration()
                                                                  .getApplicationConfiguration()
                                                                  .getToolForSearchWirelessAdapters();

        if (toolForSearchWirelessAdapters.equals("iwconfig"))
        {
            //via iwconfig
            ArrayList<String> iwconfig = Utils.runShellScript("iwconfig | cut -c1-10");
            Matcher matcher = Pattern.compile("(?<netName>\\w+)").matcher(iwconfig.toString());

            while (matcher.find())
            {
                wirelessAdaptersList.add(new WirelessAdapter(matcher.group("netName")));
            }
        }
        else if (toolForSearchWirelessAdapters.equals("iw dev"))
        {
            //via iw
            ArrayList<String> iwdev = Utils.runShellScript("iw dev");
            Matcher matcher = Pattern.compile("(Interface (?<netName>\\w+))").matcher(iwdev.toString());

            while (matcher.find())
            {
                wirelessAdaptersList.add(new WirelessAdapter(matcher.group("netName")));
            }
        }

        return wirelessAdaptersList;
    }

    private WirelessAdapter chooseWirelessAdapter(ArrayList<WirelessAdapter> wirelessAdapters)
    {
        WirelessAdapter chosenWirelessAdapter;

        if (wirelessAdapters.size() == 1)
        {
            chosenWirelessAdapter = wirelessAdapters.get(0);

            ApplicationLogger.LOGGER.info(chosenWirelessAdapter.getAdapterName() + " has been chosen.");
            return chosenWirelessAdapter;
        }
        else
        {
            final String[] chosenElement = new String[1];

            //Show Choose Adapter dialog
            try
            {
                FXUtilities.runAndWait(() -> {

                    ArrayList<String> wirelessAdaptersNames = new ArrayList<>();
                    for (WirelessAdapter adapter : wirelessAdapters)
                    {
                        wirelessAdaptersNames.add(adapter.toString());
                    }

                    Stage dialogStage = Utils.prepareStageForDialog();

                    Dialogs dialogs = Dialogs.create()
                                             .owner(dialogStage)
                                             .title("Choose Adapter")
                                             .masthead(null)
                                             .message("Choose Adapter:");

                    chosenElement[0] = dialogs.showChoices(wirelessAdaptersNames);
                });
            }
            catch (InterruptedException | ExecutionException e)
            {
                ApplicationLogger.LOGGER.severe(Arrays.toString(e.getStackTrace()));
                ApplicationLogger.LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }

            for (WirelessAdapter adapter : wirelessAdapters)
            {
                if (adapter.toString().equals(chosenElement[0]))
                {
                    chosenWirelessAdapter = adapter;

                    ApplicationLogger.LOGGER.info(chosenWirelessAdapter.getAdapterName() + " has been chosen.");
                    return chosenWirelessAdapter;
                }
            }
        }

        //You Shall Not Pass !!! :[
        return chooseWirelessAdapter(wirelessAdapters);
    }

    private void switchToMonitorMode(WirelessAdapter wirelessAdapter)
    {
        ArrayList<String> monitorModeCommands = new ArrayList<>();
        monitorModeCommands.add("ifconfig " +
                                        wirelessAdapter.getAssociatedName() +
                                        " down");
        monitorModeCommands.add("iwconfig " +
                                        wirelessAdapter.getAssociatedName() +
                                        " mode monitor");
        monitorModeCommands.add("ifconfig " + wirelessAdapter.getAssociatedName() + " up");

        monitorModeCommands.forEach((t) -> {

            ArrayList<String> out = Utils.runShellScript(t);
            if (out.size() == 0)
            {
                ApplicationLogger.LOGGER.info(t + " - ok");
            }
            else
            {
                ApplicationLogger.LOGGER.info(t + "\n" + out);

                switchToMonitorMode(wirelessAdapter);
            }
        });

        ApplicationLogger.LOGGER.info(wirelessAdapter.getAdapterName() + " has been transferred into monitor mode.");
    }

    private void startChannelSwitching(WirelessAdapter wirelessAdapter)
    {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate((Runnable) () -> {

            wirelessAdapter.nextChannel();
            System.out.print(wirelessAdapter.getChannelRoundSwitcher().getCurrentValue() + " ");

        }, 0, channelSwitchingRateMs, TimeUnit.MILLISECONDS);

        ApplicationLogger.LOGGER.info("Channel switching has been started in " +
                                              wirelessAdapter.getChannelRoundSwitcher().getMinValue() +
                                              " - " +
                                              wirelessAdapter.getChannelRoundSwitcher().getMaxValue() +
                                              " range.");
    }

    private String chooseTcpDumpCommand(WirelessAdapter wirelessAdapter)
    {
        final String[] chosenElement = new String[1];

        try
        {
            FXUtilities.runAndWait(() -> {

                Stage dialogStage = Utils.prepareStageForDialog();

                Dialogs dialogs = Dialogs.create()
                                         .owner(dialogStage)
                                         .title("Choose TcpDump Command")
                                         .masthead(null)
                                         .message(null);

                chosenElement[0] = dialogs.showTextInput("tcpdump -i " +
                                                                 wirelessAdapter.getAssociatedName() +
                                                                 " -s 0 -nne '(type data subtype qos-data)'");
            });
        }
        catch (InterruptedException | ExecutionException e)
        {
            ApplicationLogger.LOGGER.severe(e.getMessage());
            ApplicationLogger.LOGGER.severe(Arrays.toString(e.getStackTrace()));
            //            ApplicationLogger.LOGGER.severe(e.toString());
            e.printStackTrace();
        }

        return chosenElement[0];
    }

    private void startListening(String tcpDumpCommand, WirelessAdapter wirelessAdapter)
    {
        String resultExecute;
        BufferedReader tcpDumpReader = Utils.runShellScriptBR(tcpDumpCommand);
        ApplicationLogger.LOGGER.info("Listening has been started on " + wirelessAdapter.getAssociatedName());

        try
        {
            while ((resultExecute = tcpDumpReader.readLine()) != null)
            {
                Matcher matcher = Pattern.compile(
                        //                        "(?<!bad-fcs)( (?<MbFirst>\\d{1,3}\\.\\d{1,3}) Mb\\/s )?((?<frequency>\\d{4}) MHz.*?)(11(?<standart>.))(.*?(?<dB>-\\d{2,3})dB)(.*?(?<MbSecond>\\d{1,2}\\.\\d) Mb\\/s )?(.*?IV: *?(?<IV>(\\d|\\w){1,4}))?"
                        //                        "( (?<MbFirst>\\d{1,3}\\.\\d{1,3}) Mb\\/s )?((?<frequency>\\d{4}) MHz.*?)(11(?<standart>.))(.*?(?<dB>-\\d{2,3})dB)(.*?(?<MbSecond>\\d{1,2}\\.\\d) Mb\\/s )?(.*?IV: *?(?<IV>(\\d|\\w){1,4}))?")
                        "( (?<MbFirst>\\d{1,3}\\.\\d{1,3}) Mb\\/s )?((?<frequency>\\d{4}) MHz.*?)(.*?(?<dB>-\\d{2,3})dB)(.*?(?<MbSecond>\\d{1,2}\\.\\d) Mb\\/s )?")
                                         .matcher(resultExecute);

                while (matcher.find())
                {
                    try
                    {
                        byte channel = wirelessAdapter.getChannelToFrequencyMap()
                                                      .getKey(Short.valueOf(matcher.group("frequency")));

                        byte dB = Byte.valueOf(matcher.group("dB"));
                        String bpsFirst = matcher.group("MbFirst");
                        String bpsSecond = matcher.group("MbSecond");
                        float frequency = Float.valueOf(matcher.group("frequency"));
                        String standart;
                        float bps;

                        //filter from -20dBm to -105dBm
                        byte lowerLimit = -110;
                        byte upperLimit = -20;
                        if (dB >= lowerLimit && dB <= upperLimit)
                        {
                            if (bpsFirst != null)
                            {
                                bps = Float.parseFloat(bpsFirst);
                            }
                            else if (bpsSecond != null)
                            {
                                bps = Float.parseFloat(bpsSecond);
                            }
                            else
                            {
                                bps = 130f;

                                ApplicationLogger.LOGGER.warning("Without BPS");
                                ApplicationLogger.LOGGER.warning(resultExecute);
                            }

                            if (bps > 54)
                            {
                                standart = "n";
                            }
                            else
                            {
                                standart = bpsToStandart.get(bps);
                            }

                            if (standart == null)
                            {

                                if (bps > 11)
                                {
                                    standart = "g";
                                }
                                else
                                {
                                    standart = "b";
                                }
                            }
                            System.out.print(bps + " " + standart + "; ");

                            WirelessAdapterData wirelessAdapterData = new WirelessAdapterData(channel,
                                                                                              dB,
                                                                                              bps,
                                                                                              frequency,
                                                                                              standart);
                            notifyWirelessAdapterDataListeners(wirelessAdapterData);
                        }
                    }
                    catch (Exception e)
                    {
                        ApplicationLogger.LOGGER.severe(e.getMessage());
                        ApplicationLogger.LOGGER.severe(Arrays.toString(e.getStackTrace()));
                        ApplicationLogger.LOGGER.severe(resultExecute);

                        e.printStackTrace();
                    }
                }
            }
        }
        catch (IOException e)
        {
            ApplicationLogger.LOGGER.severe(e.getMessage());
            ApplicationLogger.LOGGER.severe(Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
    }

    /**
     * Observer
     */
    private final List<WirelessAdapterDataListener> wirelessAdapterDataListeners = new ArrayList<>();

    /**
     * Add listener.
     *
     * @param adapterDataListener
     *         the adapter data listener
     */
    public void addListener(WirelessAdapterDataListener adapterDataListener)
    {
        wirelessAdapterDataListeners.add(adapterDataListener);
    }

    private void notifyWirelessAdapterDataListeners(WirelessAdapterData wirelessAdapterData)
    {
        wirelessAdapterDataListeners.forEach(listener -> listener.wirelessAdapterDataEvent(wirelessAdapterData));
    }
}
