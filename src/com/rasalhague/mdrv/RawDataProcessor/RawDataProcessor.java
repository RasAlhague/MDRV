package com.rasalhague.mdrv.RawDataProcessor;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.constants.DeviceConstants;
import com.rasalhague.mdrv.logging.ApplicationLogger;

import java.util.ArrayList;

public class RawDataProcessor implements DeviceConstants
{
    static RawDataProcessingAlgorithm rawDataProcessingAlgorithm = new DefaultDataProcessingAlgorithm();

    public static ArrayList<Integer> processData(String dataToProcess, DeviceInfo deviceInfo)
    {
        if (deviceInfo.equalsPidVid(AIR_VIEW_2_PID, AIR_VIEW_2_VID) || deviceInfo.equalsPidVid(EZ430RF2500_PID,
                                                                                               EZ430RF2500_VID))
        {
            return rawDataProcessingAlgorithm.processData(dataToProcess);
        }

        ApplicationLogger.LOGGER.warning(
                "Can not processData because device not registered, trying to use default regex.");
        return rawDataProcessingAlgorithm.processData(dataToProcess);
    }
}
