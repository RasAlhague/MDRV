package com.rasalhague.mdrv.analysis;

import com.rasalhague.mdrv.DataPacket;
import com.rasalhague.mdrv.DeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

public class HelperAnalysisMap
{
    HashMap<DeviceInfo, ArrayList<HashMap<Byte, Integer>>> helperMap = new HashMap<>();

    public synchronized void updateHelperMap(DataPacket dataPacket)
    {
        DeviceInfo deviceInfo = dataPacket.getDeviceInfo();
        ArrayList<Byte> dataPacketValues = dataPacket.getDataPacketValues();

        if (!helperMap.containsKey(deviceInfo))
        {
            ArrayList<HashMap<Byte, Integer>> arrayList = new ArrayList<>();
            for (Byte dataPacketValue : dataPacketValues)
            {
                HashMap<Byte, Integer> pointToCountRelationMap = new HashMap<>();
                pointToCountRelationMap.put(dataPacketValue, 1);
                arrayList.add(pointToCountRelationMap);
            }

            helperMap.put(deviceInfo, arrayList);
        }

        helperMap.forEach((DeviceInfo deviceInf, ArrayList<HashMap<Byte, Integer>> helperMapForDevice) -> {

            if (deviceInfo.equals(deviceInf))
            {
                for (int i = 0, dataPacketValuesSize = dataPacketValues.size(); i < dataPacketValuesSize; i++)
                {

                    Byte dataPacketValue = dataPacketValues.get(i);
                    HashMap<Byte, Integer> pointToCountRelationMap = helperMapForDevice.get(i);

                    if (pointToCountRelationMap.containsKey(dataPacketValue))
                    {
                        pointToCountRelationMap.put(dataPacketValue, pointToCountRelationMap.get(dataPacketValue) + 1);
                    }
                    else
                    {
                        pointToCountRelationMap.put(dataPacketValue, 1);
                    }
                }
            }

        });
    }

    public synchronized HashMap<DeviceInfo, ArrayList<Byte>> calculateMode()
    {
        /**
         * MODE postprocessing
         */
        HashMap<DeviceInfo, ArrayList<Byte>> mode = new HashMap<>();
        int maxRSSICountValue;
        byte maxRSSI;

        Set<DeviceInfo> helperMapDeviceKeys = helperMap.keySet();
        for (DeviceInfo helperMapDeviceKey : helperMapDeviceKeys)
        {
            if (!mode.containsKey(helperMapDeviceKey))
            {
                mode.put(helperMapDeviceKey, new ArrayList<>());
            }

            ArrayList<HashMap<Byte, Integer>> pointsHelperArray = helperMap.get(helperMapDeviceKey);
            for (HashMap<Byte, Integer> helperDataPointMap : pointsHelperArray)
            {
                maxRSSICountValue = 0;
                maxRSSI = 0;

                Set<Byte> rssiKeys = helperDataPointMap.keySet();
                for (Byte helperDataPointMapKey : rssiKeys)
                {
                    if (helperDataPointMap.get(helperDataPointMapKey) > maxRSSICountValue || maxRSSICountValue == 0)
                    {
                        maxRSSICountValue = helperDataPointMap.get(helperDataPointMapKey);
                        maxRSSI = helperDataPointMapKey;
                    }
                }

                mode.get(helperMapDeviceKey).add(maxRSSI);
            }
        }

        return mode;
    }

    public synchronized HashMap<DeviceInfo, ArrayList<Byte>> calculateMedian()
    {
        /**
         * MEDIAN postprocessing
         */
        HashMap<DeviceInfo, ArrayList<Byte>> median = new HashMap<>();
        int rssiSortedArraySize;

        Set<DeviceInfo> helperMapDeviceKeys = helperMap.keySet();
        for (DeviceInfo helperMapDeviceKey : helperMapDeviceKeys)
        {
            if (!median.containsKey(helperMapDeviceKey))
            {
                median.put(helperMapDeviceKey, new ArrayList<>());
            }

            ArrayList<HashMap<Byte, Integer>> pointsHelperArray = helperMap.get(helperMapDeviceKey);
            for (HashMap<Byte, Integer> helperDataPointMap : pointsHelperArray)
            {
                ArrayList<Byte> rssiSortedArray = new ArrayList<>(new TreeMap<>(helperDataPointMap).keySet());
                rssiSortedArraySize = rssiSortedArray.size();
                median.get(helperMapDeviceKey).add(rssiSortedArray.get(rssiSortedArraySize / 2));
            }
        }

        return median;
    }
}
