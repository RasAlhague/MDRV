package com.rasalhague.mdrv.RawDataProcessor;

import java.util.ArrayList;

interface RawDataProcessingAlgorithm
{
    public ArrayList<Byte> processData(ArrayList<Byte> dataToProcess);
}
