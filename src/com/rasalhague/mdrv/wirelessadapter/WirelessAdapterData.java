package com.rasalhague.mdrv.wirelessadapter;

public class WirelessAdapterData
{
    private byte channel;
    private byte dB;
    private int  IV;

    public byte getChannel()
    {
        return channel;
    }

    public byte getdB()
    {
        return dB;
    }

    public int getIV()
    {
        return IV;
    }

    public WirelessAdapterData(byte channel, byte dB, int IV)
    {
        this.channel = channel;
        this.dB = dB;
        this.IV = IV;
    }

    @Override
    public String toString()
    {
        return "WirelessAdapterData{" +
                "channel=" + channel +
                ", dB=" + dB +
                ", IV=" + IV +
                '}';
    }
}
