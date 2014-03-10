package com.rasalhague.mdrv.constants;

public interface DeviceConstants
{
    /**
     * AirView2
     */
    public interface AirView2
    {
        static String PID                 = "0241";
        static String VID                 = "1F9B";
        static String NAME                = "Ubiquiti AirView2 (COM5)";
        static String PORT_TYPE           = "COM";
        static byte[] END_PACKET_SEQUENCE = new byte[]{'\n'};
    }

    /**
     * EZ430RF2500
     */
    public interface ez430RF2500
    {
        static String PID                 = "F432";
        static String VID                 = "0451";
        static String NAME                = "MSP430 Application UART (COM3)";
        static String PORT_TYPE           = "COM";
        static byte[] END_PACKET_SEQUENCE = new byte[]{'\n'};
    }

    /**
     * Unigen ISM Sniffer
     */
    public interface UnigenISMSniffer
    {
        static String PID                 = "2001";
        static String VID                 = "1C79";
        static String NAME                = "";
        static String PORT_TYPE           = "HID";
        static byte[] END_PACKET_SEQUENCE = new byte[]{0x00, 0x00};
    }

}