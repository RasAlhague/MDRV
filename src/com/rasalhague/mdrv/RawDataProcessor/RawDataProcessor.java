package com.rasalhague.mdrv.RawDataProcessor;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.constants.DeviceConstants;

import java.util.ArrayList;

public class RawDataProcessor
{
    private static volatile RawDataProcessingAlgorithm rawDataProcessingAlgorithm;

    public synchronized static ArrayList<Byte> processData(ArrayList<Byte> dataToProcess, DeviceInfo deviceInfo)
    {
        //TODO Hardcoded
        if (deviceInfo.equalsPidVid(DeviceConstants.AirView2.PID, DeviceConstants.AirView2.VID))
        {
            rawDataProcessingAlgorithm = new DefaultDataProcessingAlgorithm();
            return rawDataProcessingAlgorithm.processData(dataToProcess);
        }

        if (deviceInfo.equalsPidVid(DeviceConstants.ez430RF2500.PID, DeviceConstants.ez430RF2500.VID))
        {
            rawDataProcessingAlgorithm = new DefaultDataProcessingAlgorithm();
            return rawDataProcessingAlgorithm.processData(dataToProcess);
        }

        if (deviceInfo.equalsPidVid(DeviceConstants.UnigenISMSniffer.PID, DeviceConstants.UnigenISMSniffer.VID))
        {
            rawDataProcessingAlgorithm = new UnigenISMSnifferProcessingAlgorithm();
            return rawDataProcessingAlgorithm.processData(dataToProcess);
        }

        if (deviceInfo.equalsPidVid(DeviceConstants.MetaGeek_WiSpy24x2.PID, DeviceConstants.MetaGeek_WiSpy24x2.VID))
        {
            rawDataProcessingAlgorithm = new MetaGeekWiSpy24x2ProcessingAlgorithm();
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
