package com.rasalhague.mdrv.Utility;

import com.rasalhague.mdrv.logging.ApplicationLogger;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{
    public static String normalizePidVidToLength(String str)
    {
        String result = "";

        for (int i = 0; i < 4 - str.length(); i++)
        {
            result += "0";
        }

        result += str;

        return result;
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive. The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min
     *         Minimum value
     * @param max
     *         Maximum value.  Must be greater than min.
     *
     * @return Integer between min and max, inclusive.
     *
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max)
    {

        // Usually this should be a field rather than a method variable so
        // that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public static ArrayList<Integer> generateArrayToRound(String channelsToScan)
    {
        ArrayList<Integer> arrayToRound = new ArrayList<>();

        if (channelsToScan == null)
        {
            channelsToScan = "1-14";

            ApplicationLogger.LOGGER.warning("channelsToScan == null, channelsToScan = \"1-14\"");
        }

        Matcher matcher = Pattern.compile("(?<channelStart>\\d+)(-(?<channelEnd>\\d+))?").matcher(channelsToScan);
        while (matcher.find())
        {
            int channelStartInt = Integer.parseInt(matcher.group("channelStart"));
            String channelEnd = matcher.group("channelEnd");

            if (channelEnd != null)
            {
                int channelEndInt = Integer.parseInt(channelEnd);

                //else - when user put 14-1 unless 1-14
                if (channelStartInt <= channelEndInt)
                {
                    for (int i = channelStartInt; i <= channelEndInt; i++)
                    {
                        arrayToRound.add(i);
                    }
                }
                else
                {
                    for (int i = channelStartInt; i >= channelEndInt; i--)
                    {
                        arrayToRound.add(i);
                    }
                }
            }
            else
            {
                arrayToRound.add(channelStartInt);
            }
        }

        return arrayToRound;
    }

    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c)
    {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
    }

    public static boolean installInxi()
    {
        ArrayList<String> result = new ArrayList<>();
        try
        {
            String resultExecute;
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(new String[]{"/bin/bash", "-c", "apt-get install inxi"});
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((resultExecute = bufferedReader.readLine()) != null)
            {
                result.add(resultExecute);
                ApplicationLogger.LOGGER.info(resultExecute);
            }

            if (result.contains("Setting up inxi"))
            {
                return true;
            }
        }
        catch (IOException e)
        {
            ApplicationLogger.LOGGER.severe(Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            return false;
        }

        return false;
    }

    public static Stage prepareStageForDialog()
    {
        Stage dialogStage = new Stage();
        dialogStage.setWidth(400);
        dialogStage.setHeight(200);
        dialogStage.setX((Screen.getPrimary().getBounds().getMaxX() / 2) - 400);
        dialogStage.setY((Screen.getPrimary().getBounds().getMaxY() / 2) - 200);

        return dialogStage;
    }

    public static ArrayList<String> runShellScript(String command)
    {
        ArrayList<String> result = new ArrayList<>();
        try
        {
            String resultExecute;
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(new String[]{"/bin/bash", "-c", command});
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((resultExecute = bufferedReader.readLine()) != null)
            {
                result.add(resultExecute);
            }
            return result;
        }
        catch (IOException e)
        {
            ApplicationLogger.LOGGER.severe(Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }

        return result;
    }

    public static boolean checkInxiExist()
    {
        ArrayList<String> result = new ArrayList<>();
        try
        {
            String resultExecute;
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(new String[]{"/bin/bash", "-c", "inxi -n -c 0 -Z"});
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((resultExecute = bufferedReader.readLine()) != null)
            {
                result.add(resultExecute);
            }
        }
        catch (IOException e)
        {
            ApplicationLogger.LOGGER.severe(Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }

        return result.size() != 0;
    }

    public static BufferedReader runShellScriptBR(String command)
    {
        try
        {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(new String[]{"/bin/bash", "-c", command});

            return new BufferedReader(new InputStreamReader(process.getInputStream()));
        }
        catch (IOException e)
        {
            ApplicationLogger.LOGGER.severe(Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }

        return null;
    }

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
            ApplicationLogger.LOGGER.severe(Arrays.toString(e.getStackTrace()));
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
                    "HKEY_LOCAL_MACHINE.*USB.*VID_(?<vid>.{4})&PID_(?<pid>.{4}).*\\n *\\w* *\\w* *(?<devName>.*) ",
                    Pattern.UNIX_LINES);

            Matcher matcher = pattern.matcher(output);
            HashMap<String, String> devInfMap = new HashMap<>();
            matcher.find();
            devInfMap.put("vid", matcher.group("vid"));
            devInfMap.put("pid", matcher.group("pid"));
            devInfMap.put("devName", matcher.group("devName"));

            return devInfMap;
        }
        catch (IOException | InterruptedException ex)
        {
            ApplicationLogger.LOGGER.severe(ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }

    public static String byteArrayListToCharToString(ArrayList<Byte> byteArrayList)
    {
        //Convert ArrayList<Byte> to String
        String resultString = null;
        for (Byte byteToProcess : byteArrayList)
        {
            resultString += (char) (byteToProcess.byteValue());
        }
        return resultString;
    }

    public static boolean isSeriesExist(ObservableList<XYChart.Series<Number, Number>> lineChartData, String name)
    {
        for (XYChart.Series<Number, Number> numberSeries : lineChartData)
        {
            if (numberSeries.getName().equals(name))
            {
                return true;
            }
        }

        return false;
    }

    public static class StreamReader extends Thread
    {
        private final InputStream inputStream;
        private final StringWriter stringWriter = new StringWriter();

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
                ApplicationLogger.LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }
        }

        public String getResult()
        {
            return stringWriter.toString();
        }
    }
}
