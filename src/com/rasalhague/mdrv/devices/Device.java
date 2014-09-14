package com.rasalhague.mdrv.devices;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.dev_communication.DeviceCommunication;
import com.rasalhague.mdrv.dev_communication.RxRawDataReceiver;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import org.reflections.Reflections;

import java.util.Set;

public abstract class Device implements Initializable, Parsable
{
    private static final String FRIENDLY_NAME_FIELD_NAME       = "FRIENDLY_NAME";
    private static final String VENDOR_ID_FIELD_NAME           = "VENDOR_ID";
    private static final String PRODUCT_ID_FIELD_NAME          = "PRODUCT_ID";
    private static final String CHANNEL_SPACING_FIELD_NAME     = "CHANNEL_SPACING";
    private static final String END_PACKET_SEQUENCE_FIELD_NAME = "END_PACKET_SEQUENCE";
    private static final String INITIAL_FREQUENCY_FIELD_NAME   = "INITIAL_FREQUENCY";

    private static final String REFLECTION_INIT_PATH = "com.rasalhague.mdrv.devices";

    private DeviceCommunication deviceCommunication;
    private DeviceInfo          deviceInfo;

    private static DeviceHistory deviceHistory = new DeviceHistory();

    public void initializeObject(DeviceInfo deviceInfo)
    {
        this.deviceInfo = deviceInfo;
        this.deviceCommunication = DeviceCommunication.getInstance(deviceInfo);

        deviceHistory.checkForCollision(this.deviceInfo);
    }

    public static Device getConcreteDevice(DeviceInfo deviceInfo)
    {
        Reflections reflections = new Reflections(REFLECTION_INIT_PATH);
        Set<Class<? extends Device>> devicesClassSet = reflections.getSubTypesOf(Device.class);

        for (Class<? extends Device> concreteDeviceClass : devicesClassSet)
        {
            try
            {
                String vendorId = (String) concreteDeviceClass.getField(VENDOR_ID_FIELD_NAME).get(null);
                String productId = (String) concreteDeviceClass.getField(PRODUCT_ID_FIELD_NAME).get(null);
                String friendlyName = (String) concreteDeviceClass.getField(FRIENDLY_NAME_FIELD_NAME).get(null);
                float channelSpacing = concreteDeviceClass.getField(CHANNEL_SPACING_FIELD_NAME).getFloat(null);
                float initialFrequency = concreteDeviceClass.getField(INITIAL_FREQUENCY_FIELD_NAME).getFloat(null);
                byte[] endPacketSequence = (byte[]) concreteDeviceClass.getField(END_PACKET_SEQUENCE_FIELD_NAME)
                                                                       .get(null);

                if (deviceInfo.getProductID().equals(productId) && deviceInfo.getVendorID().equals(vendorId))
                {
                    Device device = concreteDeviceClass.newInstance();
                    deviceInfo.setSomeFields(friendlyName, endPacketSequence, initialFrequency, channelSpacing, device);
                    device.initializeObject(deviceInfo);

                    return device;
                }
            }
            catch (IllegalAccessException | NoSuchFieldException | InstantiationException e)
            {
                ApplicationLogger.LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        if (!deviceInfo.equals(device.deviceInfo)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return deviceInfo.hashCode();
    }

    public DeviceCommunication getDeviceCommunication()
    {
        return deviceCommunication;
    }

    public RxRawDataReceiver getRxRawDataReceiver()
    {
        return deviceCommunication.getRxRawDataReceiver();
    }

    public DeviceInfo getDeviceInfo()
    {
        return deviceInfo;
    }
}