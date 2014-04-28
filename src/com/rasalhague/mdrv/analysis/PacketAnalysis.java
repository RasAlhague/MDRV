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
    private HelperAnalysisMap helperAnalysisMap = new HelperAnalysisMap();
    private long              dataPacketCounter = 0;

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
                    if (value.containsKey(deviceInfo) &&
                            value.get(deviceInfo).containsKey(AnalysisKey.MAX) &&
                            value.get(deviceInfo).containsKey(AnalysisKey.AVR))
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
                    //MAX
                    ArrayList<Integer> joinMax = joinMax(dataPacket.getDataPacketValues(),
                                                         prevResultsMap.get(AnalysisKey.MAX));

                    //AVR
                    ArrayList<Integer> joinAvr = joinAvr(dataPacket.getDataPacketValues(),
                                                         prevResultsMap.get(AnalysisKey.AVR));

                    hashMapToAdd.put(AnalysisKey.MAX, joinMax);
                    hashMapToAdd.put(AnalysisKey.AVR, joinAvr);
                }
                else
                {
                    hashMapToAdd.put(AnalysisKey.MAX, dataPacket.getDataPacketValues());
                    hashMapToAdd.put(AnalysisKey.AVR, dataPacket.getDataPacketValues());
                    hashMapToAdd.put(AnalysisKey.NEW_SERIES, null);
                }

                /**
                 * CURRENT
                 */
                hashMapToAdd.put(AnalysisKey.CURRENT, dataPacket.getDataPacketValues());

                /**
                 * Perform Analysis for MODE and MEDIAN and AVR
                 */
                //                long startAll = System.nanoTime();

                helperAnalysisMap.updateHelperMap(dataPacket);
                //                long startMode = System.nanoTime();
                hashMapToAdd.put(AnalysisKey.MODE, helperAnalysisMap.calculateMode().get(deviceInfo));
                //                long startMedian = System.nanoTime();
                hashMapToAdd.put(AnalysisKey.MEDIAN, helperAnalysisMap.calculateMedian().get(deviceInfo));

                //                long endTime = System.nanoTime();
                //                long durationAll = endTime - startAll;
                //                long durationMode = endTime - startMode;
                //                long durationMedian = endTime - startMedian;
                //                System.out.println("Packet #                        - " + dataPacketCounter);
                //                System.out.println("Mode                            - " + durationMode);
                //                System.out.println("Median                          - " + durationMedian);
                //                System.out.println("updateHelperMap + Mode + Median - " + durationAll);
                //                System.out.println("-\t-\t-\t-\t-\t-\t-\t-\t-\t-\t-");
                //                dataPacketCounter++;

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

        HashMap<DeviceInfo, ArrayList<HashMap<Integer, Integer>>> helperMap = new HashMap<>();

        Set<Long> timeKeys = timedAnalysisResults.keySet();
        for (Long timeKey : timeKeys)
        {
            Set<DeviceInfo> deviceInfoKeys = timedAnalysisResults.get(timeKey).keySet();
            for (DeviceInfo deviceInfo : deviceInfoKeys)
            {
                ArrayList<Integer> currentPacketData = timedAnalysisResults.get(timeKey)
                                                                           .get(deviceInfo)
                                                                           .get(AnalysisKey.CURRENT);
                //its null when i add other data except CURRENT -- MEDIAN MODE
                if (currentPacketData != null)
                {
                    if (!helperMap.containsKey(deviceInfo))
                    {
                        ArrayList<HashMap<Integer, Integer>> arrayList = new ArrayList<>();
                        currentPacketData.forEach(o -> arrayList.add(new HashMap<>()));
                        helperMap.put(deviceInfo, arrayList);
                    }
                    ArrayList<HashMap<Integer, Integer>> pointsHelperArray = helperMap.get(deviceInfo);
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
                }
                else
                {
                    ApplicationLogger.LOGGER.info("currentPacketData = null");
                }
            }
        }
        //        System.out.println(helperMap);

        /**
         * Set up finalMap
         */
        HashMap<DeviceInfo, ArrayList<Integer>> mode = calculateMode(helperMap);
        HashMap<DeviceInfo, ArrayList<Integer>> median = calculateMedian(helperMap);

        helperMap.keySet().forEach(deviceInfo -> {
            finalMap.put(deviceInfo, new HashMap<>());
            finalMap.get(deviceInfo).put(AnalysisKey.MODE, mode.get(deviceInfo));
            finalMap.get(deviceInfo).put(AnalysisKey.MEDIAN, median.get(deviceInfo));
        });
        //        System.out.println();
        //        System.out.println(finalMap);

        return finalMap;
    }

    private synchronized HashMap<DeviceInfo, ArrayList<Integer>> calculateMode(HashMap<DeviceInfo, ArrayList<HashMap<Integer, Integer>>> helperMap)
    {
        /**
         * MODE postprocessing
         */
        HashMap<DeviceInfo, ArrayList<Integer>> mode = new HashMap<>();

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

                mode.get(helperMapDeviceKey).add(maxRSSI);
            }
        }
        //        System.out.println(mode);

        return mode;
    }

    private synchronized HashMap<DeviceInfo, ArrayList<Integer>> calculateMedian(HashMap<DeviceInfo, ArrayList<HashMap<Integer, Integer>>> helperMap)
    {
        /**
         * MEDIAN postprocessing
         */
        HashMap<DeviceInfo, ArrayList<Integer>> median = new HashMap<>();

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
                TreeMap<Integer, Integer> helperDataPointMapTree = new TreeMap<>(helperDataPointMap);
                ArrayList<Integer> rssiSortedArray = new ArrayList<>(helperDataPointMapTree.keySet());
                int rssiSortedArraySize = rssiSortedArray.size();
                median.get(helperMapDeviceKey).add(rssiSortedArray.get(rssiSortedArraySize / 2));
            }
        }
        //        System.out.println(median);

        return median;
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

    private synchronized ArrayList<Integer> joinAvr(ArrayList<Integer> newData, ArrayList<Integer> prevData)
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
