package com.rasalhague.mdrv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static final HashMap<String, String> searchRegistry(String location, String key)
    {
        try
        {
            // Run reg query, then read output with StreamReader (internal class)

            String request = "reg query " + "\"" + location + "\"" + " /s /f " + key;
            Process process = Runtime.getRuntime().exec(request);

            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();
            String output = reader.getResult();

            Pattern pattern = Pattern.compile(
                    "HKEY_LOCAL_MACHINE.*USB.*VID_(?<vid>.{4})&PID_(?<pid>.{4}).*\\n *\\w* *\\w* *(?<devName>.*\\))",
                    Pattern.UNIX_LINES);

            //TODO need finalize
            Matcher matcher = pattern.matcher(output);
            HashMap<String, String> devInfMap = new HashMap<String, String>();
            matcher.find();
            devInfMap.put("vid", matcher.group("vid"));
            devInfMap.put("pid", matcher.group("pid"));
            devInfMap.put("devName", matcher.group("devName"));

            return devInfMap;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static HashMap<String, String> getDeviceNameFromWinRegistry(String devPortName)
    {
        HashMap<String, String> result = searchRegistry("HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Enum\\USB",
                                                        devPortName);
        System.out.println(result);
        return result;
    }

    static class StreamReader extends Thread
    {
        private InputStream is;
        private StringWriter sw = new StringWriter();

        public StreamReader(InputStream is)
        {
            this.is = is;
        }

        @Override
        public void run()
        {
            try
            {
                int c;
                while ((c = is.read()) != -1)
                { sw.write(c); }
            }
            catch (IOException e)
            {
            }
        }

        public String getResult()
        {
            return sw.toString();
        }
    }
}
