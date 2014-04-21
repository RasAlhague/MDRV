package com.rasalhague.mdrv.analysis;

import com.rasalhague.mdrv.DeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public interface AnalysisPerformedListener
{
    public void analysisPerformedEvent(LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>>> analysisResult);
}
