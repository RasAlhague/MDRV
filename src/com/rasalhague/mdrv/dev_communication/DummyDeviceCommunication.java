package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.logging.ApplicationLogger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DummyDeviceCommunication extends DeviceCommunication
{
    private int transfersCount = 0;

    /**
     * Instantiates a new Device communication.
     *
     * @param devInfo
     *         the dev info
     */
    DummyDeviceCommunication(DeviceInfo devInfo)
    {
        super(devInfo);
    }

    @Override
    public void run()
    {
        initializeDevice();
        startGeneratingRSSI();
    }

    private void startGeneratingRSSI()
    {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {

            if (transfersCount >= 173)
            {
                rxRawDataReceiver.receiveRawData(deviceInfo.getEndPacketSequence());

                transfersCount = 0;
            }
            else
            {
                byte[] dummyData = new byte[]{(byte) (Utils.randInt(40, 100) * (-1))};

                rxRawDataReceiver.receiveRawData(new byte[]{(byte) (Utils.randInt(40, 100) * (-1))});

                transfersCount = transfersCount + dummyData.length;
            }

        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    void initializeDevice()
    {
        ApplicationLogger.LOGGER.warning(deviceInfo.getName() + " has initialized.");
    }
}