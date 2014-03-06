package com.rasalhague.mdrv.logging;

import com.google.gson.*;
import com.rasalhague.mdrv.DataPacket;
import com.rasalhague.mdrv.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Lazy singleton realization
 */
public class PacketLogger implements Observer
{
    private Writer writer;
    private Gson gson = new GsonBuilder().setPrettyPrinting()
                                         .registerTypeAdapter(ArrayList.class, new DateTimeSerializer())
                                         .create();

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

    public static PacketLogger getInstance()
    {
        return PacketLoggerHolder.INSTANCE;
    }

    @Override
    public synchronized void update(Observable o, Object arg)
    {
        //TODO setChanged(); Wont work
        //        if (o.hasChanged())
        {
            if (arg instanceof ArrayList)
            {
                ArrayList<DataPacket> dataPackets = (ArrayList<DataPacket>) arg;
                DataPacket lastDataPacket = dataPackets.get(dataPackets.size() - 1);
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
}

class DateTimeSerializer implements JsonSerializer<ArrayList<Integer>>
{
    public JsonElement serialize(ArrayList<Integer> src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.toString());
    }
}