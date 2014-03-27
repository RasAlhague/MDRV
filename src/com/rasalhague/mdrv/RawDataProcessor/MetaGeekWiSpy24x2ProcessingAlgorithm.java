package com.rasalhague.mdrv.RawDataProcessor;

import java.util.ArrayList;

public class MetaGeekWiSpy24x2ProcessingAlgorithm implements RawDataProcessingAlgorithm
{
    @Override
    public ArrayList<Integer> processData(ArrayList<Byte> dataToProcess)
    {
        final int devPassLength = 64;
        final int serviceBytesCount = 5;
        //        System.out.println(dataToProcess.size());
        //        System.out.println(dataToProcess);

        ArrayList<Integer> clearPointArray = new ArrayList<>();
        for (int i = 0, dataToProcessSize = dataToProcess.size(); i < dataToProcessSize; i++)
        {
            Byte data = dataToProcess.get(i);
            if ((i % devPassLength) == 0)
            {
                i += serviceBytesCount;
            }
            else
            {
                clearPointArray.add(Integer.valueOf(data) - 170);
            }
        }

        //        System.out.println(clearPointArray.size());
        //        for (int i = 0; i < clearPointArray.size(); )
        //        {
        //
        //            if (i > clearPointArray.size() - devPassLength - serviceBytesCount)
        //            {
        //                clearPointArray.remove(i);
        //            }
        //            else {i++;}
        //
        //        }

        return clearPointArray;
    }
}
