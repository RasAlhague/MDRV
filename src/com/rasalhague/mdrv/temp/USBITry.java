package com.rasalhague.mdrv.temp;

import com.rasalhague.mdrv.logging.ApplicationLogger;
import de.ailis.usb4java.libusb.Context;
import de.ailis.usb4java.libusb.DeviceHandle;
import de.ailis.usb4java.libusb.DeviceList;
import de.ailis.usb4java.libusb.LibUsb;

import javax.usb.*;
import javax.usb.event.*;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

public class USBITry implements UsbServicesListener, UsbPipeListener, UsbDeviceListener
{
    //ISM Sniffer
    short vId_ISMSniffer = 0x1c79;
    short pId_ISMSniffer = 0x2001;

    //wi spy
    short vId_WySpy = 0x1DD5;
    short pId_WySpy = 0x2410;

    public void startUP()
    {
        try
        {
            UsbServices services = UsbHostManager.getUsbServices();
            services.addUsbServicesListener(this);
            UsbHub rootHub = services.getRootUsbHub();

            UsbDevice usbDevice = findDevice(rootHub, vId_ISMSniffer, pId_ISMSniffer);

            //            testIO(usbDevice);
            test1(vId_WySpy, pId_WySpy);
        }
        catch (UsbException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    public void testUSB()
    {
        try
        {
            UsbServices services = UsbHostManager.getUsbServices();
            UsbHub rootHub = services.getRootUsbHub();
            dump(rootHub);
            short vId = 0x1dd5;
            short pId = 0x2410;
            //            short vId = 0x1c79;
            //            short pId = 0x2001;
            UsbDevice device = findDevice(rootHub, vId, pId);

            UsbControlIrp irp = device.createUsbControlIrp((byte) (UsbConst.REQUESTTYPE_DIRECTION_IN | UsbConst.REQUESTTYPE_TYPE_STANDARD | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE),
                                                           UsbConst.REQUEST_SET_CONFIGURATION,
                                                           (short) 0,
                                                           (short) 0);
            byte[] bytes = new byte[]{0x1, 0x2};
            irp.setData(bytes);
            //            irp.setLength(bytes.length);
            device.addUsbDeviceListener(this);
            device.syncSubmit(irp);
            //            irp.complete();

            System.out.println(irp.getActualLength());
            System.out.println(Arrays.toString(irp.getData()));

            //            testIO(device);
        }
        catch (UsbException e)
        {
            ApplicationLogger.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }

    }

    private static void dump(UsbDevice device)
    {
        UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
        System.out.format("%04x:%04x%n", desc.idVendor() & 0xffff, desc.idProduct() & 0xffff);
        System.out.println(String.valueOf(desc.bDeviceSubClass()));
        if (device.isUsbHub())
        {
            UsbHub hub = (UsbHub) device;
            for (UsbDevice child : (List<UsbDevice>) hub.getAttachedUsbDevices())
            {
                dump(child);
            }
        }
    }

    public void testIO(UsbDevice device)
    {
        try
        {
            // Access to the active configuration of the USB device, obtain
            // all the interfaces available in that configuration.
            UsbConfiguration config = device.getActiveUsbConfiguration();
            List totalInterfaces = config.getUsbInterfaces();
            // Traverse through all the interfaces, and access the endpoints
            // available to that interface for I/O.
            for (int i = 0; i < totalInterfaces.size(); i++)
            {
                UsbInterface interf = (UsbInterface) totalInterfaces.get(i);
                interf.claim(new UsbInterfacePolicy()
                {
                    @Override
                    public boolean forceClaim(UsbInterface usbInterface)
                    {
                        return true;
                    }
                });
                List totalEndpoints = interf.getUsbEndpoints();
                for (Object totalEndpoint : totalEndpoints)
                {
                    // Access the particular endpoint, determine the direction
                    // of its data flow, and type of data transfer, and open the
                    // data pipe for I/O.
                    UsbEndpoint ep = (UsbEndpoint) totalEndpoints.get(i);
                    int direction = ep.getDirection();
                    int type = ep.getType();
                    UsbPipe pipe = ep.getUsbPipe();

                    //                    pipe.addUsbPipeListener(getUsbPipeListener());

                    pipe.open();
                    // Perform I/O through the USB pipe here.

                    device.addUsbDeviceListener(this);
                    pipe.addUsbPipeListener(this);

                    //                    UsbIrp usbIrp = pipe.createUsbIrp();
                    //                    byte[] data = new byte[]{0x1, 0x2};
                    //                    usbIrp.setData(data);
                    //                    usbIrp.setLength(data.length);
                    //                    usbIrp.setOffset(0);
                    //                    usbIrp.setAcceptShortPacket(true);

                    //                    pipe.syncSubmit(usbIrp);
                    //                    usbIrp.complete();

                    //                    System.out.println("data is sent and actual length is " + usbIrp.getActualLength());

                    pipe.syncSubmit(new byte[64]);

                    //                    pipe.close();
                }
                //                interf.release();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void test1(short vId, short pId) throws UsbException, UnsupportedEncodingException
    {
        UsbServices services = UsbHostManager.getUsbServices();
        services.addUsbServicesListener(this);

        UsbHub rootHub = services.getRootUsbHub();
        //List usbPorts = rootHub.getUsbPorts();
        //List attachedUsbDevices = rootHub.getAttachedUsbDevices();

        UsbDevice device = findDevice(rootHub, vId, pId);
        UsbConfiguration config = device.getActiveUsbConfiguration();
        UsbConfigurationDescriptor usbConfigurationDescriptor = config.getUsbConfigurationDescriptor();

        List usbInterfaces = config.getUsbInterfaces();
        UsbInterface usbInterface = (UsbInterface) usbInterfaces.get(0);

        List usbEndpoints = usbInterface.getUsbEndpoints();
        UsbEndpoint usbEndpoint = (UsbEndpoint) usbEndpoints.get(0);

        usbInterface.claim(new UsbInterfacePolicy()
        {
            @Override
            public boolean forceClaim(UsbInterface usbInterface)
            {
                return true;
            }
        });

        UsbPipe usbPipe = usbEndpoint.getUsbPipe();
        usbPipe.addUsbPipeListener(this);
        usbPipe.open();

        usbPipe.syncSubmit(new byte[64]);
    }

    private void test2()
    {
        int ret;

        Context context = new Context();
        short vId = 0x1dd5;
        short pId = 0x2410;
        //        short vId = 0x1C79;
        //        short pId = 0x2001;

        LibUsb.init(context);
        LibUsb.setDebug(context, 3);
        DeviceList deviceList = new DeviceList();
        LibUsb.getDeviceList(context, deviceList);

        DeviceHandle deviceHandle = LibUsb.openDeviceWithVidPid(context, vId, pId);

        ret = LibUsb.claimInterface(deviceHandle, 0);
        System.out.println(LibUsb.errorName(ret));

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(64);
        int s = LibUsb.bulkTransfer(deviceHandle, 81, byteBuffer, IntBuffer.allocate(164), 2000);
        System.out.println(s);
        System.out.println(LibUsb.errorName(s));
    }

    public static UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
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

    @Override
    public void usbDeviceAttached(UsbServicesEvent usbServicesEvent)
    {
        //        System.out.println(usbServicesEvent);
    }

    @Override
    public void usbDeviceDetached(UsbServicesEvent usbServicesEvent)
    {
        //        System.out.println(usbServicesEvent);
    }

    @Override
    public void errorEventOccurred(UsbPipeErrorEvent usbPipeErrorEvent)
    {
        //        System.out.println(usbPipeErrorEvent);
    }

    @Override
    public void dataEventOccurred(UsbPipeDataEvent usbPipeDataEvent)
    {
        System.out.println(usbPipeDataEvent.getData());
    }

    @Override
    public void usbDeviceDetached(UsbDeviceEvent usbDeviceEvent)
    {

    }

    @Override
    public void errorEventOccurred(UsbDeviceErrorEvent usbDeviceErrorEvent)
    {

    }

    @Override
    public void dataEventOccurred(UsbDeviceDataEvent usbDeviceDataEvent)
    {
        System.out.println(usbDeviceDataEvent.getData());
    }
}
