package com.rasalhague.mdrv.temp;

import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;

import java.io.IOException;

public class UsbHidTest
{
    int vId_ISM = 7289;
    int pId_ISM = 8193;

    int vId_WySpy = 7637;
    int pId_WySpy = 9232;

    byte[] WySpyInit = new byte[]{0x53,
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
            0x00,/*
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
            0x00*/};

    public UsbHidTest()
    {
        com.codeminders.hidapi.ClassPathLibraryLoader.loadNativeHIDLibrary();

        try
        {
            HIDDeviceInfo[] hidDeviceInfos = HIDManager.getInstance().listDevices();

            for (HIDDeviceInfo hidDeviceInfo : hidDeviceInfos)
            {
                System.out.println(hidDeviceInfo);
            }

            //            HIDDevice hidDevice = HIDManager.getInstance().openById(vId_WySpy, pId_WySpy, null);
            //
            //            byte[] buf = new byte[32];
            //            buf [0] = 0x0;           // num protocol
            //            buf [1] = (byte) buf.length;           // size
            //            buf [2] = (byte) 0x80;

            //                        hidDevice.enableBlocking();

            //            int featureReport = hidDevice.getFeatureReport(buf);
            //            hidDevice.write(buf);
            //            int featureReport = hidDevice.getFeatureReport(buf);
            //            System.out.println(featureReport);

            //            byte[] b = new byte[64];
            //            int read = hidDevice.read(b);
            //            System.out.println(read);
            //
            //            for (int i = 0; i < read; i++)
            //            {
            //                byte b1 = b[i];
            //                System.out.print(b1+ " ");
            //            }
            //            System.out.println();
            //            System.out.println(new String(b));
            //            for (byte b1 : b)
            //            {
            //                System.out.print(b1 + " ");
            //            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                HIDManager.getInstance().release();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}