package com.rasalhague.mdrv.dev_communication;

import com.codeminders.hidapi.HIDManager;
import com.rasalhague.mdrv.device.core.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;

import java.io.IOException;
import java.util.Arrays;

/**
 * The type HID device communication.
 * <p>
 * Class realizes HID communication layer
 */
class HIDDeviceCommunication extends DeviceCommunication
{
    static
    {
        com.codeminders.hidapi.ClassPathLibraryLoader.loadNativeHIDLibrary();
    }

    /**
     * Instantiates a new HID device communication.
     *
     * @param devInfo
     *         the dev info
     */
    HIDDeviceCommunication(DeviceInfo devInfo)
    {
        super(devInfo);
    }

    @Override
    public void run()
    {
        openHIDDevice(); //open only when MANUAL_DEVICE_CONTROL = false : use initializeDevice()
        initializeDevice(); //call to specific device
        readDataFromDevice(); //MANUAL_DEVICE_CONTROL ? customReadMethod() : defaultReadMethod()
    }

    private void readDataFromDevice()
    {
        if (deviceInfo.isManualDeviceControl())
        {
            customReadMethod();
        }
        else
        {
            defaultReadMethod();
        }
    }

    private void defaultReadMethod()
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
                //for Windows
                if (e.getMessage().equals("The device is not connected."))
                {
                    break;
                }

                ApplicationLogger.LOGGER.severe(Arrays.toString(e.getStackTrace()).replace(", ", "\n"));
                e.printStackTrace();
                //for linux xD
                break;
            }
        }
    }

    private void customReadMethod()
    {
        while (true)
        {
            byte[] buffer = deviceInfo.getDevice().customReadMethod();
            if (buffer.length > 0)
            {
                rxRawDataReceiver.receiveRawData(buffer);
            }
            else
            {
                ApplicationLogger.getLogger().warning("customReadMethod() returns 0-length byte[]" +
                                                              "\n" +
                                                              "Breaking " +
                                                              deviceInfo.getFriendlyNameWithId() +
                                                              " communication thread");

                break;
            }
        }
    }

    private void openHIDDevice()
    {
        if (!deviceInfo.isManualDeviceControl())
        {
            defaultOpenMethod();
        }
    }

    private void defaultOpenMethod()
    {
        ApplicationLogger.LOGGER.info("Trying to open " + deviceInfo.getName() + " using defaultOpenMethod()");

        try
        {
            hidDevice = HIDManager.getInstance()
                                  .openById(Integer.valueOf(deviceInfo.getVendorID(), 16),
                                            Integer.valueOf(deviceInfo.getProductID(), 16),
                                            null);

            if (hidDevice == null) throw new NullPointerException("HIDManager.getInstance().openById() returns null");

            ApplicationLogger.LOGGER.info(deviceInfo.getName() + " " + "has been opened!");
        }
        catch (IOException | NullPointerException e)
        {
            ApplicationLogger.LOGGER.severe(deviceInfo.getName() + " opening Exception.");
            ApplicationLogger.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }
}
