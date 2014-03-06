package com.rasalhague.mdrv.temp;

import com.rasalhague.mdrv.DataPacket;
import com.xeiam.xchart.Chart;
import com.xeiam.xchart.StyleManager;
import com.xeiam.xchart.SwingWrapper;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class XChartVisualizer implements Observer
{
    private Chart chart;
    private int seriesCounter = 0;

    public Chart getChart()
    {
        return chart;
    }

    public XChartVisualizer()
    {
        chart = new Chart(900, 400);
        //        chart.setChartTitle("LineChart03");
        //        chart.getStyleManager().setChartTitleFont(new Font(Font.DIALOG, Font.BOLD, 24));

        StyleManager styleManager = chart.getStyleManager();
        styleManager.setYAxisMax(-60);
        styleManager.setYAxisMin(-120);
    }

    public XChartVisualizer(int width, int height)
    {
        chart = new Chart(width, height);
    }

    boolean once = true;
    JFrame swingWrapper;

    private void view(ArrayList<DataPacket> dataToView)
    {
        ArrayList<Number> xData = new ArrayList<Number>();
        for (int i = 0; i < dataToView.get(dataToView.size() - 1).getDataPacketValues().size(); i++)
        {
            xData.add(i);
        }
        chart.getSeriesMap().clear();
        chart.addSeries(String.valueOf(seriesCounter),
                        xData,
                        dataToView.get(dataToView.size() - 1).getDataPacketValues());

        seriesCounter++;

        if (!once)
        {
            swingWrapper.repaint();
        }
        else
        {
            chart.setChartTitle(dataToView.get(0).getDeviceInfo().getDeviceName());
            swingWrapper = new SwingWrapper(getChart()).displayChart();
            once = false;
        }
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (arg instanceof ArrayList)
        {
            view((ArrayList<DataPacket>) arg);
        }
    }
}
