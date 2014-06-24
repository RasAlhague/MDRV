package com.rasalhague.mdrv.dev_communication;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import jssc.SerialPortException;

/**
 * Result of the run method must be call to RxRawDataReceiver
 */
public abstract class DeviceCommunication implements Runnable
{
    /**
     * The Device info.
     */
    final DeviceInfo        deviceInfo;
    /**
     * The Rx raw data receiver.
     */
    final RxRawDataReceiver rxRawDataReceiver;

    /**
     * Instantiates a new Device communication.
     *
     * @param devInfo
     *         the dev info
     */
    DeviceCommunication(DeviceInfo devInfo)
    {
        deviceInfo = devInfo;
        rxRawDataReceiver = new RxRawDataReceiver(deviceInfo);
    }

    /**
     * Gets rx raw data receiver.
     *
     * @return the rx raw data receiver
     */
    public RxRawDataReceiver getRxRawDataReceiver()
    {
        return rxRawDataReceiver;
    }

    /**
     * Factory method. Choose device.
     *
     * @param deviceInfo
     *         the device info
     *
     * @return the instance
     */
    public static DeviceCommunication getInstance(DeviceInfo deviceInfo)
    {
        //TODO Hardcoded
        String AirView2PID = "0241";
        String eZ430PID = "F432";
        String ISMSnifferPID = "2001";
        String MetaGeek_WiSpy24x2PID = "2410";

        String devPid = deviceInfo.getProductID();

        if (deviceInfo.getDeviceType() == DeviceInfo.DeviceType.DUMMY)
        {
            return new DummyDeviceCommunication(deviceInfo);
        }

        if (deviceInfo.getDeviceType() == DeviceInfo.DeviceType.COM)
        {
            //AirView2 -- USB\VID_1F9B&PID_0241&REV_8888
            if (devPid.equals(AirView2PID))
            {
                return new AirView2(deviceInfo);
            }

            //eZ430 -- USB\VID_0451&PID_F432&MI_00
            if (devPid.equals(eZ430PID))
            {
                return new ez430RF2500(deviceInfo);
            }

            ApplicationLogger.LOGGER.warning(
                    "Device not specified. Getting COMDeviceCommunication to try to work with COM device.");

            return new COMDeviceCommunication(deviceInfo);
        }
        else if (deviceInfo.getDeviceType() == DeviceInfo.DeviceType.HID)
        {
            if (devPid.equals(ISMSnifferPID))
            {
                return new ISMSniffer(deviceInfo);
            }

            if (devPid.equals(MetaGeek_WiSpy24x2PID))
            {
                return new MetaGeek_WiSpy24x2(deviceInfo);
            }

            //            ApplicationLogger.LOGGER.warning(
            //                    "Device not specified. Getting HIDDeviceCommunication to try to work with HID device.");
            //
            //            return new HIDDeviceCommunication(deviceInfo);
        }

        return null;
    }

    /**
     * Initialize device.
     *
     * @throws SerialPortException
     *         the serial port exception
     */
    abstract void initializeDevice() throws SerialPortException;
}