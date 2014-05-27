package com.rasalhague.mdrv.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.logging.ApplicationLogger;

import java.io.*;

/**
 * The type Configuration loader.
 * <p>
 * Load nad deserialize configuration from config.cfg
 */
public class ConfigurationLoader
{
    private static final String CONFIG_FILE_PATH          = "cfg" + File.separator + "config.cfg";
    private static final String CONFIG_FILE_RESERVED_PATH = "config.cfg";
    private static ConfigurationHolder configurationHolder;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Initialize void.
     */
    public static void initialize()
    {
        if (configurationHolder == null)
        {
            /**
             * Try to get file from /cfg/config.cfg
             */
            File configFile = new File(CONFIG_FILE_PATH);
            if (configFile.exists())
            {
                try (FileReader reader = new FileReader(configFile))
                {
                    configurationHolder = gson.fromJson(reader, ConfigurationHolder.class);
                }
                catch (FileNotFoundException e)
                {
                    ApplicationLogger.LOGGER.severe("Configuration FileNotFoundException");
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    ApplicationLogger.LOGGER.severe(e.getMessage());
                    e.printStackTrace();
                }
            }
            else
            {
                /**
                 * If /cfg/config.cfg does not exists - get reserved file from CONFIG_FILE_RESERVED_PATH
                 */
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(ConfigurationLoader.class.getResourceAsStream(
                        CONFIG_FILE_RESERVED_PATH)))
                )
                {
                    configurationHolder = gson.fromJson(reader, ConfigurationHolder.class);
                }
                catch (FileNotFoundException e)
                {
                    ApplicationLogger.LOGGER.severe("Configuration FileNotFoundException");
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    ApplicationLogger.LOGGER.severe(e.getMessage());
                    e.printStackTrace();
                }

                /**
                 * Create Config file into /cfg/config.cfg from CONFIG_FILE_RESERVED_PATH
                 */
                ApplicationLogger.LOGGER.info("Config file does not exist in /cfg/config.cfg. Creating...");

                int readBytes;
                byte[] buffer = new byte[4096];

                Utils.createFile(CONFIG_FILE_PATH);

                try (OutputStream resStreamOut = new FileOutputStream(new File(CONFIG_FILE_PATH));
                     InputStream stream = ConfigurationLoader.class.getResourceAsStream(CONFIG_FILE_RESERVED_PATH))
                {
                    while ((readBytes = stream.read(buffer)) > 0)
                    {
                        resStreamOut.write(buffer, 0, readBytes);
                    }
                }
                catch (IOException e1)
                {
                    ApplicationLogger.LOGGER.severe(e1.getMessage());
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets loaded configuration.
     *
     * @return the configuration
     */
    public static ConfigurationHolder getConfiguration()
    {
        return configurationHolder;
    }
}
