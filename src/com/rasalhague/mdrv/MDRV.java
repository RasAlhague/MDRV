package com.rasalhague.mdrv;

public class MDRV implements DeviceConnectionListenerI
{
    public static void main(String[] args)
    {
        ApplicationLogger.setup();

        MDRV mdrv = new MDRV();
        mdrv.doIt();

//        mdrv.testUSB();
    }

    //region USB

    /*

    private void testUSB() throws UsbException, UnsupportedEncodingException
    {
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub rootHub = services.getRootUsbHub();
        dump(rootHub);

        short ISMSnifferVID = 0x1C79;
        short ISMSnifferPID = 0x2001;

//        short AirView2VID = 0x1f9b; //0x1dd5
//        short AirView2PID = 0x0241; //0x2410

        UsbDevice ISMSnifferDevice = findDevice(rootHub, ISMSnifferVID, ISMSnifferPID);
//        UsbDevice AirView2Device = findDevice(rootHub, AirView2VID, AirView2PID);
//        System.out.println(AirView2Device);

//        testIO(ISMSnifferDevice);

        commUSB(ISMSnifferDevice);

//        logBus();
//        testJavaLibUSB();
    }

    private static void logBus() {
        // if you don't use the ch.ntb.usb.Device class you must initialise
        // Libusb before use
        LibusbJava.usb_init();
        LibusbJava.usb_find_busses();
        LibusbJava.usb_find_devices();

        // retrieve a object tree representing the bus with its devices and
        // descriptors
        Usb_Bus bus = LibusbJava.usb_get_busses();

        // log the bus structure to standard out
        ch.ntb.usb.Utils.logBus(bus);
    }

    private static void logData(byte[] data) {
        System.out.print("Data: ");
        for (int i = 0; i < data.length; i++) {
            System.out.print("0x" + Integer.toHexString(data[i] & 0xff) + " ");
        }
        System.out.println();
    }

    private void testJavaLibUSB()
    {
        System.loadLibrary("LibusbJava");
        // get a device instance with vendor id and product id
        Device dev = USB.getDevice((short) 0x1c79, (short) 0x2001);
        try {
            // data to write to the device
            byte[] data = new byte[64];
            // data read from the device
            byte[] readData = new byte[data.length];

            // open the device with configuration 1, interface 0 and without
            // altinterface
            // this will initialise Libusb for you
            dev.open(1, 0, -1);
            // write some data to the device
            // 0x03 is the endpoint address of the OUT endpoint 3 (from PC to
            // device)
//            dev.writeInterrupt(0x03, data, data.length, 2000, false);
            // read some data from the device
            // 0x84 is the endpoint address of the IN endpoint 4 (from PC to
            // device)
            // bit 7 (0x80) is set in case of an IN endpoint
//            dev.readInterrupt(0x84, readData, readData.length, 20000, true);
            dev.readBulk(0x86, readData, readData.length, 200, false);
            // log the data from the device
            logData(readData);
            // close the device
            dev.close();
        } catch (USBException e) {
            // if an exception occures during connect or read/write an exception
            // is thrown
            e.printStackTrace();
        }
    }

    //wont work
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
                interf.claim();
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

                    UsbIrp usbIrp = pipe.createUsbControlIrp(
                            (byte) (UsbConst.ENDPOINT_TYPE_INTERRUPT),
                            UsbConst.REQUEST_GET_CONFIGURATION,
                            (short) 0,
                            (short) 0
                    );

                    byte[] bytes = new byte[40];
                    usbIrp.setData(bytes);
                    pipe.asyncSubmit(usbIrp);
                    usbIrp.waitUntilComplete();
                    bytes = usbIrp.getData();
                    for (byte b : bytes)
                    {
                        System.out.print(b);
                    }

                    pipe.close();
                }
                interf.release();
            }
        }
        catch (Exception e)
        {
        }
    }

    private UsbPipeListener getUsbPipeListener()
    {
        return new UsbPipeListener()
        {
            @Override
            public void errorEventOccurred(UsbPipeErrorEvent usbPipeErrorEvent)
            {
                System.out.println(usbPipeErrorEvent.toString());
            }

            @Override
            public void dataEventOccurred(UsbPipeDataEvent usbPipeDataEvent)
            {
                byte[] data = usbPipeDataEvent.getData();
                for (byte b : data)
                {
                    System.out.print(b);
                }
                System.out.println();

            }
        };
    }

    //wont work
    private void commUSB(UsbDevice device) throws UsbException, UnsupportedEncodingException
    {
        UsbControlIrp irp = device.createUsbControlIrp(
                (byte) (UsbConst.ENDPOINT_TYPE_INTERRUPT | UsbConst.REQUESTTYPE_DIRECTION_IN
                        | UsbConst.REQUESTTYPE_TYPE_STANDARD
                        | UsbConst.REQUESTTYPE_RECIPIENT_ENDPOINT),
                UsbConst.REQUEST_GET_CONFIGURATION,
                (short) 0,
                (short) 0
        );

        byte[] ib = new byte[40];

        irp.setData(ib);
        device.asyncSubmit(irp);

        for (byte b : ib)
        {
            System.out.print(b);
        }
        System.out.println();
    }

    private static void dump(UsbDevice device)
    {
        UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
        System.out.format("%04x:%04x%n", desc.idVendor() & 0xffff, desc.idProduct() & 0xffff);
//        System.out.println(desc);
        if (device.isUsbHub())
        {
            UsbHub hub = (UsbHub) device;
            for (UsbDevice child : (List<UsbDevice>) hub.getAttachedUsbDevices())
            {
                dump(child);
            }
        }
    }

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

*/

    //endregion

    private void doIt()
    {
        startGUI();

        DeviceConnectionListener devConnListener = new DeviceConnectionListener(this);
        Thread devConnListenerThread = new Thread(devConnListener);
        devConnListenerThread.start();
    }

    private void startGUI()
    {
        MainWindow.initialize();
    }

    @Override
    public void deviceConnectionEvent(DeviceInfo connectedDevice,
                                      DeviceConnectionStateEnum deviceConnectionStateEnum)
    {
        ApplicationLogger.LOGGER.info(connectedDevice.devicePortName + " " + deviceConnectionStateEnum);

        if (deviceConnectionStateEnum == DeviceConnectionStateEnum.CONNECTED)
        {
            if (connectedDevice.deviceType == DeviceInfo.DeviceTypeEnum.COM)
            {
                //Create GUI for output
                OutputForm outputForm = new OutputForm();
                outputForm.startGUI();

                //Call Factory method and set form to out
                COMDeviceCommunication comDeviceCommunication = COMDeviceCommunication.getInstance(connectedDevice);
                comDeviceCommunication.rxRawDataReceiver.addObserver(outputForm);
                comDeviceCommunication.rxRawDataReceiver.addObserver(PacketLogger.getInstance());

                new Thread(comDeviceCommunication).start();
            }
        }
    }
}
