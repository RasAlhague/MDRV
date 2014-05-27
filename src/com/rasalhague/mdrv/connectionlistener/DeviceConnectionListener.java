package com.rasalhague.mdrv.connectionlistener;

import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;
import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.analysis.PacketAnalysis;
import com.rasalhague.mdrv.dev_communication.DeviceCommunication;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import com.rasalhague.mdrv.logging.PacketLogger;
import jssc.SerialPortList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The type Device connection listener.
 * <p>
 * Singleton. Listen for a new devices connections.
 */
public class DeviceConnectionListener implements DeviceConnectionListenerI
{
    private final ArrayList<DeviceInfo>           connectedDeviceList = new ArrayList<>();
    private final List<DeviceConnectionListenerI> listeners           = new ArrayList<>();
    private final long                            scanTimerPeriodMs   = 1000;
    private       boolean                         isListening         = false;
    private Timer timer;

    private static final DeviceConnectionListener instance = new DeviceConnectionListener();

    static
    {
        com.codeminders.hidapi.ClassPathLibraryLoader.loadNativeHIDLibrary();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static DeviceConnectionListener getInstance()
    {
        return instance;
    }

    private DeviceConnectionListener()
    {
        addListener(this);
    }

    /**
     * Start listening.
     */
    public void startListening()
    {
        runSchedule();
    }

    /**
     * Stop listening.
     */
    public void stopListening()
    {
        cancelSchedule();
    }

    /**
     * Check for listening.
     *
     * @return the boolean
     */
    public boolean isListening()
    {
        return isListening;
    }

    private void runSchedule()
    {
        if (!isListening)
        {
            timer = new Timer();
            TimerTask timerTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    scanForDeviceConnections();
                }
            };

            long timerDelayMs = 0;
            timer.schedule(timerTask, timerDelayMs, scanTimerPeriodMs);
            isListening = true;

            ApplicationLogger.LOGGER.info("Listening schedule has started. Waiting for devices...");
        }
        else
        {
            cancelSchedule();
            runSchedule();
        }
    }

    private void cancelSchedule()
    {
        timer.cancel();
        isListening = false;

        ApplicationLogger.LOGGER.info("Listening schedule has canceled.");
    }

    private void scanForDeviceConnections()
    {
        ArrayList<DeviceInfo> combinedList = new ArrayList<>();

        ArrayList<DeviceInfo> comPortsList = getCOMPortsList();
        combinedList.addAll(comPortsList);
        ArrayList<DeviceInfo> hidDevicesList = getHIDDevicesList();
        combinedList.addAll(hidDevicesList);

        updateConnectedDeviceList(combinedList);
    }

    /**
     * Get current connected ports via ArrayList<DeviceInfo>
     *
     * @return current connected ports
     */
    private ArrayList<DeviceInfo> getCOMPortsList()
    {
        //Get connected port names
        String[] portNames = SerialPortList.getPortNames();

        //Generate array list from portNames
        ArrayList<DeviceInfo> deviceInfoList = new ArrayList<>();

        for (String portName : portNames)
        {
            deviceInfoList.add(new DeviceInfo(portName));
        }

        return deviceInfoList;
    }

    private ArrayList<DeviceInfo> getHIDDevicesList()
    {
        try
        {
            HIDDeviceInfo[] hidDeviceInfos = HIDManager.getInstance().listDevices();

            //Generate array list from portNames
            ArrayList<DeviceInfo> deviceInfoList = new ArrayList<>();

            if (hidDeviceInfos != null)
            {
                for (HIDDeviceInfo hidDeviceInfo : hidDeviceInfos)
                {
                    deviceInfoList.add(new DeviceInfo(hidDeviceInfo));
                }
            }

            return deviceInfoList;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Analise previously scan with current for find new conn or disconn
     *
     * @param scannedDevicesList
     */
    private void updateConnectedDeviceList(ArrayList<DeviceInfo> scannedDevicesList)
    {
        //adding
        for (DeviceInfo deviceInfo : scannedDevicesList)
        {
            if (!connectedDeviceList.contains(deviceInfo))
            {
                connectedDeviceList.add(deviceInfo);
                performDeviceConnectionEvent(deviceInfo, DeviceConnectionStateEnum.CONNECTED);
            }
        }

        //removing
        //create temp array coz ConcurrentModificationException
        ArrayList<DeviceInfo> clone = new ArrayList<>();
        clone = clone.getClass().cast(connectedDeviceList.clone());
        for (DeviceInfo deviceInfo : clone)
        {
            if (!scannedDevicesList.contains(deviceInfo))
            {
                performDeviceConnectionEvent(deviceInfo, DeviceConnectionStateEnum.DISCONNECTED);
                connectedDeviceList.remove(deviceInfo);
            }
        }
    }

    @Override
    public void deviceConnectionEvent(DeviceInfo connectedDevice, DeviceConnectionStateEnum deviceConnectionStateEnum)
    {
        ApplicationLogger.LOGGER.info(connectedDevice.getName() + " " + deviceConnectionStateEnum);

        if (deviceConnectionStateEnum == DeviceConnectionStateEnum.CONNECTED)
        {
            //Call Factory method
            DeviceCommunication deviceCommunication = DeviceCommunication.getInstance(connectedDevice);

            //filter known devices
            if (deviceCommunication != null)
            {
                deviceCommunication.getRxRawDataReceiver().addListener(PacketLogger.getInstance());
                deviceCommunication.getRxRawDataReceiver().addListener(PacketAnalysis.getInstance());

                Thread thread = new Thread(deviceCommunication);
                thread.setDaemon(true);
                thread.start();
            }
            else
            {
                ApplicationLogger.LOGGER.info(connectedDevice.getName() + " " + "ignored.");
            }

        }
    }

    //region Observer implementation

    /**
     * Add listener.
     *
     * @param toAdd
     *         the to add
     */
    public void addListener(DeviceConnectionListenerI toAdd)
    {
        listeners.add(toAdd);
    }

    private void performDeviceConnectionEvent(DeviceInfo deviceName, DeviceConnectionStateEnum connectionStateEnum)
    {
        // Notify everybody that may be interested.
        for (DeviceConnectionListenerI listenerI : listeners)
        {
            listenerI.deviceConnectionEvent(deviceName, connectionStateEnum);
        }
    }

    //endregion
}
