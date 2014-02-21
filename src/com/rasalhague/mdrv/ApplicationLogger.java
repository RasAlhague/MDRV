package com.rasalhague.mdrv;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

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

            //choose file header to add
            FileHandler fileTxt = new FileHandler(fileName);
            logger.addHandler(fileTxt);

            fileTxt.setFormatter(new MyLogFormatter());
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

class MyLogFormatter extends Formatter
{
    private static final SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss.SSS");

    @Override
    public String format(LogRecord record)
    {
        StringBuilder builder = new StringBuilder();
//        String bracerOpen = "[";
//        String bracerClose = "]";
//        String dot = ".";

        builder.append(dataFormat.format(new Date(record.getMillis())));

        builder.append(" - ");

        builder.append("[").append(record.getLevel()).append("]");

        builder.append(" - ");

        builder.append("[")
                .append(record.getSourceClassName())
                .append(".")
                .append(record.getSourceMethodName())
                .append("]");

        builder.append(" - ");

        builder.append(formatMessage(record));

        builder.append("\n");

        return builder.toString();
    }
}