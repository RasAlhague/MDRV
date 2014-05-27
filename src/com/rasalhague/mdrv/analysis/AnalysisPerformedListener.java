package com.rasalhague.mdrv.analysis;

import com.rasalhague.mdrv.DeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * The interface Analysis performed listener.
 */
public interface AnalysisPerformedListener
{
    /**
     * Analysis performed event.
     *
     * @param analysisResult
     *         the analysis result
     */
    public void analysisPerformedEvent(LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>>> analysisResult);
}
