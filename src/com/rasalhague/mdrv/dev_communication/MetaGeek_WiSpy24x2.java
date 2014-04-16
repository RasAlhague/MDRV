package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;

class MetaGeek_WiSpy24x2 extends HIDDeviceCommunication
{
    MetaGeek_WiSpy24x2(DeviceInfo devInfo)
    {
        super(devInfo);
    }

    @Override
    void initializeDevice()
    {
        byte[] dataToWrite = new byte[]{0x53,
                0x10,
                0x11,
                0x00,
                (byte) 0x9F,
                0x24,
                0x00,
                (byte) 0xC4,
                0x15,
                0x05,
                0x00,
                0x6C,
                (byte) 0xDC,
                0x02,
                0x00,
                0x1E,
                0x01,
                0x64,
                0x01,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00};

        try
        {
            if (SystemUtils.IS_OS_LINUX)
            {
                hidDevice.write(dataToWrite);
                ApplicationLogger.LOGGER.info("MetaGeek_WiSpy24x2 has been initialized");
            }
            else
            {
                ApplicationLogger.LOGGER.info("MetaGeek_WiSpy24x2 has not been initialized due to OS");
            }

            //            byte[] data = new byte[64];
            //            data[0] = 0x01;    //Report ID
            ////            data[1] = (byte) (data.length-2);    //l
            //
            //            System.out.println((data.length));
            //            System.out.println(hidDevice.write(data));
        }
        catch (IOException e)
        {
            ApplicationLogger.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }
}
