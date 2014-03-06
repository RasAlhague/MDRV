package com.rasalhague.mdrv.temp;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;

public class JFreeCharTest
{
    void view()
    {
        DefaultXYDataset xyDataset = new DefaultXYDataset();
        //        double[] doubles = ArrayUtils.toPrimitive(dataToView.toArray(new Double[dataToView.size()]));
        double[][] doubles = new double[][]{{1, 2}, {5, 10}};
        xyDataset.addSeries("1", doubles);

        JFreeChart objChart = ChartFactory.createXYLineChart("Name",
                                                             "X",
                                                             "Y",
                                                             xyDataset,
                                                             PlotOrientation.HORIZONTAL,
                                                             true,
                                                             true,
                                                             false);
        ChartFrame frame = new ChartFrame("Demo", objChart);

        frame.pack();
        frame.setVisible(true);
    }
}
