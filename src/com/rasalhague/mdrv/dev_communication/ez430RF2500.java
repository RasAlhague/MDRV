package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import org.apache.commons.lang3.SystemUtils;

final class ez430RF2500 extends COMDeviceCommunication
{
    ez430RF2500(DeviceInfo deviceInfo)
    {
        super(deviceInfo);
    }

    @Override
    void initializeDevice() throws SerialPortException
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            final int initRetryingDelay = 500;

            try
            {
                int initRetryingCounter = 1;
                while (true)
                {
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
        }
        else if (SystemUtils.IS_OS_LINUX)
        {
            ApplicationLogger.LOGGER.info("Executing \n" +
                                                  "stty 9600 -icrnl -opost -onlcr -isig -icanon -iexten -echo -crterase -echok -echoctl -echoke -F " +
                                                  deviceInfo.getDevicePortName());

            Utils.runShellScript(
                    "stty 9600 -icrnl -opost -onlcr -isig -icanon -iexten -echo -crterase -echok -echoctl -echoke -F " +
                            deviceInfo.getDevicePortName()
            );
        }

        ApplicationLogger.LOGGER.info("ez430RF2500 has initialized");
    }
}
