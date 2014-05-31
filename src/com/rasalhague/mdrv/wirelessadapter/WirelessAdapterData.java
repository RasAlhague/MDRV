package com.rasalhague.mdrv.wirelessadapter;

/**
 * The type Wireless adapter data.
 * <p>
 * Used as data structure from tcpdump output
 */
public class WirelessAdapterData
{
    private final byte   channel;
    private final byte   dB;
    private final float  bps;
    private final float  frequency;
    private final String standart;
    private final String BSSID;

    /**
     * Gets channel.
     *
     * @return the channel
     */
    public byte getChannel()
    {
        return channel;
    }

    /**
     * Gets b.
     *
     * @return the b
     */
    public byte getdB()
    {
        return dB;
    }

    /**
     * Gets bps.
     *
     * @return the bps
     */
    public float getBps()
    {
        return bps;
    }

    /**
     * Gets frequency.
     *
     * @return the frequency
     */
    public float getFrequency()
    {
        return frequency;
    }

    /**
     * Gets standart.
     *
     * @return the standart
     */
    public String getStandart()
    {
        return standart;
    }

    /**
     * Gets bSSID.
     *
     * @return the bSSID
     */
    public String getBSSID()
    {
        return BSSID;
    }

    /**
     * Instantiates a new Wireless adapter data.
     *
     * @param channel
     *         the channel
     * @param dB
     *         the d b
     * @param bps
     *         the bps
     * @param frequency
     *         the frequency
     * @param standart
     *         the standart
     * @param BSSID
     *         the bSSID
     */
    public WirelessAdapterData(byte channel, byte dB, float bps, float frequency, String standart, String BSSID)
    {
        this.channel = channel;
        this.dB = dB;
        this.bps = bps;
        this.frequency = frequency;
        this.standart = standart;
        this.BSSID = BSSID;
    }

    @Override
    public String toString()
    {
        return "WirelessAdapterData{" +
                "channel=" + channel +
                ", dB=" + dB +
                ", bps=" + bps +
                ", frequency=" + frequency +
                ", standart='" + standart + '\'' +
                ", BSSID='" + BSSID + '\'' +
                '}';
    }

}
