package com.rasalhague.mdrv.analysis;

import com.rasalhague.mdrv.DataPacket;
import com.rasalhague.mdrv.DataPacketListener;
import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;

import java.util.*;

/**
 * Lazy singleton realization
 */
public class PacketAnalysis implements DataPacketListener
{
    private volatile LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>>> timedAnalysisResults = new LinkedHashMap<>();

    public boolean isAnalysisOn()
    {
        return isAnalysisOn;
    }

    public void setAnalysisOn(boolean isAnalysisOn)
    {
        this.isAnalysisOn = isAnalysisOn;
    }

    private boolean isAnalysisOn = true;

    /**
     * Gets instance.
     * <p>
     * Singleton realisation.
     *
     * @return the instance
     */
    public static PacketAnalysis getInstance()
    {
        return PacketAnalysisHolder.INSTANCE;
    }

    /**
     * Gets timed analysis results.
     * <p>
     * Returns LinkedHashMap with final analysis results.
     *
     * @return the timed analysis results
     */
    public LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>>> getTimedAnalysisResults()
    {
        return timedAnalysisResults;
    }

    @Override
    public synchronized void dataPacketEvent(DataPacket dataPacket)
    {
        if (isAnalysisOn)
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
                    if (value.containsKey(deviceInfo) && value.get(deviceInfo).containsKey(AnalysisKey.MAX))
                    {
                        prevResultsMap = value.get(deviceInfo);
                        break;
                    }
                }

                /**
                 * generate scheme and put <AnalysisKey> into
                 */
                HashMap<AnalysisKey, ArrayList<Integer>> hashMapToAdd = new HashMap<>();
                if (prevResultsMap != null)
                {
                    /**
                     * MAX
                     */
                    //                    if (prevResultsMap.containsKey(AnalysisKey.MAX))
                    //                    {
                    ArrayList<Integer> joinMax = joinMax(dataPacket.getDataPacketValues(),
                                                         prevResultsMap.get(AnalysisKey.MAX));

                    hashMapToAdd.put(AnalysisKey.MAX, joinMax);
                    //                    }
                }
                else
                {
                    hashMapToAdd.put(AnalysisKey.MAX, dataPacket.getDataPacketValues());
                    hashMapToAdd.put(AnalysisKey.NEW_SERIES, null);
                }

                /**
                 * CURRENT
                 */
                hashMapToAdd.put(AnalysisKey.CURRENT, dataPacket.getDataPacketValues());

                /**
                 * When we have 2 or > DataPacket in one moment
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
                notifyAnalysisPerformedListeners(getTimedAnalysisResults());
            }
        }
    }

    public void analyseAndSaveCollectedData()
    {
        setAnalysisOn(false);

        HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> analysedCollectedData = analyseCollectedData();

        timedAnalysisResults.put(new Date().getTime(), analysedCollectedData);
        notifyAnalysisPerformedListeners(getTimedAnalysisResults());

        saveAnalysedCollectedData(analysedCollectedData);

        setAnalysisOn(true);
    }

    private void saveAnalysedCollectedData(HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> analysedCollectedData)
    {

    }

    private HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> analyseCollectedData()
    {
        HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> finalMap = new HashMap<>();

        HashMap<DeviceInfo, ArrayList<HashMap<Integer, Integer>>> modeHelperMap = new HashMap<>();

        Set<Long> timeKeys = timedAnalysisResults.keySet();
        for (Long timeKey : timeKeys)
        {
            Set<DeviceInfo> deviceInfoKeys = timedAnalysisResults.get(timeKey).keySet();
            for (DeviceInfo deviceInfo : deviceInfoKeys)
            {
                /**
                 * MODE
                 */
                ArrayList<Integer> currentPacketData = timedAnalysisResults.get(timeKey)
                                                                           .get(deviceInfo)
                                                                           .get(AnalysisKey.CURRENT);
                if (currentPacketData != null)
                {
                    if (!modeHelperMap.containsKey(deviceInfo))
                    {
                        ArrayList<HashMap<Integer, Integer>> arrayList = new ArrayList<>();
                        currentPacketData.forEach(o -> arrayList.add(new HashMap<>()));
                        modeHelperMap.put(deviceInfo, arrayList);
                    }
                    ArrayList<HashMap<Integer, Integer>> pointsHelperArray = modeHelperMap.get(deviceInfo);
                    for (int i = 0, currentPacketDataSize = currentPacketData.size(); i < currentPacketDataSize; i++)
                    {
                        Integer currentDataPoint = currentPacketData.get(i);
                        HashMap<Integer, Integer> helperDataPointMap = pointsHelperArray.get(i);

                        if (helperDataPointMap.containsKey(currentDataPoint))
                        {
                            helperDataPointMap.put(currentDataPoint, helperDataPointMap.get(currentDataPoint) + 1);
                        }
                        else
                        {
                            helperDataPointMap.put(currentDataPoint, 1);
                        }
                    }

                    /**
                     * AVR
                     */
                }
                else
                {
                    ApplicationLogger.LOGGER.info("currentPacketData = null");
                }
            }
        }
        //        System.out.println(modeHelperMap);

        /**
         * MODE postprocessing
         */
        HashMap<DeviceInfo, ArrayList<Integer>> mode = new HashMap<>();

        Set<DeviceInfo> modeHelperMapDeviceKeys = modeHelperMap.keySet();
        for (DeviceInfo modeHelperMapDeviceKey : modeHelperMapDeviceKeys)
        {
            if (!mode.containsKey(modeHelperMapDeviceKey))
            {
                mode.put(modeHelperMapDeviceKey, new ArrayList<>());
            }

            ArrayList<HashMap<Integer, Integer>> pointsHelperArray = modeHelperMap.get(modeHelperMapDeviceKey);
            for (HashMap<Integer, Integer> helperDataPointMap : pointsHelperArray)
            {
                int maxRSSICountValue = 0;
                int maxRSSI = 0;

                Set<Integer> rssiKeys = helperDataPointMap.keySet();
                for (Integer helperDataPointMapKey : rssiKeys)
                {
                    if (helperDataPointMap.get(helperDataPointMapKey) > maxRSSICountValue || maxRSSICountValue == 0)
                    {
                        maxRSSICountValue = helperDataPointMap.get(helperDataPointMapKey);
                        maxRSSI = helperDataPointMapKey;
                    }
                }

                mode.get(modeHelperMapDeviceKey).add(maxRSSI);
            }

        }
        //        System.out.println(mode);

        /**
         * Set up finalMap
         */
        modeHelperMapDeviceKeys.forEach(deviceInfo -> {
            finalMap.put(deviceInfo, new HashMap<>());
            finalMap.get(deviceInfo).put(AnalysisKey.MODE, mode.get(deviceInfo));
        });
        //        System.out.println();
        //        System.out.println(finalMap);

        return finalMap;
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
            ApplicationLogger.LOGGER.severe("newData.size() != prevData.size(); can not process. Returning prevData");
            ApplicationLogger.LOGGER.info("prevData" + prevData.size());
            ApplicationLogger.LOGGER.info("newData" + newData.size());

            return prevData;
        }
    }

    private static class PacketAnalysisHolder
    {
        /**
         * The constant INSTANCE.
         */
        public static final PacketAnalysis INSTANCE = new PacketAnalysis();
    }

    //region Observer implementation

    private final List<AnalysisPerformedListener> analysisPerformedListeners = new ArrayList<>();

    /**
     * Add listener.
     *
     * @param toAdd
     *         the to add
     */
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
