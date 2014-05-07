package com.rasalhague.mdrv.wirelessadapter;

public class WirelessAdapterData
{
    private byte   channel;
    private byte   dB;
    private float  bps;
    private float  frequency;
    private String standart;

    public byte getChannel()
    {
        return channel;
    }

    public byte getdB()
    {
        return dB;
    }

    public float getBps()
    {
        return bps;
    }

    public float getFrequency()
    {
        return frequency;
    }

    public String getStandart()
    {
        return standart;
    }

    public WirelessAdapterData(byte channel, byte dB, float bps, float frequency, String standart)
    {
        this.channel = channel;
        this.dB = dB;
        this.bps = bps;
        this.frequency = frequency;
        this.standart = standart;
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
                '}';
    }
}
