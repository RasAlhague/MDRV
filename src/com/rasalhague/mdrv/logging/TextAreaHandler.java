package com.rasalhague.mdrv.logging;

import javafx.scene.control.TextArea;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class TextAreaHandler extends Handler
{
    volatile TextArea textArea;

    public TextAreaHandler(TextArea textArea)
    {
        this.textArea = textArea;
    }

    @Override
    public synchronized void publish(LogRecord record)
    {
        String msg = getFormatter().format(record);
        try
        {
            textArea.appendText(msg);
        }
        catch (NullPointerException e)
        {
            ApplicationLogger.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void flush()
    {

    }

    @Override
    public synchronized void close() throws SecurityException
    {

    }
}
