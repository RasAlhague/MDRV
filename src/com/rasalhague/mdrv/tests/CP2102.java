package com.rasalhague.mdrv.tests;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.junit.Test;

import java.util.Arrays;

public class CP2102 implements SerialPortEventListener
{
    SerialPort serialPort = new SerialPort("COM3");

    @Test
    public void CP2102()
    {
        try
        {
            serialPort.openPort();
            serialPort.addEventListener(this);
            serialPort.setParams(SerialPort.BAUDRATE_38400,
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);
            serialPort.setDTR(true);
            serialPort.setRTS(true);

            serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
            serialPort.writeBytes(new byte[]{(byte) 0xA6, 0x72, 0x0A});
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
            serialPort.writeBytes(new byte[]{(byte) 0xA6, 0x72, 0x0A});
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
            serialPort.writeBytes(new byte[]{(byte) 0xA6, 0x57, 0x30, 0x3D, (byte) 0xA6, 0x57});

            //          serialPort.writeBytes(new byte[]{(byte) 0xA6, 0x72, 0x0A, (byte) 0xA6, 0x72, 0x0B});
            //            serialPort.writeBytes(new byte[]{(byte) 0xA6, 0x53, 0x00, (byte) 0xFF, 0x00});
            //            serialPort.writeBytes(new byte[]{(byte) 0xA6, 0x72, 0x0A});
        }
        catch (SerialPortException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent)
    {
        System.out.println(serialPortEvent);
        try
        {
            System.out.println(Arrays.toString(serialPort.readBytes(serialPortEvent.getEventValue())));
        }
        catch (SerialPortException e)
        {
            e.printStackTrace();
        }
    }
}
