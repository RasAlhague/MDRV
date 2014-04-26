package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import jssc.SerialPortException;

final class AirView2 extends COMDeviceCommunication
{
    AirView2(DeviceInfo deviceInfo)
    {
        super(deviceInfo);
    }

    @Override
    public void initializeDevice()
    {
        byte[] intByte = new byte[]{0x69, 0x6E, 0x74}; //int
        //        byte[] gdiByte = new byte[]{0x0A, 0x67, 0x64, 0x69, 0x0A}; //.gdi.
        byte[] bsByte = new byte[]{0x0A, 0x62, 0x73, 0x0A}; //.bs.

        try
        {
            //            Thread.sleep(5000);
            serialPort.writeBytes(intByte);
            //            serialPort.writeBytes(gdiByte);
            serialPort.writeBytes(bsByte);
            Thread.sleep(5000);
        }
        catch (SerialPortException | InterruptedException e)
        {
            e.printStackTrace();
        }

        ApplicationLogger.LOGGER.info("AirView2 has been initialized");
    }
}
