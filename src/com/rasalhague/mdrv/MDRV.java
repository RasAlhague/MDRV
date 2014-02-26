package com.rasalhague.mdrv;

public class MDRV implements DeviceConnectionListenerI
{
    public static void main(String[] args)
    {
        ApplicationLogger.setup();

        MDRV mdrv = new MDRV();
        mdrv.doIt();
    }

    private void doIt()
    {
        startGUI();

        DeviceConnectionListener devConnListener = new DeviceConnectionListener(this);
        Thread devConnListenerThread = new Thread(devConnListener);
        devConnListenerThread.start();
    }

    private void startGUI()
    {
        MainWindow.initialize();
    }

    @Override
    public void deviceConnectionEvent(DeviceInfo connectedDevice,
                                      DeviceConnectionStateEnum deviceConnectionStateEnum)
    {
        ApplicationLogger.info(connectedDevice.getDevicePortName() + " " + deviceConnectionStateEnum);

        if (deviceConnectionStateEnum == DeviceConnectionStateEnum.CONNECTED)
        {
            if (connectedDevice.getDeviceType() == DeviceInfo.DeviceTypeEnum.COM)
            {
                //Create GUI for output
                OutputForm outputForm = new OutputForm();
                outputForm.startGUI();

                //Call Factory method and set form to out
                DeviceCommunication deviceCommunication = DeviceCommunication.getInstance(connectedDevice);
                deviceCommunication.rxRawDataReceiver.addObserver(outputForm);
                deviceCommunication.rxRawDataReceiver.addObserver(PacketLogger.getInstance());

                new Thread(deviceCommunication).start();
            }
        }
    }
}
