package com.rasalhague.mdrv.devices;

import java.util.ArrayList;

public interface Parsable
{
    public ArrayList<Byte> parse(ArrayList<Byte> dataToParse);
}
