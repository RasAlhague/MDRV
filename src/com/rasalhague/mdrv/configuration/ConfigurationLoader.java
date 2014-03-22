package com.rasalhague.mdrv.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigurationLoader
{
    private static final String CONFIG_FILE_PATH = "cfg" + File.separator + "config.cfg";
    private static ConfigurationHolder configurationHolder;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static ConfigurationHolder getConfiguration()
    {
        if (configurationHolder == null)
        {
            try (FileReader reader = new FileReader(CONFIG_FILE_PATH))
            {
                configurationHolder = gson.fromJson(reader, ConfigurationHolder.class);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return configurationHolder;
    }
}
