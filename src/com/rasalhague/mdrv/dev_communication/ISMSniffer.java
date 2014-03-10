package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;

public class ISMSniffer extends HIDDeviceCommunication
{
    protected ISMSniffer(DeviceInfo deviceInfo) {super(deviceInfo);}

    @Override
    void initializeDevice()
    {
        ApplicationLogger.LOGGER.info("ISMSniffer has initialized.");
    }
}
