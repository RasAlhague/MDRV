package com.rasalhague.mdrv.replay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rasalhague.mdrv.DataPacket;
import com.rasalhague.mdrv.DataPacketListener;
import com.rasalhague.mdrv.logging.ApplicationLogger;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class Replay
{
    private static final Replay INSTANCE = new Replay();
    private static final Gson   gson     = new GsonBuilder().setPrettyPrinting().create();

    public static Replay getInstance()
    {
        return INSTANCE;
    }

    public void loadReplay()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int ret = fileChooser.showDialog(null, "Choose replay file");
        if (ret == JFileChooser.APPROVE_OPTION)
        {
            File inputFile = fileChooser.getSelectedFile();

            try (FileReader fileReader = new FileReader(inputFile))
            {
                Type type = new TypeToken<ArrayList<DataPacket>>() {}.getType();
                ArrayList<DataPacket> dataPackets = gson.fromJson(fileReader, type);
                //                    System.out.println(Arrays.toString(dataPackets.get(0).getDeviceInfo().getEndPacketSequence()));
            }
            catch (FileNotFoundException e)
            {
                ApplicationLogger.LOGGER.severe("Replay FileNotFoundException");
            }
            catch (IOException e)
            {
                ApplicationLogger.LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //region Observer implementation

    private ArrayList<DataPacketListener> dataPacketListeners = new ArrayList<>();

    public void addListener(DataPacketListener toAdd)
    {
        dataPacketListeners.add(toAdd);
    }

    private void notifyDataPacketListeners(DataPacket dataPacket)
    {
        dataPacketListeners.forEach(o -> o.dataPacketEvent(dataPacket));
    }

    //endregion
}
