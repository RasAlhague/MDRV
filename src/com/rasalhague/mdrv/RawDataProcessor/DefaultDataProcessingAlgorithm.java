package com.rasalhague.mdrv.RawDataProcessor;

import com.rasalhague.mdrv.Utility.Utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DefaultDataProcessingAlgorithm implements RawDataProcessingAlgorithm
{
    private final static String DEFAULT_REGEX_PATTERN = "(?<data>-\\d{2,3})";

    @Override
    public ArrayList<Byte> processData(ArrayList<Byte> dataToProcess)
    {
        //Convert ArrayList<Byte> to String
        String strToProcess = Utils.byteArrayListToCharToString(dataToProcess);

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
