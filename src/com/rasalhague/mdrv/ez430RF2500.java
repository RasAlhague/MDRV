package com.rasalhague.mdrv;

import com.rasalhague.mdrv.logging.ApplicationLogger;
import jssc.SerialPortException;

final class ez430RF2500 extends COMDeviceCommunication
{
    protected ez430RF2500(DeviceInfo deviceInfo)
    {
        super(deviceInfo);
    }

    @Override
    void initializeDevice()
    {
        byte[] intByte = new byte[]{0x7};

        try
        {
            serialPort.writeBytes(intByte);
        }
        catch (SerialPortException e)
        {
            e.printStackTrace();
        }

        ApplicationLogger.LOGGER.info("ez430RF2500 has initialized");
    }
}
