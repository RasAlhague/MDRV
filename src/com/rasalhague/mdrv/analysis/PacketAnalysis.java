package com.rasalhague.mdrv.analysis;

import com.rasalhague.mdrv.DataPacket;
import com.rasalhague.mdrv.DataPacketListener;
import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Lazy singleton realization
 */
public class PacketAnalysis implements DataPacketListener
{
    private volatile LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>>> timedAnalysisResults = new LinkedHashMap<>();

    public static PacketAnalysis getInstance()
    {
        return PacketAnalysisHolder.INSTANCE;
    }

    public LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>>> getTimedAnalysisResults()
    {
        return timedAnalysisResults;
    }

    /**
     * countering ConcurrentModificationException
     *
     * @return
     */
    public synchronized LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>>> getTimedAnalysisResultsClone()
    {
        return new LinkedHashMap<>(timedAnalysisResults);
    }

    @Override
    public synchronized void dataPacketEvent(DataPacket dataPacket)
    {
        if (dataPacket.isAnalyzable())
        {
            final DeviceInfo deviceInfo = dataPacket.getDeviceInfo();
            final long packetCreationTimeMs = dataPacket.getPacketCreationTimeMs();

            //search for last <AnalysisKey> for specific dev
            HashMap<AnalysisKey, ArrayList<Integer>> prevResultsMap = null;
            ArrayList<HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>>> list = new ArrayList<>(
                    timedAnalysisResults.values());
            for (int i = list.size() - 1; i >= 0; i--)
            {
                HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> value = list.get(i);
                if (value.containsKey(deviceInfo))
                {
                    prevResultsMap = value.get(deviceInfo);
                    break;
                }
            }

            //generate scheme and put <AnalysisKey> into
            HashMap<AnalysisKey, ArrayList<Integer>> hashMapToAdd = new HashMap<>();
            if (prevResultsMap != null)
            {
                hashMapToAdd.put(AnalysisKey.MAX,
                                 joinMax(dataPacket.getDataPacketValues(), prevResultsMap.get(AnalysisKey.MAX)));
            }
            else
            {
                hashMapToAdd.put(AnalysisKey.MAX, new ArrayList<>(dataPacket.getDataPacketValues()));
                hashMapToAdd.put(AnalysisKey.NEW_SERIES, null);
            }

            /**
             * if packetCreationTimeMs does not exist, timedAnalysisResults will created
             * else - it will be just updated
             */
            if (!timedAnalysisResults.containsKey(packetCreationTimeMs))
            {
                HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> map = new HashMap<>();
                map.put(deviceInfo, hashMapToAdd);

                timedAnalysisResults.put(packetCreationTimeMs, map);
            }
            else
            {
                HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> map1 = timedAnalysisResults.get(
                        packetCreationTimeMs);

                map1.put(deviceInfo, hashMapToAdd);
            }

            //            System.out.println(timedAnalysisResults);
            notifyAnalysisPerformedListeners(getTimedAnalysisResultsClone());
        }
    }

    /**
     * This method join new data to second parameter prevData
     *
     * @param newData
     * @param prevData
     *         will be changed
     *
     * @return
     */
    private synchronized ArrayList<Integer> joinMax(ArrayList<Integer> newData, ArrayList<Integer> prevData)
    {
        if (newData.size() == prevData.size())
        {
            ArrayList<Integer> joinedData = new ArrayList<>(prevData);

            for (int i = 0, prevDataSize = prevData.size(); i < prevDataSize; i++)
            {
                Integer prevNumber = prevData.get(i);
                Integer newDataNumber = newData.get(i);

                if (newDataNumber > prevNumber)
                {
                    joinedData.set(i, newDataNumber);
                }
            }

            return joinedData;
        }
        else
        {
            ApplicationLogger.LOGGER.severe("newData.size() != prevData.size(); can not process");
            return null;
        }
    }

    private static class PacketAnalysisHolder
    {
        public static final PacketAnalysis INSTANCE = new PacketAnalysis();
    }

    //region Observer implementation

    private final List<AnalysisPerformedListener> analysisPerformedListeners = new ArrayList<>();

    public void addListener(AnalysisPerformedListener toAdd)
    {
        analysisPerformedListeners.add(toAdd);
    }

    private void notifyAnalysisPerformedListeners(LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>>> analysisResultsMap)
    {
        for (AnalysisPerformedListener listener : analysisPerformedListeners)
        {
            listener.analysisPerformedEvent(analysisResultsMap);
        }
    }

    //endregion
}
