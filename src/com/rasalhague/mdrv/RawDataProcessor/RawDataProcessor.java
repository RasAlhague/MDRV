package com.rasalhague.mdrv.RawDataProcessor;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.constants.DeviceConstants;

import java.util.ArrayList;

public class RawDataProcessor implements DeviceConstants
{
    static RawDataProcessingAlgorithm rawDataProcessingAlgorithm = new DefaultDataProcessingAlgorithm();

    public static ArrayList<Integer> processData(ArrayList<Byte> dataToProcess, DeviceInfo deviceInfo)
    {
        if (deviceInfo.equalsPidVid(AirView2.PID, AirView2.VID))
        {
            return rawDataProcessingAlgorithm.processData(dataToProcess);
        }

        if (deviceInfo.equalsPidVid(ez430RF2500.PID, ez430RF2500.VID))
        {
            return rawDataProcessingAlgorithm.processData(dataToProcess);
        }

        //        ApplicationLogger.LOGGER.warning(
        //                "Can not processData because device not registered, trying to use default regex.");
        //        return rawDataProcessingAlgorithm.processData(dataToProcess);

        //        ApplicationLogger.LOGGER.warning(
        //                "Can not processData because device not registered, null will be return. isAnalyzable will remain FALSE");

        return null;
    }
}
