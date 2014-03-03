package com.rasalhague.mdrv.RawDataProcessor;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

interface RawDataProcessingAlgorithm
{
    public ArrayList<Integer> processData(String dataToProcess);

    public ArrayList<Integer> processData(String dataToProcess, String regex, String groupToFind);
}

class DefaultDataProcessingAlgorithm implements RawDataProcessingAlgorithm
{
    final static String DEFAULT_REGEX_PATTERN = "(?<data>-\\d{2,3})";

    @Override
    public ArrayList<Integer> processData(String dataToProcess)
    {
        Pattern pattern = Pattern.compile(DEFAULT_REGEX_PATTERN);
        Matcher matcher = pattern.matcher(dataToProcess);
        ArrayList<Integer> list = new ArrayList<Integer>();
        while (matcher.find())
        {
            int itemToAdd = Integer.parseInt(matcher.group("data"));
            list.add(itemToAdd);
        }

        return list;
    }

    @Override
    public ArrayList<Integer> processData(String dataToProcess, String regex, String groupToFind)
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dataToProcess);
        ArrayList<Integer> list = new ArrayList<Integer>();
        while (matcher.find())
        {
            int itemToAdd = Integer.parseInt(matcher.group(groupToFind));
            list.add(itemToAdd);
        }

        return list;
    }
}