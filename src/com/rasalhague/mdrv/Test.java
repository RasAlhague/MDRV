//package com.rasalhague.mdrv;
//
//import org.usb4java.*;
//
//import javax.usb.*;
//import java.nio.ByteBuffer;
//import java.util.List;
//
//public class Test
//{
//    public short wispyvid = (short) 0x1dd5;
//    public short wispypid = (short) 0x2410;
//    public short ismvid   = (short) 0x1c79;
//    public short ismpid   = (short) 0x2001;
//
//    public Test()
//    {
//    }
//
//    public void libusbWay()
//    {
//        Context context = new Context();
//        int initResult = LibUsb.init(context);
//        if (initResult != LibUsb.SUCCESS)
//        {
//            try
//            {
//                throw new LibUsbException("Unable to initializeDevice libusb.", initResult);
//            }
//            catch (LibUsbException e)
//            {
//                e.printStackTrace();
//            }
//        }
//
//        //        Device device = findDeviceLibusb((short) 0x1dd5, (short) 0x2410); // wispy
//        Device device = findDeviceLibusb((short) 0x1c79, (short) 0x2001); // ism
//
//        DeviceHandle handle = new DeviceHandle();
//        int result = LibUsb.open(device, handle);
//        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to open USB device", result);
//        try
//        {
//            // Use device handle here
//            byte interfaceNumber = (byte) 1;
//
//            result = LibUsb.claimInterface(handle, interfaceNumber);
//            if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to claim interface", result);
//            try
//            {
//                // Use interface here
//
//                ByteBuffer buffer = ByteBuffer.allocateDirect(8);
//                buffer.put(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
//                long timeout = 500;
//                int transfered = LibUsb.controlTransfer(handle,
//                                                        (byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE),
//                                                        (byte) 0x09,
//                                                        (short) 2,
//                                                        (short) 1,
//                                                        buffer,
//                                                        timeout);
//                if (transfered < 0) throw new LibUsbException("Control transfer failed", transfered);
//                System.out.println(transfered + " bytes sent");
//            }
//            finally
//            {
//                result = LibUsb.releaseInterface(handle, interfaceNumber);
//                if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to release interface", result);
//            }
//        }
//        finally
//        {
//            LibUsb.close(handle);
//        }
//
//        LibUsb.exit(context);
//    }
//
//    public Device findDeviceLibusb(short vendorId, short productId)
//    {
//        // Read the USB device list
//        DeviceList list = new DeviceList();
//        int result = LibUsb.getDeviceList(null, list);
//        if (result < 0)
//        {
//            try
//            {
//                throw new LibUsbException("Unable to get device list", result);
//            }
//            catch (LibUsbException e)
//            {
//                e.printStackTrace();
//            }
//        }
//
//        try
//        {
//            // Iterate over all device and scan for the right one
//            for (Device device : list)
//            {
//                DeviceDescriptor descriptor = new DeviceDescriptor();
//                result = LibUsb.getDeviceDescriptor(device, descriptor);
//                if (result != LibUsb.SUCCESS)
//                {
//                    try
//                    {
//                        throw new LibUsbException("Unable to read device descriptor", result);
//                    }
//                    catch (LibUsbException e)
//                    {
//                        e.printStackTrace();
//                    }
//                }
//                if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) return device;
//            }
//        }
//        finally
//        {
//            // Ensure the allocated device list is freed
//            LibUsb.freeDeviceList(list, true);
//        }
//
//        // Device not found
//        return null;
//    }
//
//    public UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
//    {
//        if (hub == null)
//        {
//            try
//            {
//                hub = UsbHostManager.getUsbServices().getRootUsbHub();
//            }
//            catch (UsbException e)
//            {
//                e.printStackTrace();
//            }
//        }
//
//        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
//        {
//            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
//            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
//            if (device.isUsbHub())
//            {
//                device = findDevice((UsbHub) device, vendorId, productId);
//                if (device != null) return device;
//            }
//        }
//        return null;
//    }
//
//    public void dumpDevice(final UsbDevice device)
//    {
//        // Dump information about the device itself
//        System.out.println(device);
//        final UsbPort port = device.getParentUsbPort();
//        if (port != null)
//        {
//            System.out.println("Connected to port: " + port.getPortNumber());
//            System.out.println("Parent: " + port.getUsbHub());
//        }
//
//        // Dump device descriptor
//        System.out.println(device.getUsbDeviceDescriptor());
//
//        // Process all configurations
//        for (UsbConfiguration configuration : (List<UsbConfiguration>) device.getUsbConfigurations())
//        {
//            // Dump configuration descriptor
//            System.out.println(configuration.getUsbConfigurationDescriptor());
//
//            // Process all interfaces
//            for (UsbInterface iface : (List<UsbInterface>) configuration.getUsbInterfaces())
//            {
//                // Dump the interface descriptor
//                System.out.println(iface.getUsbInterfaceDescriptor());
//
//                // Process all endpoints
//                for (UsbEndpoint endpoint : (List<UsbEndpoint>) iface.getUsbEndpoints())
//                {
//                    // Dump the endpoint descriptor
//                    System.out.println(endpoint.getUsbEndpointDescriptor());
//                }
//            }
//        }
//
//        System.out.println();
//
//        // Dump child device if device is a hub
//        if (device.isUsbHub())
//        {
//            final UsbHub hub = (UsbHub) device;
//            for (UsbDevice child : (List<UsbDevice>) hub.getAttachedUsbDevices())
//            {
//                dumpDevice(child);
//            }
//        }
//    }
//
//    public void commUSB(UsbDevice device)
//    {
//        UsbConfiguration configuration = device.getActiveUsbConfiguration();
//        UsbInterface iface = configuration.getUsbInterface((byte) 1);
//        try
//        {
//            iface.claim();
//
//            UsbEndpoint endpoint = iface.getUsbEndpoint((byte) 0x03);
//            UsbPipe pipe = endpoint.getUsbPipe();
//            pipe.open();
//            int sent = pipe.syncSubmit(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
//            System.out.println(sent + " bytes sent");
//
//            pipe.close();
//        }
//        catch (UsbException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            try
//            {
//                iface.release();
//            }
//            catch (UsbException e)
//            {
//                e.printStackTrace();
//            }
//        }
//
//    }
//}
