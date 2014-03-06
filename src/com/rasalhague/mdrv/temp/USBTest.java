package com.rasalhague.mdrv.temp;

import javax.usb.*;
import javax.usb.event.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class USBTest implements UsbPipeListener, UsbDeviceListener
{
    public final static int VERNIER_VENDOR_ID = 0x1dd5; // my usb vid
    public final static int GOTEMP_PRODUCT_ID = 0x2410; // my usb pid

    public void writeAndReadTest() throws UsbException, IOException
    {
        UsbDevice radarUsbDevice = null;
        UsbConfiguration activeUsbConfiguration = null;
        UsbInterface usbInterface = null;
        UsbEndpoint outputEndpoint = null;
        UsbEndpoint inputEndpoint = null;
        UsbPipe outputPipe = null; // pipe used for sending data to usb device
        UsbPipe inputPipe = null; // receiving data from usb

        radarUsbDevice = getRadarUsbDevice();// find out my device.
        if (radarUsbDevice == null)
        {
            System.err.println("no radarUsbDevice.");
            return;
        }

        activeUsbConfiguration = radarUsbDevice.getActiveUsbConfiguration();
        /*
         * with dump info of my usb, i know there is only one interface, so, 0
		 * is used here to get the first one.
		 */
        usbInterface = activeUsbConfiguration.getUsbInterface((byte) 0);
        usbInterface.claim(new UsbInterfacePolicy()
        {
            // absolutely, i wanna claim it :)
            public boolean forceClaim(UsbInterface usbInterface)
            {
                return true;
            }
        });

		/*
		 * first, i get to write cmds to usb for initializing radar. so,
		 * direction is 'out'. my usb device is of type 'bulk', so use bulk.
		 */
        outputEndpoint = findEndpoint(usbInterface.getUsbEndpoints(),
                                      UsbConst.REQUESTTYPE_DIRECTION_OUT,
                                      UsbConst.ENDPOINT_TYPE_INTERRUPT);
        outputPipe = outputEndpoint.getUsbPipe();

        outputPipe.addUsbPipeListener(this);
        radarUsbDevice.addUsbDeviceListener(this);

        outputPipe.open();

		/*
		 * init radar settings
		 */
        byte[] data = new byte[6]; // cmds for init radar.
        // data[0] = 1;...


		/*
		 * irp, not a control-irp, bcz i just need to send data, not to control
		 * usb. if u wanna control usb, or to get sth of ur usb, as the vid,
		 * pid, version and so on, u sld use ctrl-irp.
		 */
        UsbIrp irp = outputPipe.createUsbIrp();
        irp.setData(data);
        irp.setLength(data.length);
        irp.setOffset(0);
        irp.setAcceptShortPacket(true);
        outputPipe.syncSubmit(irp); // :) it works fine!
        irp.complete();
        System.out.println("data is sent and actual length is " + irp.getActualLength());

		/*
		 * ctrl radar to send data
		 */
        data = new byte[6];
        // data[0] = 1;...

        irp = outputPipe.createUsbIrp();
        irp.setData(data);
        irp.setLength(data.length);
        irp.setOffset(0);
        irp.setAcceptShortPacket(true);
        outputPipe.syncSubmit(irp); // :) it works fine!
        irp.complete();
        System.out.println("data is sent and actual length is " + irp.getActualLength());

		/*
		 * --------------------------------------------------------------------
		 * read data from radar usb device
		 * --------------------------------------------------------------------
		 * now, i wanna read data from usb, so 'IN' is used.
		 */

        inputEndpoint = findEndpoint(usbInterface.getUsbEndpoints(),
                                     UsbConst.REQUESTTYPE_DIRECTION_IN,
                                     UsbConst.ENDPOINT_TYPE_BULK);
        inputPipe = inputEndpoint.getUsbPipe();
        inputPipe.open();
        irp = inputPipe.createUsbIrp();
        data = new byte[512];// for my usb, max-size of pkg of in-pipe is 512
        irp.setData(data);

		/*
		 * always time-out on this line!!! no matter sync or async method is
		 * used. no matter sumbmit a byte[] or irp, i get a timeout error.
		 */
        inputPipe.syncSubmit(irp); // god bless me, sb help me......
        irp.complete();
        System.out.println("data is read and actual length is " + irp.getActualLength());

        // outputPipe.close();
        // inputPipe.close();
        // usbInterface.release();
    }

    /**
     * before u do this, u get to install a right driver for ur usb device. pls google it for details. pls make ur usb
     * device is driver-ed properly by ur new driver.
     *
     * @return
     *
     * @throws UsbException
     * @throws IOException
     */
    private static UsbDevice getRadarUsbDevice() throws UsbException, IOException
    {
		/*
		 * javax.usb will use a property file to create the service: String
		 * className =
		 * getProperties().getProperty(JAVAX_USB_USBSERVICES_PROPERTY); pls make
		 * sure the prop file is created with content: javax.usb.services =
		 * de.ailis.usb4java.Services and add the file path to class path of ur
		 * project. so the compiler cld find it.
		 */
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub root = services.getRootUsbHub();

		/*
		 * with the root device, we get to search all the devices to get my
		 * radar usb.
		 */
        return searchDevices(root);
    }

    private static UsbDevice searchDevices(UsbHub hub) throws UsbException, IOException
    {
        List devices = hub.getAttachedUsbDevices();
        Iterator iterator = devices.iterator();
        while (iterator.hasNext())
        {
            UsbDevice device = (UsbDevice) iterator.next();
            UsbDeviceDescriptor descriptor = device.getUsbDeviceDescriptor();
            int manufacturerCode = descriptor.idVendor();
            int productCode = descriptor.idProduct();

            if (manufacturerCode == VERNIER_VENDOR_ID && productCode == GOTEMP_PRODUCT_ID)
            {
                return device;
            }
            else if (device.isUsbHub())
            {
                UsbDevice found = searchDevices((UsbHub) device);
                if (found != null) { return found; }
            }
        }
        return null; // didn't find it
    }

    private static UsbEndpoint findEndpoint(List totalEndpoints, int inDirection, int inType)
    {
        for (int j = 0; j < totalEndpoints.size(); j++)
        {
            UsbEndpoint ep = (UsbEndpoint) totalEndpoints.get(j);
            int direction = ep.getDirection();
            int type = ep.getType();
            //            if (((byte) (inDirection) == (byte) direction) && (inType == type)) {
            //                return ep;
            //            }
            if ((inType == type))
            {
                return ep;
            }
        }

        // fail("End point NOT FOUND!!!");
        return null;

    }

    @Override
    public void errorEventOccurred(UsbPipeErrorEvent usbPipeErrorEvent)
    {
        System.out.println(usbPipeErrorEvent);
    }

    @Override
    public void dataEventOccurred(UsbPipeDataEvent usbPipeDataEvent)
    {
        System.out.println(usbPipeDataEvent.getData());
    }

    @Override
    public void usbDeviceDetached(UsbDeviceEvent usbDeviceEvent)
    {
        System.out.println(usbDeviceEvent);

    }

    @Override
    public void errorEventOccurred(UsbDeviceErrorEvent usbDeviceErrorEvent)
    {
        System.out.println(usbDeviceErrorEvent);

    }

    @Override
    public void dataEventOccurred(UsbDeviceDataEvent usbDeviceDataEvent)
    {
        System.out.println(usbDeviceDataEvent.getData());

    }
}
