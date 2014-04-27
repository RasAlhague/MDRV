package com.rasalhague.mdrv.logging;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.rasalhague.mdrv.DataPacket;
import com.rasalhague.mdrv.DataPacketListener;
import com.rasalhague.mdrv.Utils;
import com.rasalhague.mdrv.configuration.ConfigurationLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lazy singleton realization
 */
public class PacketLogger implements DataPacketListener
{
    private static final PacketLogger INSTANCE = new PacketLogger();

    private final Gson gson = new GsonBuilder()
            //            .setPrettyPrinting()
            //                                         .registerTypeAdapter(ArrayList.class, new ArrayListSerializer())
            //                                         .registerTypeAdapter(byte[].class, new ByteArraySerializer())
            .setExclusionStrategies(new CustomExclusionStrategies()).create();

    private final File       logFile;
    private       JsonWriter jsonWriter;
    private       Writer     writer;
    private       boolean firstPacketExist       = false;
    private       long    packetCounter          = 0;
    private final int     packetsRequiredToWrite = 10;

    private PacketLogger()
    {
        String filePath = "logs" + File.separator;
        String fileName = Utils.addTimeStampToFileName("PacketData");

        logFile = Utils.createFile(filePath + fileName);
        try
        {
            writer = new FileWriter(logFile, false);
            jsonWriter = new JsonWriter(writer);
            jsonWriter.beginArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        ApplicationLogger.LOGGER.info("PacketLogger has been started");
    }

    public static PacketLogger getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void dataPacketEvent(DataPacket dataPacket)
    {
        writeDataPacket(dataPacket);
    }

    private synchronized void writeDataPacket(DataPacket dataPacket)
    {
        packetCounter++;

        //append - 2d parameter
        try /*(FileWriter writer = new FileWriter(logFile, false))*/
        {
            //firstPacketExist needs for exclude null in the end
            if (firstPacketExist)
            {
                writer.write("," + gson.toJson(dataPacket));
            }
            else
            {
                writer.write(gson.toJson(dataPacket));
            }
            writer.flush();
            firstPacketExist = true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void closeWriter()
    {
        try
        {
            jsonWriter.endArray();
            jsonWriter.flush();
            jsonWriter.close();

            if (packetCounter < packetsRequiredToWrite)
            {
                logFile.delete();
                ApplicationLogger.LOGGER.info("PacketData file has been deleted via small count of packets");
            }
        }
        catch (IOException e)
        {
            ApplicationLogger.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Custom serializer for ArrayList<Integer>
 */
class ArrayListSerializer implements JsonSerializer<ArrayList<Integer>>
{
    public JsonElement serialize(ArrayList<Integer> src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.toString());
    }
}

/**
 * Custom serializer for byte[]
 */
class ByteArraySerializer implements JsonSerializer<byte[]>
{
    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(Arrays.toString(src));
    }
}

/**
 * Custom fields exclusion
 */
class CustomExclusionStrategies implements ExclusionStrategy
{
    @Override
    public boolean shouldSkipField(FieldAttributes f)
    {
        List<String> excludedFieldsList = ConfigurationLoader.getConfiguration()
                                                             .getApplicationConfiguration()
                                                             .getExcludedFieldsList();

        return excludedFieldsList.contains(f.getName());
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz)
    {
        return false;
    }
}