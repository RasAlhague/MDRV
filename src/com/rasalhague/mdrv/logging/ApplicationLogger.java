package com.rasalhague.mdrv.logging;

import com.rasalhague.mdrv.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.logging.*;

public class ApplicationLogger extends Logger
{
    private final static String LOGGER_NAME = "ApplicationLogger";
    //    public final static Logger GLOBAL_LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public final static  Logger LOGGER      = new ApplicationLogger();

    private static ArrayList<LogRecord> logRecordArrayList = new ArrayList<>();

    /**
     * Protected method to construct a logger for a named subsystem.
     * <p>
     * The logger will be initially configured with a null Level and with useParentHandlers set to true.
     *
     * @throws MissingResourceException
     *         if the resourceBundleName is non-null and no corresponding resource can be found.
     */
    protected ApplicationLogger()
    {
        super(ApplicationLogger.LOGGER_NAME, null);
    }

    static public void setup()
    {
        LOGGER.setLevel(Level.ALL);

        try
        {
            String fileName = "logs" + File.separator + Utils.addTimeStampToFileName("Application");
            Utils.createFile(fileName);

            //choose file header to add
            FileHandler fileTxt = new FileHandler(fileName);
            LOGGER.addHandler(fileTxt);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            LOGGER.addHandler(consoleHandler);

            setFormatterToLoggerHandlers(LOGGER, new MyLogFormatter());

            LOGGER.info("Logger has initialized");
        }
        catch (SecurityException e)
        {
            LOGGER.log(Level.SEVERE, "Cannot create file due to Security reason.", e);
        }
        catch (IOException e)
        {
            LOGGER.log(Level.SEVERE, "Cannot create file due to IO error.", e);
        }
    }

    public synchronized static void addCustomHandler(Handler handler)
    {
        LOGGER.addHandler(handler);

        //update formatter
        setFormatterToLoggerHandlers(LOGGER, new MyLogFormatter());

        //send logged records
        for (LogRecord record : logRecordArrayList)
        {
            handler.publish(record);
        }

    }

    //TODO return wrong SourceClassName if active
/*
    @Override
    public synchronized void log(LogRecord record)
    {
        super.log(record);

        logRecordArrayList.add(record);
    }
*/
    public synchronized static void closeHandlers()
    {
        Handler[] handlers = LOGGER.getHandlers();
        for (Handler handler : handlers)
        {
            handler.close();
        }
    }

    private static void setFormatterToLoggerHandlers(Logger logger, Formatter formatter)
    {
        //For logger handlers
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers)
        {
            handler.setFormatter(formatter);
        }

        //For logger parent handlers
        Logger loggerParent = logger.getParent();
        if (loggerParent != null)
        {
            Handler[] loggerParentHandlers = loggerParent.getHandlers();
            for (Handler handler : loggerParentHandlers)
            {
                handler.setFormatter(formatter);
            }
        }
    }
}

class MyLogFormatter extends Formatter
{
    //    private final static String LOGGER_NAME = "ApplicationLogger";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");

    @Override
    public String format(LogRecord record)
    {
        //        LocationInfo locationInfo = new LocationInfo(new Throwable(), LOGGER_NAME);

        StringBuilder builder = new StringBuilder();
        //        String bracerOpen = "[";
        //        String bracerClose = "]";
        //        String dot = ".";
        String separator = " ";

        //        String packageName = this.getClass().getPackage().getName();
        String packageName = "com.rasalhague.mdrv";

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