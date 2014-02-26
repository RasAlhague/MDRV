package com.rasalhague.mdrv;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class ApplicationLogger
{
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

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
            fileTxt.setFormatter(new MyLogFormatter());
            logger.addHandler(fileTxt);

            Logger parent = logger.getParent();
            Handler[] handlers = parent.getHandlers();
            for (Handler handler : handlers)
            {
                handler.setFormatter(new MyLogFormatter());
            }
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

    static public void severe(String msg)
    {
        LOGGER.severe(msg);
    }

    static public void warning(String msg)
    {
        LOGGER.warning(msg);
    }

    static public void info(String msg)
    {
        LOGGER.info(msg);
    }

    static public void config(String msg)
    {
        LOGGER.config(msg);
    }

    static public void fine(String msg)
    {
        LOGGER.fine(msg);
    }

    static public void finer(String msg)
    {
        LOGGER.finer(msg);
    }

    static public void finest(String msg)
    {
        LOGGER.finest(msg);
    }
}

class MyLogFormatter extends Formatter
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm:ss.SSS");

    @Override
    public String format(LogRecord record)
    {
        StringBuilder builder = new StringBuilder();
//        String bracerOpen = "[";
//        String bracerClose = "]";
//        String dot = ".";
        String separator = " ";

        String packageName = this.getClass().getPackage().getName();

        builder.append(DATE_FORMAT.format(new Date(record.getMillis())));

        builder.append(separator);

        builder.append("[").append(record.getLevel()).append("]");

        builder.append(separator);

        builder.append("[")
                .append(record.getSourceClassName().replace(packageName + ".", ""))
                .append(".")
                .append(record.getSourceMethodName())
                .append("]");

        builder.append(separator);

        builder.append(formatMessage(record));

        builder.append("\n");

        return builder.toString();
    }
}