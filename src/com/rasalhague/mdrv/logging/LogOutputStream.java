package com.rasalhague.mdrv.logging;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * The type Log output stream.
 */
public class LogOutputStream extends java.io.OutputStream
{
    private static final Logger       logger           = ApplicationLogger.LOGGER;
    private final        char         lineSeparatorEnd = '\n';
    private final        String       lineSeparator    = System.getProperty("line.separator");
    private final        StringBuffer buffer           = new StringBuffer();

    /**
     * Sets .
     */
    public static void setup()
    {
        PrintStream out = new PrintStream(new LogOutputStream(), true);
        System.setOut(out);
    }

    public void write(int b) throws IOException
    {
        char ch = (char) b;
        this.buffer.append(ch);
        if (ch == lineSeparatorEnd)
        {
            // Check on a char by char basis for speed
            String s = buffer.toString();
            if (s.contains(lineSeparator))
            {
                // The whole separator string is written
                logger.info(s.substring(0, s.length() - lineSeparator.length()));
                buffer.setLength(0);
            }
        }
    }
}
