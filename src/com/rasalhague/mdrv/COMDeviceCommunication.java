package com.rasalhague.mdrv;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.util.Timer;
import java.util.TimerTask;

public class COMDeviceCommunication extends DeviceCommunication implements SerialPortEventListener
{
    SerialPort serialPort;
    RxRawDataReceiver rxRawDataReceiver;

    COMDeviceCommunication(DeviceInfo deviceInfo)
    {
        super(deviceInfo);

        serialPort = new SerialPort(deviceInfo.devicePortName);
        rxRawDataReceiver = new RxRawDataReceiver(this.deviceInfo);
    }

    //Factory method
    public static COMDeviceCommunication getInstance(DeviceInfo deviceInfo)
    {
        //TODO Return correct class. At now AirView2 class is universal.
        return new AirView2(deviceInfo);
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent)
    {
        if (serialPortEvent.isRXCHAR() && serialPortEvent.getEventValue() > 0)
        {
            try
            {
                String rawData = serialPort.readString(serialPortEvent.getEventValue());
                rxRawDataReceiver.receiveRawData(rawData);

                System.out.println(rawData);
            }
            catch (SerialPortException ex)
            {
                ApplicationLogger.LOGGER.severe(ex.toString());
            }
        }
    }

    @Override
    public void run()
    {
        try
        {
            openPort();
            addListener();
            initializeDevice();

            testSignalTimer();
        }
        catch (SerialPortException ex)
        {
            ex.printStackTrace();
        }
    }

    private void openPort() throws SerialPortException
    {
        ApplicationLogger.LOGGER.info("Trying to open " + deviceInfo.devicePortName);

        serialPort.openPort();

        ApplicationLogger.LOGGER.info(deviceInfo.devicePortName + " " + "has benn opened!");

//        serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
    }

    private void addListener() throws SerialPortException
    {
        serialPort.addEventListener(this, SerialPort.MASK_RXCHAR);

        ApplicationLogger.LOGGER.info("SerialPort Listening has started");
    }

    public void initializeDevice()
    {
    }

    /**
     * close port
     */
    private void stop()
    {
        try
        {
            boolean port = serialPort.closePort();
            ApplicationLogger.LOGGER.info("Stopping " + serialPort.getPortName() + "result: " + port);
        }
        catch (SerialPortException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Generate serialPort.writeInt(0) for ensure that device still connected
     * Else - call stop() and kill timer
     */
    private void testSignalTimer()
    {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    if (!serialPort.writeInt(0))
                    {
                        stop();
                        //stop timer
                        this.cancel();
                    }
                }
                catch (SerialPortException e)
                {
                    e.printStackTrace();
                }
            }
        };

        long timerDelay = 0;
        long timerPeriod = 500;
        timer.schedule(timerTask, timerDelay, timerPeriod);
    }
}
