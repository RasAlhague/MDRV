package com.rasalhague.mdrv;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ApplicationLogger
{
    public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    static public void setup()
    {
        // Get the global logger to configure it
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        logger.setLevel(Level.ALL);

        try
        {
            String fileName = "logs" + File.separator + Utils.addTimeStampToFileName("Application");
            Utils.createFile(fileName);

            FileHandler fileTxt = new FileHandler(fileName);
            logger.addHandler(fileTxt);

            // create txt Formatter
            SimpleFormatter formatterTxt = new SimpleFormatter();
            fileTxt.setFormatter(formatterTxt);
        }
        catch (SecurityException e)
        {
            logger.log(Level.SEVERE, "Cannot create file due to Security reason.", e);
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, "Cannot create file due to IO error.", e);
        }
    }
}
