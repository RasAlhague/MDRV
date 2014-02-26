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
            ApplicationLogger.severe(e.getMessage());
            e.printStackTrace();
        }

        return file;
    }

    public static HashMap<String, String> searchRegistry(String location, String key)
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
            ApplicationLogger.severe(ex.getMessage());
            ex.printStackTrace();
        }
        catch (InterruptedException e)
        {
            ApplicationLogger.severe(e.getMessage());
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
        private InputStream inputStream;
        private StringWriter stringWriter = new StringWriter();

        public StreamReader(InputStream is)
        {
            this.inputStream = is;
        }

        @Override
        public void run()
        {
            try
            {
                int byteOfData;
                while ((byteOfData = inputStream.read()) != -1)
                { stringWriter.write(byteOfData); }
            }
            catch (IOException e)
            {
                ApplicationLogger.severe(e.getMessage());
                e.printStackTrace();
            }
        }

        public String getResult()
        {
            return stringWriter.toString();
        }
    }
}
