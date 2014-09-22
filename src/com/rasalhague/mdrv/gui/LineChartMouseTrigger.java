package com.rasalhague.mdrv.gui;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class LineChartMouseTrigger
{
    private static List<LineChartMouseListener>       lineChartMouseListeners       = new ArrayList<>();
    private static List<LineChartMousePointsListener> lineChartMousePointsListeners = new ArrayList<>();

    public static void addLineChartMouseListener(LineChartMouseListener listener)
    {
        lineChartMouseListeners.add(listener);
    }

    public static void addLineChartMousePointsListener(LineChartMousePointsListener listener)
    {
        lineChartMousePointsListeners.add(listener);
    }

    private static void notifyLineChartMousePointsListeners(Axis axis,
                                                            double displayPosition,
                                                            ArrayList<XYChart.Data<Number, Number>> points,
                                                            MouseEvent mouseEvent)
    {
        for (LineChartMousePointsListener lineChartMousePointsListener : lineChartMousePointsListeners)
        {
            lineChartMousePointsListener.lineChartMousePointEvent(axis, displayPosition, points, mouseEvent);
        }
    }

    private static void notifyLineChartMouseListeners(Axis axis, double displayPosition)
    {
        for (LineChartMouseListener lineChartMouseListener : lineChartMouseListeners)
        {
            lineChartMouseListener.lineChartMouseEvent(axis, displayPosition);
        }
    }

    private LineChartMouseTrigger()
    { }

    public static void addChartToListen(LineChart lineChart)
    {
        // find chart area Node
        Node chartArea = lineChart.lookup(".chart-plot-background");
        Bounds chartAreaBounds = chartArea.localToScene(chartArea.getBoundsInLocal());

        // remember scene position of chart area
        double xMarkerShift = chartAreaBounds.getMinX();
        double yMarkerShift = chartAreaBounds.getMinY();

        lineChart.setOnMouseMoved(new EventHandler<MouseEvent>()
        {
            double mouseSceneX;
            double mouseSceneY;
            double shiftedMouseSceneX;
            double shiftedMouseSceneY;
            javafx.scene.chart.Axis<Number> xAxis;
            javafx.scene.chart.Axis<Number> yAxis;
            ObservableList<XYChart.Series<Number, Number>> lineChartData;

            @Override
            public void handle(MouseEvent event)
            {
                mouseSceneX = event.getSceneX();
                mouseSceneY = event.getSceneY();
                shiftedMouseSceneX = event.getSceneX() - xMarkerShift;
                shiftedMouseSceneY = event.getSceneY() - yMarkerShift;
                xAxis = lineChart.getXAxis();
                yAxis = lineChart.getYAxis();
                lineChartData = lineChart.getData();

                ArrayList<XYChart.Data<Number, Number>> xPoints = new ArrayList<>();
                ArrayList<XYChart.Data<Number, Number>> yPoints = new ArrayList<>();

                for (XYChart.Series<Number, Number> series : lineChartData)
                {
                    if (series.getNode().isVisible())
                    {
                        ObservableList<XYChart.Data<Number, Number>> seriesData = series.getData();
                        for (XYChart.Data<Number, Number> point : seriesData)
                        {
                            double xAxisDisplayPosition = xAxis.getDisplayPosition(point.getXValue());
                            double yAxisDisplayPosition = yAxis.getDisplayPosition(point.getYValue());

                            if (shiftedMouseSceneX == xAxisDisplayPosition)
                            {
                                xPoints.add(point);
                                notifyLineChartMouseListeners(Axis.X, mouseSceneX);
                            }
                            if (shiftedMouseSceneY == yAxisDisplayPosition)
                            {
                                yPoints.add(point);
                                notifyLineChartMouseListeners(Axis.Y, mouseSceneY);
                            }
                        }

                        if (xPoints.size() != 0)
                        {
                            notifyLineChartMousePointsListeners(Axis.X, mouseSceneX, xPoints, event);
                        }
                        if (yPoints.size() != 0)
                        {
                            notifyLineChartMousePointsListeners(Axis.Y, mouseSceneY, yPoints, event);
                        }
                    }
                }
            }
        });
    }
}

interface LineChartMousePointsListener
{
    void lineChartMousePointEvent(Axis axis,
                                  double displayPosition,
                                  ArrayList<XYChart.Data<Number, Number>> point,
                                  MouseEvent mouseEvent);

}

interface LineChartMouseListener
{
    void lineChartMouseEvent(Axis axis, double displayPosition);
}

enum Axis
{
    X,
    Y;
}

enum LineChartMouseListenerKey
{
    AXIS,
    DISPLAY_POSITION,
    POINTS,
    MOUSE_EVENT;
}