package com.rasalhague.mdrv.connectionlistener;

import com.rasalhague.mdrv.analysis.PacketAnalysis;
import com.rasalhague.mdrv.device.core.Device;
import com.rasalhague.mdrv.device.core.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import com.rasalhague.mdrv.logging.PacketLogger;
import com.rits.cloning.Cloner;

public class DeviceConnectionHandler implements DeviceConnectionListenerI
{
    @Override
    public void deviceConnectionEvent(DeviceInfo connectedDevice, DeviceConnectionStateEnum deviceConnectionStateEnum)
    {
        DeviceInfo deviceInfoClone = new Cloner().deepClone(connectedDevice);

        ApplicationLogger.LOGGER.info(deviceInfoClone.getName() + " " + deviceConnectionStateEnum);

        if (deviceConnectionStateEnum == DeviceConnectionStateEnum.CONNECTED)
        {
            //Call Factory method
            Device device = Device.getConcreteDevice(deviceInfoClone);

            //filter known device
            if (device != null)
            {
                device.getRxRawDataReceiver().addListener(PacketLogger.getInstance());
                device.getRxRawDataReceiver().addListener(PacketAnalysis.getInstance());

                Thread thread = new Thread(device.getDeviceCommunication());
                thread.setName(device.getDeviceInfo().getFriendlyNameWithId());
                thread.setDaemon(true);
                thread.start();
            }
            else
            {
                ApplicationLogger.LOGGER.info(deviceInfoClone.getName() +
                                                      " on " +
                                                      deviceInfoClone.getPortName() +
                                                      " ignored.");
            }
        }
    }
}
