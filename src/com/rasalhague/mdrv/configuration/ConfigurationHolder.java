package com.rasalhague.mdrv.configuration;

import java.util.List;

public class ConfigurationHolder
{
    private List<DeviceConfigurationHolder> devicesConfiguration;
    private ApplicationConfigsHolder        applicationConfiguration;

    public DeviceConfigurationHolder getDeviceConfiguration(String productID, String vendorID)
    {
        for (DeviceConfigurationHolder devConfiguration : devicesConfiguration)
        {
            String pID = devConfiguration.getProductID();
            String vID = devConfiguration.getVendorID();

            if (pID.equals(productID) && vID.equals(vendorID))
            {
                return devConfiguration;
            }
        }

        //        ApplicationLogger.LOGGER.severe("Device configuration does not exist in config file!");

        return new DeviceConfigurationHolder();
    }

    public ApplicationConfigsHolder getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    public class ApplicationConfigsHolder
    {
        List<String> excludedFieldsList;

        public List<String> getExcludedFieldsList()
        {
            return excludedFieldsList;
        }
    }

    /**
     * Contains config fields Too add new config fields simple add field to class
     */
    public class DeviceConfigurationHolder
    {
        private String vendorID;
        private String productID;
        private byte[] endPacketSequence;
        private float  initialFrequency;
        private float  channelSpacing;

        public String getVendorID()
        {
            return vendorID;
        }

        public String getProductID()
        {
            return productID;
        }

        public byte[] getEndPacketSequence()
        {
            return endPacketSequence;
        }

        public float getInitialFrequency()
        {
            return initialFrequency;
        }

        public float getChannelSpacing()
        {
            return channelSpacing;
        }
    }
}