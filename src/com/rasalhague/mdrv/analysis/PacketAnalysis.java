package com.rasalhague.mdrv.analysis;

import com.rasalhague.mdrv.DataPacket;
import com.rasalhague.mdrv.DataPacketListener;
import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Lazy singleton realization
 */
public class PacketAnalysis implements DataPacketListener
{
    private volatile HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> analysisResultsMap = new HashMap<>();

    public static PacketAnalysis getInstance()
    {
        return PacketAnalysisHolder.INSTANCE;
    }

    public synchronized HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> getAnalysisResultsMap()
    {
        return analysisResultsMap;
    }

    @Override
    public synchronized void dataPacketEvent(DataPacket dataPacket)
    {
        if (dataPacket.isAnalyzable())
        {
            final DeviceInfo deviceInfo = dataPacket.getDeviceInfo();

            /**
             * Creates schema when it is needed
             */
            if (!analysisResultsMap.containsKey(deviceInfo))
            {
                ArrayList<Integer> listToAdd = new ArrayList<>(dataPacket.getDataPacketValues());
                HashMap<AnalysisKey, ArrayList<Integer>> hashMapToAdd = new HashMap<>();
                hashMapToAdd.put(AnalysisKey.MAX, listToAdd);
                analysisResultsMap.put(deviceInfo, hashMapToAdd);
            }
            HashMap<AnalysisKey, ArrayList<Integer>> prevResultsMap = analysisResultsMap.get(deviceInfo);
            joinMax(dataPacket.getDataPacketValues(), prevResultsMap.get(AnalysisKey.MAX));

            notifyAnalysisPerformedListeners(analysisResultsMap);
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
            for (int i = 0, prevDataSize = prevData.size(); i < prevDataSize; i++)
            {
                Integer prevNumber = prevData.get(i);
                Integer newDataNumber = newData.get(i);

                if (newDataNumber > prevNumber)
                {
                    prevData.set(i, newDataNumber);
                }
            }

            return prevData;
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

    private List<AnalysisPerformedListener> analysisPerformedListeners = new ArrayList<>();

    public void addListener(AnalysisPerformedListener toAdd)
    {
        analysisPerformedListeners.add(toAdd);
    }

    private void notifyAnalysisPerformedListeners(HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> analysisResultsMap)
    {
        for (AnalysisPerformedListener listener : analysisPerformedListeners)
        {
            listener.analysisPerformedEvent(analysisResultsMap);
        }
    }

    //endregion
}
