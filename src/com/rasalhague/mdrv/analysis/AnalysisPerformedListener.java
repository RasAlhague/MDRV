package com.rasalhague.mdrv.analysis;

import com.rasalhague.mdrv.DeviceInfo;
import org.apache.commons.collections4.OrderedMap;

import java.util.ArrayList;
import java.util.HashMap;

public interface AnalysisPerformedListener
{
    public void analysisPerformedEvent(OrderedMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>>> analysisResult);
}
