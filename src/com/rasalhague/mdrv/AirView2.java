package com.rasalhague.mdrv;

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
//        byte[] gdiByte = new byte[]{0x0A, 0x67, 0x64, 0x69, 0x0A}; //.gdi.
        byte[] initByte = new byte[]{0x0A, 0x69, 0x6E, 0x74, 0x0A}; //.init.
        byte[] bsByte = new byte[]{0x0A, 0x62, 0x73, 0x0A}; //.bs.

        try
        {
            serialPort.writeBytes(initByte);
            serialPort.writeBytes(bsByte);
        }
        catch (SerialPortException e)
        {
            e.printStackTrace();
        }
    }
}
