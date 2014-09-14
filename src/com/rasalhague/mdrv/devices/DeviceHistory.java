package com.rasalhague.mdrv.devices;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.Utility.FXUtilities;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class DeviceHistory
{
    private static HashMap<DeviceInfo, Integer> connectedDeviceHistory = new HashMap<>();

    public void checkForCollision(DeviceInfo deviceInfo)
    {
        if (checkDevice(deviceInfo))
        {
            solveCollision(deviceInfo);
        }
        else
        {
            connectedDeviceHistory.put(deviceInfo, deviceInfo.getId());
        }
    }

    public void printHistory()
    {
        ApplicationLogger.LOGGER.info(String.valueOf(connectedDeviceHistory));
    }

    private boolean checkDevice(DeviceInfo deviceInfo)
    {
        return connectedDeviceHistory.containsKey(deviceInfo);
    }

    private void solveCollision(DeviceInfo deviceInfo)
    {
        String message = "Device collision detected\n" +
                "Device with name " +
                deviceInfo.getFriendlyNameWithId() + " on " + deviceInfo.getPortName() +
                " was connected earlier.\n" +
                "Ok - connect as new.\n" +
                "No - merge with previous.";

        //        Platform.runLater(() -> {
        //
        //            Action response = Dialogs.create()
        //                                     .owner(null)
        //                                     .title("Device collision detected")
        //                                     .masthead(null)
        //                                     .message(message)
        //                                     .lightweight()
        //                                     .actions(Dialog.Actions.OK, Dialog.Actions.NO)
        //                                     .showConfirm();
        //
        //            if (response == Dialog.Actions.OK)
        //            {
        //                device.getDeviceInfo().incrementID();
        //            }
        //        });

        try
        {
            FXUtilities.runAndWait(() -> {

                Action collisionResponse = Dialogs.create()
                                                  .owner(null)
                                                  .masthead(null)
                                                  .message(message)
                                                  .lightweight()
                                                  .style(DialogStyle.UNDECORATED)
                                                  .actions(Dialog.Actions.OK, Dialog.Actions.NO)
                                                  .showConfirm();

                if (collisionResponse == Dialog.Actions.OK)
                {
                    connectedDeviceHistory.put(deviceInfo, connectedDeviceHistory.get(deviceInfo) + 1);
                    deviceInfo.setId(connectedDeviceHistory.get(deviceInfo));
                }
                if (collisionResponse == Dialog.Actions.NO)
                {
                    ArrayList<String> choices = new ArrayList<>();

                    for (int i = 0; i <= connectedDeviceHistory.get(deviceInfo); i++)
                    {
                        choices.add(String.valueOf(i));
                    }

                    Optional chooseResponse = Dialogs.create()
                                                     .owner(null)
                                                     .masthead(null)
                                                     .message("Choose device flow to connect to")
                                                     .lightweight()
                                                     .style(DialogStyle.UNDECORATED)
                                                     .actions(Dialog.Actions.OK)
                                                     .showChoices(choices);

                    int chosenId = Integer.parseInt(chooseResponse.get().toString());
                    deviceInfo.setId(chosenId);
                }
            });
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
    }
}
