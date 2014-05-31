package com.rasalhague.mdrv.configuration;

import java.util.List;

/**
 * The type Configuration holder.
 * <p>
 * Used for serialize and deserialize configuration from/to config.cfg
 */
public class ConfigurationHolder
{
    private List<DeviceConfigurationHolder> devicesConfiguration;
    private ApplicationConfigsHolder        applicationConfiguration;

    /**
     * Gets device configuration.
     *
     * @param productID
     *         the product iD
     * @param vendorID
     *         the vendor iD
     *
     * @return the device configuration
     */
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

    /**
     * Gets application configuration.
     *
     * @return the application configuration
     */
    public ApplicationConfigsHolder getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    public class ApplicationConfigsHolder
    {
        List<String> excludedFieldsList;
        String       channelsToScan;

        public List<String> getExcludedFieldsList()
        {
            return excludedFieldsList;
        }

        public String getChannelsToScan()
        {
            return channelsToScan;
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

        /**
         * Gets vendor iD.
         *
         * @return the vendor iD
         */
        public String getVendorID()
        {
            return vendorID;
        }

        /**
         * Gets product iD.
         *
         * @return the product iD
         */
        public String getProductID()
        {
            return productID;
        }

        /**
         * Get end packet sequence.
         *
         * @return the byte [ ]
         */
        public byte[] getEndPacketSequence()
        {
            return endPacketSequence;
        }

        /**
         * Gets initial frequency.
         *
         * @return the initial frequency
         */
        public float getInitialFrequency()
        {
            return initialFrequency;
        }

        /**
         * Gets channel spacing.
         *
         * @return the channel spacing
         */
        public float getChannelSpacing()
        {
            return channelSpacing;
        }
    }
}
