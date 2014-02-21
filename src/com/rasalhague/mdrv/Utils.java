package com.rasalhague.mdrv;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils
{
    public final static Boolean DEBUG_OUTPUT = false;

    public static String addTimeStampToFileName(String name)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy 'at' HH.mm.ss", Locale.ENGLISH);
        Date currentDate = new Date();
        String formattedDate = dateFormat.format(currentDate);

        int index = name.lastIndexOf(".");

        String mainFName;
        if (index != -1)
        {
            mainFName = name.substring(0, index - 1) + " " + formattedDate + name.substring(index - 1, name.length());
        }
        else
        {
            mainFName = name + " " + formattedDate + ".log";
        }

        return mainFName;
    }

    public static File createFile(String fileNameWithPath)
    {
        File file = new File(fileNameWithPath);
        file.getParentFile().mkdirs();
        try
        {
            file.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return file;
    }
}
