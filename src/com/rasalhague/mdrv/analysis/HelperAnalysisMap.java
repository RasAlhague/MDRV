package com.rasalhague.mdrv.analysis;

import com.rasalhague.mdrv.DataPacket;
import com.rasalhague.mdrv.DeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

public class HelperAnalysisMap
{
    HashMap<DeviceInfo, ArrayList<HashMap<Integer, Integer>>> helperMap = new HashMap<>();

    public synchronized void updateHelperMap(DataPacket dataPacket)
    {
        DeviceInfo deviceInfo = dataPacket.getDeviceInfo();
        ArrayList<Integer> dataPacketValues = dataPacket.getDataPacketValues();

        if (!helperMap.containsKey(deviceInfo))
        {
            ArrayList<HashMap<Integer, Integer>> arrayList = new ArrayList<>();
            for (Integer dataPacketValue : dataPacketValues)
            {
                HashMap<Integer, Integer> pointToCountRelationMap = new HashMap<>();
                pointToCountRelationMap.put(dataPacketValue, 1);
                arrayList.add(pointToCountRelationMap);
            }

            helperMap.put(deviceInfo, arrayList);
        }

        helperMap.forEach((DeviceInfo deviceInf, ArrayList<HashMap<Integer, Integer>> helperMapForDevice) -> {

            if (deviceInfo.equals(deviceInf))
            {
                for (int i = 0, dataPacketValuesSize = dataPacketValues.size(); i < dataPacketValuesSize; i++)
                {

                    Integer dataPacketValue = dataPacketValues.get(i);
                    HashMap<Integer, Integer> pointToCountRelationMap = helperMapForDevice.get(i);

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

    public synchronized HashMap<DeviceInfo, ArrayList<Integer>> calculateMode()
    {
        /**
         * MODE postprocessing
         */
        HashMap<DeviceInfo, ArrayList<Integer>> mode = new HashMap<>();
        int maxRSSICountValue;
        int maxRSSI;

        Set<DeviceInfo> helperMapDeviceKeys = helperMap.keySet();
        for (DeviceInfo helperMapDeviceKey : helperMapDeviceKeys)
        {
            if (!mode.containsKey(helperMapDeviceKey))
            {
                mode.put(helperMapDeviceKey, new ArrayList<>());
            }

            ArrayList<HashMap<Integer, Integer>> pointsHelperArray = helperMap.get(helperMapDeviceKey);
            for (HashMap<Integer, Integer> helperDataPointMap : pointsHelperArray)
            {
                maxRSSICountValue = 0;
                maxRSSI = 0;

                Set<Integer> rssiKeys = helperDataPointMap.keySet();
                for (Integer helperDataPointMapKey : rssiKeys)
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

    public synchronized HashMap<DeviceInfo, ArrayList<Integer>> calculateMedian()
    {
        /**
         * MEDIAN postprocessing
         */
        HashMap<DeviceInfo, ArrayList<Integer>> median = new HashMap<>();
        int rssiSortedArraySize;

        Set<DeviceInfo> helperMapDeviceKeys = helperMap.keySet();
        for (DeviceInfo helperMapDeviceKey : helperMapDeviceKeys)
        {
            if (!median.containsKey(helperMapDeviceKey))
            {
                median.put(helperMapDeviceKey, new ArrayList<>());
            }

            ArrayList<HashMap<Integer, Integer>> pointsHelperArray = helperMap.get(helperMapDeviceKey);
            for (HashMap<Integer, Integer> helperDataPointMap : pointsHelperArray)
            {
                ArrayList<Integer> rssiSortedArray = new ArrayList<>(new TreeMap<>(helperDataPointMap).keySet());
                rssiSortedArraySize = rssiSortedArray.size();
                median.get(helperMapDeviceKey).add(rssiSortedArray.get(rssiSortedArraySize / 2));
            }
        }

        return median;
    }
}
