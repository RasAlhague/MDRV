package com.rasalhague.mdrv.analysis;

import com.rasalhague.mdrv.DeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;

public interface AnalysisPerformedListener
{
    public void analysisPerformedEvent(HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> analysisResult);
}
