package com.rasalhague.mdrv.dev_communication;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;
import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;

import java.io.IOException;

public class HIDDeviceCommunication extends DeviceCommunication
{
    HIDDevice hidDevice;

    static
    {
        com.codeminders.hidapi.ClassPathLibraryLoader.loadNativeHIDLibrary();
    }

    HIDDeviceCommunication(DeviceInfo devInfo)
    {
        super(devInfo);
    }

    @Override
    public void run()
    {
        openHIDDevice();
        initializeDevice();
        readDataFromDevice();
    }

    private void readDataFromDevice()
    {
        /**
         * hidDevice.read() reads 1 message necessarily, no more no less.
         */
        while (true)
        {
            try
            {
                byte[] buffer = new byte[128];
                int bytesRead = hidDevice.read(buffer);
                byte[] cattedBuffer = new byte[bytesRead];
                System.arraycopy(buffer, 0, cattedBuffer, 0, bytesRead);
                rxRawDataReceiver.receiveRawData(cattedBuffer);
            }
            catch (IOException e)
            {
                if (e.getMessage().equals("The device is not connected."))
                {
                    break;
                }
                ApplicationLogger.LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void openHIDDevice()
    {
        ApplicationLogger.LOGGER.info("Trying to open " + deviceInfo.getDeviceName());

        try
        {
            hidDevice = HIDManager.getInstance()
                                  .openById(Integer.valueOf(deviceInfo.getDeviceVid(), 16),
                                            Integer.valueOf(deviceInfo.getDevicePid(), 16),
                                            null);

            ApplicationLogger.LOGGER.info(deviceInfo.getDeviceName() + " " + "has been opened!");
        }
        catch (IOException e)
        {
            ApplicationLogger.LOGGER.severe(deviceInfo.getDeviceName() + " " + "opening IOException.");
            ApplicationLogger.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    void initializeDevice()
    {
        ApplicationLogger.LOGGER.warning(
                "Device not specified. Can not choose right init sequence. Initialization ignored.");
    }
}
