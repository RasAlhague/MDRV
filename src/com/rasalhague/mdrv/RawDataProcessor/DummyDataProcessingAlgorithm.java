package com.rasalhague.mdrv.RawDataProcessor;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DummyDataProcessingAlgorithm implements RawDataProcessingAlgorithm
{
    private final static String DEFAULT_REGEX_PATTERN = "(?<data>-\\d{2,3})";

    @Override
    public ArrayList<Byte> processData(ArrayList<Byte> dataToProcess)
    {
        String strToProcess = dataToProcess.toString();

        Pattern pattern = Pattern.compile(DEFAULT_REGEX_PATTERN);
        Matcher matcher = pattern.matcher(strToProcess);
        ArrayList<Byte> list = new ArrayList<>();
        while (matcher.find())
        {
            Byte itemToAdd = Byte.parseByte(matcher.group("data"));
            list.add(itemToAdd);
        }

        return list;
    }
}