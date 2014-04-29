package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

final class ez430RF2500 extends COMDeviceCommunication
{
    ez430RF2500(DeviceInfo deviceInfo)
    {
        super(deviceInfo);
    }

    @Override
    void initializeDevice()
    {
        final int initRetryingDelay = 500;

        try
        {
            int initRetryingCounter = 1;
            while (true)
            {
                //TODO do not work on linux (Ubuntu)
                ApplicationLogger.LOGGER.info(initRetryingCounter++ + " try to init ez430RF2500");

                serialPort.setParams(SerialPort.BAUDRATE_9600,
                                     SerialPort.DATABITS_8,
                                     SerialPort.STOPBITS_1,
                                     SerialPort.PARITY_NONE);

                try
                {
                    String s = serialPort.readString(1, initRetryingDelay);
                    //                    System.out.println(s);
                    break;
                }
                catch (SerialPortTimeoutException ignored)
                {
                    //                    System.out.println(ignored.getMessage());
                }
            }
        }
        catch (SerialPortException e)
        {
            ApplicationLogger.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }

        ApplicationLogger.LOGGER.info("ez430RF2500 has initialized");
    }
}
