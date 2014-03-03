package com.rasalhague.mdrv;

import com.rasalhague.mdrv.logging.ApplicationLogger;
import jssc.SerialPortList;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

interface DeviceConnectionListenerI
{
    public void deviceConnectionEvent(DeviceInfo connectedDevice, DeviceConnectionStateEnum deviceConnectionStateEnum);
}

enum DeviceConnectionStateEnum
{
    CONNECTED, DISCONNECTED
}

public class DeviceConnectionListener implements Runnable
{
    private List<DeviceConnectionListenerI> listeners         = new ArrayList<DeviceConnectionListenerI>();
    private ArrayList<DeviceInfo>           comDeviceInfoList = new ArrayList<DeviceInfo>();

    public DeviceConnectionListener(DeviceConnectionListenerI listenerToAdd)
    {
        addListener(listenerToAdd);
    }

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

    @Override
    public void run()
    {
        startListening();
    }

    public void startListening()
    {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                scanForDeviceConnections();
            }
        };

        long timerDelay = 0;
        long timerPeriod = 1000;
        timer.schedule(timerTask, timerDelay, timerPeriod);

        ApplicationLogger.info("Waiting for devices...");
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
}
