package com.rasalhague.mdrv.analysis;

import com.rasalhague.mdrv.DataPacket;
import com.rasalhague.mdrv.DataPacketListener;
import com.rasalhague.mdrv.device.core.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;

import java.util.*;

/**
 * Lazy singleton realization
 */
public class PacketAnalysis implements DataPacketListener
{
    private volatile LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>>> timedAnalysisResults = new LinkedHashMap<>();
    private final    HelperAnalysisMaps                                                              helperAnalysisMaps   = new HelperAnalysisMaps();

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
    public LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>>> getTimedAnalysisResults()
    {
        return timedAnalysisResults;
    }

    @Override
    public synchronized void dataPacketEvent(DataPacket dataPacket)
    {
        if (dataPacket.isAnalyzable())
        {
            final DeviceInfo deviceInfo = dataPacket.getDeviceInfo();
            final long packetCreationTimeMs = dataPacket.getPacketCreationTimeMs();

            //search for last <AnalysisKey> for specific dev
            HashMap<AnalysisKey, ArrayList<Byte>> prevResultsMap = null;
            ArrayList<HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>>> list = new ArrayList<>(
                    timedAnalysisResults.values());
            for (int i = list.size() - 1; i >= 0; i--)
            {
                HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>> value = list.get(i);
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
            HashMap<AnalysisKey, ArrayList<Byte>> hashMapToAdd = new HashMap<>();
            if (prevResultsMap != null)
            {
                //MAX
                ArrayList<Byte> joinMax = joinMax(dataPacket.getDataPacketValues(),
                                                  prevResultsMap.get(AnalysisKey.MAX));

                //AVR
                ArrayList<Byte> joinAvr = joinAvr(dataPacket.getDataPacketValues(),
                                                  helperAnalysisMaps.getPacketCountForDevice(deviceInfo),
                                                  helperAnalysisMaps.getRssiSumForDevice(deviceInfo));

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
            helperAnalysisMaps.updateHelperMaps(dataPacket);
            hashMapToAdd.put(AnalysisKey.MODE, calculateMode(helperAnalysisMaps, deviceInfo));
            hashMapToAdd.put(AnalysisKey.MEDIAN, calculateMedian(helperAnalysisMaps, deviceInfo));

            /**
             * When we have 2 or > DataPacket in one moment
             * if packetCreationTimeMs does not exist, timedAnalysisResults will created
             * else - it will be just updated
             */
            if (!timedAnalysisResults.containsKey(packetCreationTimeMs))
            {
                HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>> map = new HashMap<>();
                map.put(deviceInfo, hashMapToAdd);

                timedAnalysisResults.put(packetCreationTimeMs, map);
            }
            else
            {
                HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>> map1 = timedAnalysisResults.get(
                        packetCreationTimeMs);

                map1.put(deviceInfo, hashMapToAdd);
            }

            //            System.out.println(timedAnalysisResults);
            notifyAnalysisPerformedListeners(getTimedAnalysisResults());
        }
    }

    /**
     * Calculate mode.
     * <p>
     * For concrete device
     *
     * @param helperAnalysisMaps
     *         the helper analysis maps
     * @param deviceInfo
     *         the device info
     *
     * @return the array list
     */
    synchronized ArrayList<Byte> calculateMode(HelperAnalysisMaps helperAnalysisMaps, DeviceInfo deviceInfo)
    {
        /**
         * MODE postprocessing
         */
        ArrayList<Byte> mode = new ArrayList<>();
        HashMap<DeviceInfo, ArrayList<HashMap<Byte, Integer>>> helperMap = helperAnalysisMaps.getModeMedianHelperMap();
        int maxRSSICountValue;
        byte maxRSSI;

        Set<DeviceInfo> helperMapDeviceKeys = helperMap.keySet();
        for (DeviceInfo helperMapDeviceKey : helperMapDeviceKeys)
        {
            if (helperMapDeviceKey.equals(deviceInfo))
            {
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

                    mode.add(maxRSSI);
                }
            }
        }

        return mode;
    }

    /**
     * Calculate median.
     * <p>
     * For concrete device
     *
     * @param helperAnalysisMaps
     *         the helper analysis maps
     * @param deviceInfo
     *         the device info
     *
     * @return the array list
     */
    synchronized ArrayList<Byte> calculateMedian(HelperAnalysisMaps helperAnalysisMaps, DeviceInfo deviceInfo)
    {
        /**
         * MEDIAN postprocessing
         */
        ArrayList<Byte> median = new ArrayList<>();
        HashMap<DeviceInfo, ArrayList<HashMap<Byte, Integer>>> helperMap = helperAnalysisMaps.getModeMedianHelperMap();
        int rssiSortedArraySize;

        Set<DeviceInfo> helperMapDeviceKeys = helperMap.keySet();
        for (DeviceInfo helperMapDeviceKey : helperMapDeviceKeys)
        {
            if (helperMapDeviceKey.equals(deviceInfo))
            {
                ArrayList<HashMap<Byte, Integer>> pointsHelperArray = helperMap.get(helperMapDeviceKey);
                for (HashMap<Byte, Integer> helperDataPointMap : pointsHelperArray)
                {
                    ArrayList<Byte> rssiSortedArray = new ArrayList<>(new TreeMap<>(helperDataPointMap).keySet());
                    rssiSortedArraySize = rssiSortedArray.size();
                    median.add(rssiSortedArray.get(rssiSortedArraySize / 2));
                }
            }
        }

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
    private synchronized ArrayList<Byte> joinMax(ArrayList<Byte> newData, ArrayList<Byte> prevData)
    {
        if (newData.size() == prevData.size())
        {
            ArrayList<Byte> joinedData = new ArrayList<>(prevData);
            byte prevNumber;
            byte newDataNumber;

            for (int i = 0, prevDataSize = prevData.size(); i < prevDataSize; i++)
            {
                prevNumber = prevData.get(i);
                newDataNumber = newData.get(i);

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

    private synchronized ArrayList<Byte> joinAvr(ArrayList<Byte> newData,
                                                 Integer packetsAmount,
                                                 ArrayList<Integer> rssiSumForDevice)
    {
        ArrayList<Byte> avrArray = new ArrayList<>();
        if (newData.size() == rssiSumForDevice.size())
        {
            int newRssi;
            int sumRssi;
            for (int i = 0, newDataSize = newData.size(); i < newDataSize; i++)
            {
                newRssi = newData.get(i);
                sumRssi = rssiSumForDevice.get(i);

                avrArray.add((byte) ((sumRssi + newRssi) / (packetsAmount + 1)));
            }
        }
        else
        {
            ApplicationLogger.LOGGER.severe("newData.size() != prevData.size(); can not process. Returning prevData");
            ApplicationLogger.LOGGER.info("rssiSumForDevice " + rssiSumForDevice.size());
            ApplicationLogger.LOGGER.info("newData " + newData.size());
        }

        return avrArray;
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

    private void notifyAnalysisPerformedListeners(LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>>> analysisResultsMap)
    {
        for (AnalysisPerformedListener listener : analysisPerformedListeners)
        {
            listener.analysisPerformedEvent(analysisResultsMap);
        }
    }

    //endregion
}
