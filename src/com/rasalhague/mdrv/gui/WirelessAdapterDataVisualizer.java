package com.rasalhague.mdrv.gui;

import com.rasalhague.mdrv.wirelessadapter.WirelessAdapterData;
import com.rasalhague.mdrv.wirelessadapter.WirelessAdapterDataListener;

public class WirelessAdapterDataVisualizer implements WirelessAdapterDataListener
{

    @Override
    public void wirelessAdapterDataEvent(WirelessAdapterData wirelessAdapterData)
    {
        processWirelessAdapterData(wirelessAdapterData);
    }

    private void processWirelessAdapterData(WirelessAdapterData wirelessAdapterData)
    {

    }
}
