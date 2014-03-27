package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DeviceInfo;

public class MetaGeek_WiSpy24x2 extends HIDDeviceCommunication
{
    MetaGeek_WiSpy24x2(DeviceInfo devInfo)
    {
        super(devInfo);
    }

    @Override
    void initializeDevice()
    {
        //        try
        //        {
        //            byte[] dataToWrite = new byte[]{10,
        //                    0x53,
        //                    0x10,
        //                    0x11,
        //                    0x00,
        //                    (byte) 0x9F,
        //                    0x24,
        //                    0x00,
        //                    (byte) 0xC4,
        //                    0x15,
        //                    0x05,
        //                    0x00,
        //                    0x6C,
        //                    (byte) 0xDC,
        //                    0x02,
        //                    0x00,
        //                    0x1E,
        //                    0x01,
        //                    0x64,
        //                    0x01,
        //                    0x00,
        //            0x00,
        //            0x00,
        //            0x00,
        //            0x00,
        //            0x00,
        //            0x00,
        //            0x00,
        //            0x00,
        //            0x00,
        //            0x00,
        //            0x00,
        //            0x00};
        //
        //                        hidDevice.write(dataToWrite);
        //            ApplicationLogger.LOGGER.info("MetaGeek_WiSpy24x2 has initialized");
        //
        //            byte[] bytes = new byte[32];
        //            System.out.println(hidDevice.read(bytes));
        //            System.out.println(Arrays.toString(bytes));
        //
        //
        //            //            byte[] bytes = new byte[65];
        ////            bytes [0] = 0x02;           // num protocol
        ////            bytes [1] = (byte) (bytes.length+1); // length of the data
        ////            bytes [2] = 5;
        ////            int featureReport = hidDevice.write(bytes);
        ////            System.out.println(featureReport);
        //        }
        //        catch (IOException e)
        //        {
        //            ApplicationLogger.LOGGER.severe(e.getMessage());
        //            e.printStackTrace();
        //        }
    }
}
