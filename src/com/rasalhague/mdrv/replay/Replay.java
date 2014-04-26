package com.rasalhague.mdrv.replay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.rasalhague.mdrv.DataPacket;
import com.rasalhague.mdrv.DataPacketListener;
import com.rasalhague.mdrv.analysis.PacketAnalysis;
import com.rasalhague.mdrv.logging.ApplicationLogger;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Provides replay loading
 */
public class Replay
{
    private static final Replay INSTANCE = new Replay();
    private static final Gson   gson     = new GsonBuilder().setPrettyPrinting().create();

    private ArrayList<DataPacket> loadedDataPackets;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static Replay getInstance()
    {
        return INSTANCE;
    }

    /**
     * Load replay.
     * <p>
     * Load and send loaded data via packetAnalysis.dataPacketEvent(dataPacket);
     */
    public void loadReplay()
    {
        loadedDataPackets = loadPacketsFromFile();
        if (loadedDataPackets != null)
        {
            PacketAnalysis packetAnalysis = PacketAnalysis.getInstance();

            for (DataPacket dataPacket : loadedDataPackets)
            {
                packetAnalysis.dataPacketEvent(dataPacket);
            }
        }
        else
        {
            ApplicationLogger.LOGGER.severe("loadedDataPackets != null");
        }
    }

    private ArrayList<DataPacket> loadPacketsFromFile()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int ret = fileChooser.showDialog(null, "Choose replay file");
        if (ret == JFileChooser.APPROVE_OPTION)
        {
            File inputFile = fileChooser.getSelectedFile();

            try (FileReader fileReader = new FileReader(inputFile))
            {
                JsonReader jsonReader = new JsonReader(fileReader);
                Type type = new TypeToken<ArrayList<DataPacket>>() {}.getType();
                return gson.fromJson(jsonReader, type);
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
        return null;
    }

    //region Observer implementation

    private final ArrayList<DataPacketListener> dataPacketListeners = new ArrayList<>();

    /**
     * Add listener.
     *
     * Observer realisation
     *
     * @param toAdd the to add
     */
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
