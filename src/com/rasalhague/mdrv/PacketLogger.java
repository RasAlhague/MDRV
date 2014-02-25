package com.rasalhague.mdrv;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Lazy singleton realization
 */
public class PacketLogger implements Observer
{
    private Writer writer;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private PacketLogger()
    {
        String filePath = "logs" + File.separator;
        String fileName = Utils.addTimeStampToFileName("PacketData");

        File logFile = Utils.createFile(filePath + fileName);

        try
        {
            writer = new FileWriter(logFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Observable o, Object arg)
    {
        //TODO setChanged(); Wont work
//        if (o.hasChanged())
        {
            if (arg instanceof ArrayList)
            {
                ArrayList<RxRawDataPacket> dataPackets = (ArrayList<RxRawDataPacket>) arg;
                RxRawDataPacket lastDataPacket = dataPackets.get(dataPackets.size() - 1);
                gson.toJson(lastDataPacket, writer);

                try
                {
                    writer.flush();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class PacketLoggerHolder
    {
        public static final PacketLogger INSTANCE = new PacketLogger();
    }

    public static PacketLogger getInstance()
    {
        return PacketLoggerHolder.INSTANCE;
    }

}
