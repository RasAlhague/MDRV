package com.rasalhague.mdrv.devices;

import org.apache.commons.lang3.SystemUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WixelPololu extends Device
{
    public final static String FRIENDLY_NAME       = "Wixel Pololu";
    public final static String VENDOR_ID           = "1FFB";
    public final static String PRODUCT_ID          = "2200";
    public final static float  INITIAL_FREQUENCY   = 2400;
    public final static float  CHANNEL_SPACING     = 327.450980f;
    public final static byte[] END_PACKET_SEQUENCE = new byte[]{10};

    @Override
    public void initializeDevice()
    {

    }

    @Override
    public ArrayList<Byte> parse(ArrayList<Byte> dataToParse)
    {
        if (SystemUtils.IS_OS_LINUX)
        {
            ArrayList<Byte> result = new ArrayList<>();
            String tempString = "";

            for (Byte aByte : dataToParse)
            {
                tempString += (char) (aByte.byteValue());
            }

            Pattern pattern = Pattern.compile("(?<data>-\\d{2,3})");
            Matcher matcher = pattern.matcher(tempString);

            while (matcher.find())
            {
                Byte itemToAdd = Byte.parseByte(matcher.group("data"));
                result.add(itemToAdd);
            }

            return result;
        }

        return dataToParse;
    }
}
