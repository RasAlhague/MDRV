package com.rasalhague.mdrv;

import com.rasalhague.mdrv.analysis.PacketAnalysis;
import com.rasalhague.mdrv.dev_communication.DeviceCommunication;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import com.rasalhague.mdrv.logging.PacketLogger;
import jssc.SerialPortList;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

enum DeviceConnectionStateEnum
{
    CONNECTED,
    DISCONNECTED
}

interface DeviceConnectionListenerI
{
    public void deviceConnectionEvent(DeviceInfo connectedDevice, DeviceConnectionStateEnum deviceConnectionStateEnum);
}

/**
 * Singleton
 */
public class DeviceConnectionListener implements DeviceConnectionListenerI
{
    private ArrayList<DeviceInfo>           comDeviceInfoList = new ArrayList<DeviceInfo>();
    private List<DeviceConnectionListenerI> listeners         = new ArrayList<DeviceConnectionListenerI>();
    private long    timerDelayMs  = 0;
    private long    timerPeriodMs = 1000;
    private boolean isListening   = false;
    private Timer timer;

    private static DeviceConnectionListener instance = new DeviceConnectionListener();

    //    private static class DeviceConnectionListenerHolder
    //    {
    //        private final static DeviceConnectionListener INSTANCE = new DeviceConnectionListener();
    //    }
    //
    //    public static DeviceConnectionListener getInstance()
    //    {
    //        return DeviceConnectionListenerHolder.INSTANCE;
    //    }

    public static DeviceConnectionListener getInstance()
    {
        return instance;
    }

    private DeviceConnectionListener()
    {
        addListener(this);
    }

    public static void startListening()
    {
        instance.runSchedule();
    }

    public static void stopListening()
    {
        instance.cancelSchedule();
    }

    public static boolean isListening()
    {
        return instance.isListening;
    }

    private void runSchedule()
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

        timer.schedule(timerTask, timerDelayMs, timerPeriodMs);
        isListening = true;

        ApplicationLogger.LOGGER.info("Listening schedule started. Waiting for devices...");
    }

    private void cancelSchedule()
    {
        timer.cancel();
        isListening = false;

        ApplicationLogger.LOGGER.info("Listening schedule canceled.");
    }

    private void scanForDeviceConnections()
    {
        scanForCOMPorts();

        //TODO ScanUSB
    }

    private void scanForCOMPorts()
    {
        String[] portNames = SerialPortList.getPortNames();
        ArrayList<DeviceInfo> portNamesInfoArrayList = DeviceInfo.createArrayListFromNames(portNames,
                                                                                           DeviceInfo.DeviceTypeEnum.COM);

        //        System.out.println("portNames.length = " + String.valueOf(portNames.length));

        //adding
        for (String port : portNames)
        {
            //TODO need to avoid new DeviceInfo every scan
            DeviceInfo deviceInfo = new DeviceInfo(port, DeviceInfo.DeviceTypeEnum.COM);
            if (!comDeviceInfoList.contains(deviceInfo))
            {
                comDeviceInfoList.add(deviceInfo);
                performDeviceConnectionEvent(deviceInfo, DeviceConnectionStateEnum.CONNECTED);
            }
        }

        //removing
        //create temp array bcs ConcurrentModificationException

        ArrayList<DeviceInfo> clone = new ArrayList<DeviceInfo>();
        clone = clone.getClass().cast(comDeviceInfoList.clone());
        for (DeviceInfo deviceInfo : clone)
        {
            if (!portNamesInfoArrayList.contains(deviceInfo))
            {
                performDeviceConnectionEvent(deviceInfo, DeviceConnectionStateEnum.DISCONNECTED);
                comDeviceInfoList.remove(deviceInfo);
            }
        }

    }

    @Override
    public void deviceConnectionEvent(DeviceInfo connectedDevice, DeviceConnectionStateEnum deviceConnectionStateEnum)
    {
        ApplicationLogger.LOGGER.info(connectedDevice.getDevicePortName() + " " + deviceConnectionStateEnum);

        if (deviceConnectionStateEnum == DeviceConnectionStateEnum.CONNECTED)
        {
            if (connectedDevice.getDeviceType() == DeviceInfo.DeviceTypeEnum.COM)
            {
                //Create GUI for output
                //                OutputForm outputForm = new OutputForm();
                //                outputForm.launchGUI();

                //Call Factory method and set form to out
                DeviceCommunication deviceCommunication = DeviceCommunication.getInstance(connectedDevice);

                //                deviceCommunication.rxRawDataReceiver.addObserver(outputForm);
                deviceCommunication.getRxRawDataReceiver().addObserver(PacketLogger.getInstance());
                deviceCommunication.getRxRawDataReceiver().addListener(PacketAnalysis.getInstance());

                Thread thread = new Thread(deviceCommunication);
                thread.setName(connectedDevice.getDeviceName() + "QWEQWEQWEWQE Thread");
                //TODO Need correct thread control
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    //region Observer implementation

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
