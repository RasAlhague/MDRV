package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;

class ISMSniffer extends HIDDeviceCommunication
{
    ISMSniffer(DeviceInfo deviceInfo) {super(deviceInfo);}

    @Override
    void initializeDevice()
    {
        ApplicationLogger.LOGGER.info("ISMSniffer has initialized.");
    }
}
