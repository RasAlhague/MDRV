package com.rasalhague.mdrv.device.core;

import java.util.ArrayList;

public interface Parsable
{
    public ArrayList<Byte> parse(ArrayList<Byte> dataToParse);
}
