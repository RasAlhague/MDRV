package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DeviceInfo;
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

    COMDeviceCommunication(DeviceInfo deviceInfo)
    {
        super(deviceInfo);

        serialPort = new SerialPort(deviceInfo.getDevicePortName());
    }

    @Override
    void initializeDevice()
    {
        ApplicationLogger.LOGGER.warning(
                "Device not specified. Can not choose right init sequence. Initialization ignored.");
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent)
    {
        if (serialPortEvent.isRXCHAR() && serialPortEvent.getEventValue() > 0)
        {
            try
            {
                byte[] rawData;
                //                rawData = serialPort.readString(serialPortEvent.getEventValue());
                //                rxRawDataReceiver.receiveRawData(rawData);

                //                rawData = serialPort.readHexStringArray(serialPortEvent.getEventValue());
                //                System.out.println(Byte.parseByte(rawData[0], 16));
                //                rxRawDataReceiver.receiveRawData(rawData);

                rawData = serialPort.readBytes(serialPortEvent.getEventValue());
                rxRawDataReceiver.receiveRawData(rawData);
            }
            catch (SerialPortException e)
            {
                ApplicationLogger.LOGGER.severe(e.getMessage());
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
            ApplicationLogger.LOGGER.severe(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void openPort() throws SerialPortException
    {
        ApplicationLogger.LOGGER.info("Trying to open " + deviceInfo.getDevicePortName());

        serialPort.openPort();

        ApplicationLogger.LOGGER.info(deviceInfo.getDevicePortName() + " " + "has been opened!");

        serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
    }

    private void addListener() throws SerialPortException
    {
        serialPort.addEventListener(this, SerialPort.MASK_RXCHAR);

        ApplicationLogger.LOGGER.info("SerialPort Listening has started");
    }

    /**
     * close port
     */
    private void stop()
    {
        try
        {
            boolean port = serialPort.closePort();
            ApplicationLogger.LOGGER.info("Stopping " + serialPort.getPortName() + " result: " + port);
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
