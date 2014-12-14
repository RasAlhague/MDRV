package com.rasalhague.mdrv.tests;

import javax.usb.*;
import java.util.Arrays;
import java.util.List;

public class MetaGeekWiSpyGen1
{
    public UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
    {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
            if (device.isUsbHub())
            {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        return null;
    }

    public void libusbTest()
    {
        try
        {
            UsbServices services = UsbHostManager.getUsbServices();

            UsbDevice device = findDevice(services.getRootUsbHub(), (short) 6017, (short) 2110);
            UsbControlIrp irp = device.createUsbControlIrp((byte) (UsbConst.REQUESTTYPE_DIRECTION_IN |
                                                                   UsbConst.REQUESTTYPE_TYPE_STANDARD |
                                                                   UsbConst.REQUESTTYPE_RECIPIENT_DEVICE),
                                                           UsbConst.REQUEST_GET_CONFIGURATION,
                                                           (short) 0,
                                                           (short) 0);
            irp.setData(new byte[1]);
            device.syncSubmit(irp);
            System.out.println(irp.getData()[0]);

            UsbConfiguration configuration = device.getActiveUsbConfiguration();
            UsbInterface iface = configuration.getUsbInterface((byte) 0);
            iface.claim();
            try
            {
                List<UsbEndpoint> usbEndpoints = iface.getUsbEndpoints();
                UsbPipe pipe = usbEndpoints.get(0).getUsbPipe();
                pipe.open();
                try
                {
                    byte[] data = new byte[8];
                    //                    int received = pipe.syncSubmit(data);
                    //                    System.out.println(received + " bytes received");

                    irp = device.createUsbControlIrp((byte) (UsbConst.REQUESTTYPE_DIRECTION_IN |
                                                             UsbConst.REQUESTTYPE_TYPE_STANDARD |
                                                             UsbConst.REQUESTTYPE_RECIPIENT_DEVICE),
                                                     UsbConst.REQUEST_CLEAR_FEATURE,
                                                     (short) 2,
                                                     (short) 1);
                    irp.setData(data);
                    device.syncSubmit(irp);
                    System.out.println(Arrays.toString(data));
                }
                catch (UsbException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    pipe.close();
                }
            }
            finally
            {
                iface.release();
            }
        }
        catch (UsbException e)
        {
            e.printStackTrace();
        }
    }
}
