package com.rasalhague.mdrv.wirelessadapter;

import com.rasalhague.mdrv.Utility.FXUtilities;
import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WirelessAdapterCommunication implements Runnable
{
    WirelessAdapter wirelessAdapter;

    @Override
    public void run()
    {
        ArrayList<WirelessAdapter> wirelessAdapters = searchWirelessAdapters();
        wirelessAdapter = chooseWirelessAdapter(wirelessAdapters);
        switchToMonitorMode(wirelessAdapter);
        //        startChannelSwitching(wirelessAdapter);
        //        startListenning(wirelessAdapter);
    }

    private ArrayList<WirelessAdapter> searchWirelessAdapters()
    {
        ArrayList<WirelessAdapter> wirelessAdaptersList = new ArrayList<>();
        ArrayList<String> iwconfig = Utils.runShellScript("iwconfig | cut -c1-10");

        Matcher iwconfigMatcher = Pattern.compile("(?<netName>\\w+)").matcher(iwconfig.toString());

        while (iwconfigMatcher.find())
        {
            wirelessAdaptersList.add(new WirelessAdapter(iwconfigMatcher.group("netName")));
        }

        return wirelessAdaptersList;
    }

    private WirelessAdapter chooseWirelessAdapter(ArrayList<WirelessAdapter> wirelessAdapters)
    {
        if (wirelessAdapters.size() == 1)
        {
            return wirelessAdapters.get(0);
        }
        else
        {
            final String[] chosenElement = new String[1];

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
                e.printStackTrace();
            }

            for (WirelessAdapter adapter : wirelessAdapters)
            {
                if (adapter.toString().equals(chosenElement[0]))
                {
                    return adapter;
                }
            }
        }

        return null;
    }

    private void switchToMonitorMode(WirelessAdapter wirelessAdapter)
    {
        final String[] pass = {"1"};

        try
        {
            FXUtilities.runAndWait(() -> {

                Stage dialogStage = Utils.prepareStageForDialog();

                Dialogs dialogs = Dialogs.create()
                                         .owner(dialogStage)
                                         .title("Set root pass")
                                         .masthead(null)
                                         .message("Set root pass");

                pass[0] = dialogs.showTextInput();
            });
        }
        catch (InterruptedException | ExecutionException e)
        {
            ApplicationLogger.LOGGER.severe(Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }

        ArrayList<String> monitorModeCommands = new ArrayList<>();
        monitorModeCommands.add("echo " +
                                        pass[0] +
                                        " | sudo -S ifconfig " +
                                        wirelessAdapter.getNetworkName() +
                                        " down");
        monitorModeCommands.add("echo " +
                                        pass[0] +
                                        " | sudo -S iwconfig " +
                                        wirelessAdapter.getNetworkName() +
                                        " mode monitor");
        monitorModeCommands.add("echo " + pass[0] + " | sudo -S ifconfig " + wirelessAdapter.getNetworkName() + " up");

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
    }

    /**
     * Observer
     */
    private final List<WirelessAdapterDataListener> wirelessAdapterDataListeners = new ArrayList<>();

    public void addListener(WirelessAdapterDataListener adapterDataListener)
    {
        wirelessAdapterDataListeners.add(adapterDataListener);
    }

    private void notifyWirelessAdapterDataListeners(int channel, WirelessAdapterData wirelessAdapterData)
    {
        wirelessAdapterDataListeners.forEach(listener -> listener.wirelessAdapterDataEvent(channel,
                                                                                           wirelessAdapterData));
    }
}
