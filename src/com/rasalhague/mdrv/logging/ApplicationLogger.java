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
    //    public final static Logger GLOBAL_LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public final static Logger LOGGER = new ApplicationLogger("ApplicationLogger", null);

    private static ArrayList<LogRecord> logRecordArrayList = new ArrayList<LogRecord>();

    /**
     * Protected method to construct a logger for a named subsystem.
     * <p/>
     * The logger will be initially configured with a null Level and with useParentHandlers set to true.
     *
     * @param name
     *         A name for the logger.  This should be a dot-separated name and should normally be based on the package
     *         name or class name of the subsystem, such as java.net or javax.swing.  It may be null for anonymous
     *         Loggers.
     * @param resourceBundleName
     *         name of ResourceBundle to be used for localizing messages for this logger.  May be null if none of the
     *         messages require localization.
     *
     * @throws MissingResourceException
     *         if the resourceBundleName is non-null and no corresponding resource can be found.
     */
    protected ApplicationLogger(String name, String resourceBundleName)
    {
        super(name, resourceBundleName);
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

    @Override
    public synchronized void log(LogRecord record)
    {
        super.log(record);

        logRecordArrayList.add(record);
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

    //TODO Do not work: out wrong class name (this) instead of caller class name
    //region Do not work: out wrong class name (this) instead of caller class name

    /*
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
    */

    //endregion
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