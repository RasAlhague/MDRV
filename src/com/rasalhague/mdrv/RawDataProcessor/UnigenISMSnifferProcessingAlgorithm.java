package com.rasalhague.mdrv.RawDataProcessor;

import java.util.ArrayList;

public class UnigenISMSnifferProcessingAlgorithm implements RawDataProcessingAlgorithm
{
    @Override
    public ArrayList<Integer> processData(ArrayList<Byte> dataToProcess)
    {
        ArrayList<Byte> arrayList = new ArrayList<>();
        byte byteCounter = 0;
        for (int i = 0; i < dataToProcess.size(); )
        {
            if (dataToProcess.get(i) == byteCounter)
            {
                if (dataToProcess.get(i + 1) == byteCounter)
                {
                    arrayList.add(dataToProcess.get(i + 2));
                    byteCounter++;
                    i += 3;
                }
                else
                {
                    i++;
                }
            }
            else
            {
                i++;
            }
        }

        ArrayList<Integer> finalArrayList = new ArrayList<>();
        for (Byte aByte : arrayList)
        {
            //$frequency[$i] = ((hexdec($frequency_temp[3*$i]) - 254) + 82) * 1.428 - 97;
            //            finalArrayList.add((aByte - 135));
            finalArrayList.add((int) ((((aByte - 135) + 100) * 1.428) - 100));
        }

        return finalArrayList;
    }
}
