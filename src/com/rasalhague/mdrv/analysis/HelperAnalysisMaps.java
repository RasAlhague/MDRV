package com.rasalhague.mdrv.analysis;

import com.rasalhague.mdrv.DataPacket;
import com.rasalhague.mdrv.DeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class HelperAnalysisMaps
{
    HashMap<DeviceInfo, ArrayList<HashMap<Byte, Integer>>> modeMedianHelperMap     = new HashMap<>();
    HashMap<DeviceInfo, ArrayList<Integer>>                avrRssiSumHelperMap     = new HashMap<>();
    HashMap<DeviceInfo, Integer>                           avrPacketCountHelperMap = new HashMap<>();

    public HashMap<DeviceInfo, ArrayList<HashMap<Byte, Integer>>> getModeMedianHelperMap()
    {
        return modeMedianHelperMap;
    }

    public synchronized void updateHelperMaps(DataPacket dataPacket)
    {
        updateHelperMap(dataPacket);
        updateAvrHelperMap(dataPacket);
    }

    private void updateHelperMap(DataPacket dataPacket)
    {
        DeviceInfo deviceInfo = dataPacket.getDeviceInfo();
        ArrayList<Byte> dataPacketValues = dataPacket.getDataPacketValues();

        if (!modeMedianHelperMap.containsKey(deviceInfo))
        {
            ArrayList<HashMap<Byte, Integer>> arrayList = new ArrayList<>();
            for (Byte dataPacketValue : dataPacketValues)
            {
                HashMap<Byte, Integer> pointToCountRelationMap = new HashMap<>();
                pointToCountRelationMap.put(dataPacketValue, 1);
                arrayList.add(pointToCountRelationMap);
            }

            modeMedianHelperMap.put(deviceInfo, arrayList);
        }

        modeMedianHelperMap.forEach((DeviceInfo deviceInf, ArrayList<HashMap<Byte, Integer>> helperMapForDevice) -> {

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

    private void updateAvrHelperMap(DataPacket dataPacket)
    {
        DeviceInfo deviceInfo = dataPacket.getDeviceInfo();

        //PACKET_AMOUNT
        if (avrPacketCountHelperMap.containsKey(deviceInfo))
        {
            avrPacketCountHelperMap.put(deviceInfo, avrPacketCountHelperMap.get(deviceInfo) + 1);
        }
        else
        {
            avrPacketCountHelperMap.put(deviceInfo, 1);
        }

        //RSSI_SUM
        ArrayList<Byte> dataPacketValues = dataPacket.getDataPacketValues();
        if (avrRssiSumHelperMap.containsKey(deviceInfo))
        {
            ArrayList<Integer> rssiList = avrRssiSumHelperMap.get(deviceInfo);
            for (int i = 0, dataPacketValuesSize = dataPacketValues.size(); i < dataPacketValuesSize; i++)
            {
                Byte dataPacketValue = dataPacketValues.get(i);

                rssiList.set(i, rssiList.get(i) + dataPacketValue);
            }
        }
        else
        {
            ArrayList<Integer> rssiList = new ArrayList<>();
            dataPacketValues.forEach(rssi -> rssiList.add(Integer.valueOf(rssi)));
            avrRssiSumHelperMap.put(deviceInfo, rssiList);
        }
    }

    public ArrayList<Integer> getRssiSumForDevice(DeviceInfo deviceInfo)
    {
        return avrRssiSumHelperMap.get(deviceInfo);
    }

    public int getPacketCountForDevice(DeviceInfo deviceInfo)
    {
        return avrPacketCountHelperMap.get(deviceInfo);
    }
}
