package com.rasalhague.mdrv;

import com.rasalhague.mdrv.analysis.PacketAnalysis;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import com.rasalhague.mdrv.logging.PacketLogger;
import jssc.SerialPortList;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

enum DeviceConnectionStateEnum
{
    CONNECTED, DISCONNECTED
}

interface DeviceConnectionListenerI
{
    public void deviceConnectionEvent(DeviceInfo connectedDevice, DeviceConnectionStateEnum deviceConnectionStateEnum);
}

public class DeviceConnectionListener implements Runnable, DeviceConnectionListenerI
{
    private ArrayList<DeviceInfo>           comDeviceInfoList = new ArrayList<DeviceInfo>();
    private List<DeviceConnectionListenerI> listeners         = new ArrayList<DeviceConnectionListenerI>();

    public DeviceConnectionListener()
    {
        addListener(this);
    }

    public void addListener(DeviceConnectionListenerI toAdd)
    {
        listeners.add(toAdd);
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

        ApplicationLogger.LOGGER.info("Waiting for devices...");
    }

    private void scanForDeviceConnections()
    {
        scanForCOMPorts();

        //TODO ScanUSB
    }

    //region Observer implementation

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

    private void performDeviceConnectionEvent(DeviceInfo deviceName, DeviceConnectionStateEnum connectionStateEnum)
    {
        // Notify everybody that may be interested.
        for (DeviceConnectionListenerI listenerI : listeners)
        {
            listenerI.deviceConnectionEvent(deviceName, connectionStateEnum);
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
                deviceCommunication.rxRawDataReceiver.addObserver(PacketLogger.getInstance());
                deviceCommunication.rxRawDataReceiver.addListener(PacketAnalysis.getInstance());

                Thread thread = new Thread(deviceCommunication);
                thread.setName(connectedDevice.getDeviceName() + " Thread");
                thread.start();
            }
        }
    }

    //endregion
}
