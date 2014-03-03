package com.rasalhague.mdrv;

import com.rasalhague.mdrv.logging.ApplicationLogger;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.util.Timer;
import java.util.TimerTask;

public class COMDeviceCommunication extends DeviceCommunication implements SerialPortEventListener
{
    SerialPort serialPort;
    //    private static final int BYTE_TO_READ_COUNT = 512;

    COMDeviceCommunication(DeviceInfo deviceInfo)
    {
        super(deviceInfo);

        serialPort = new SerialPort(deviceInfo.getDevicePortName());
    }

    @Override
    void initializeDevice()
    {
        ApplicationLogger.warning("Device not specified. Can not choose right init sequence. Initialization ignored.");
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent)
    {
        if (serialPortEvent.isRXCHAR() && serialPortEvent.getEventValue() > 0)
        {
            try
            {
                String rawData;
                rawData = serialPort.readString(serialPortEvent.getEventValue());
                //                rawData = serialPort.readString(BYTE_TO_READ_COUNT);
                rxRawDataReceiver.receiveRawData(rawData);

                //                System.out.println(rawData);
            }
            catch (SerialPortException e)
            {
                ApplicationLogger.severe(e.getMessage());
                e.printStackTrace();
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
            ApplicationLogger.severe(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void openPort() throws SerialPortException
    {
        ApplicationLogger.info("Trying to open " + deviceInfo.getDevicePortName());

        serialPort.openPort();

        ApplicationLogger.info(deviceInfo.getDevicePortName() + " " + "has been opened!");

        serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
    }

    private void addListener() throws SerialPortException
    {
        serialPort.addEventListener(this, SerialPort.MASK_RXCHAR);

        ApplicationLogger.info("SerialPort Listening has started");
    }

    /**
     * close port
     */
    private void stop()
    {
        try
        {
            boolean port = serialPort.closePort();
            ApplicationLogger.info("Stopping " + serialPort.getPortName() + "result: " + port);
        }
        catch (SerialPortException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Generate serialPort.writeInt(0) for ensure that device still connected Else - call stop() and kill timer
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
