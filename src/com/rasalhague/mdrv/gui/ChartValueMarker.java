package com.rasalhague.mdrv.gui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

public class ChartValueMarker implements LineChartMouseListener
{
    private static ChartValueMarker          instance;
    private        LineChart<Number, Number> lineChart;
    private Line xMarker = new Line();
    private Line yMarker = new Line();
    private Pane   pane;

    private ChartValueMarker()
    { }

    public static ChartValueMarker getInstance()
    {
        if (instance == null)
        {
            instance = new ChartValueMarker();
        }

        return instance;
    }

    public void init(LineChart<Number, Number> lineChart)
    {
        this.lineChart = lineChart;
        this.pane = getPane();

        LineChartMouseTrigger.addLineChartMouseListener(this);

        configureMarkers();
        addMarkersToChart();
        setUpMarkersBehavior();
    }

    public void setXMarkerVisibility(boolean visibility)
    {
        xMarker.setVisible(visibility);
    }

    public void setYMarkerVisibility(boolean visibility)
    {
        yMarker.setVisible(visibility);
    }

    private class SizeListener implements ChangeListener
    {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue)
        {
            new Thread(() -> {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    configureMarkers();
                });
            }).start();
        }
    }

    @Override
    public void lineChartMouseEvent(Axis axis, double displayPosition)
    {
        if (xMarker.isVisible())
        {
            //TODO JavaFX uses CPU - 10%
            switch (axis)
            {
                case X:
                {
                    xMarker.setStartX(displayPosition);
                    xMarker.setEndX(displayPosition);

                    break;
                }
                case Y:
                {
                    yMarker.setStartY(displayPosition);
                    yMarker.setEndY(displayPosition);

                    break;
                }
            }
        }
    }

    private Pane getPane()
    {
        Parent p = lineChart.getParent();
        while (p.getClass() != AnchorPane.class)
        {
            p = p.getParent();
        }

        return (Pane) p;
    }

    private void configureMarkers()
    {
        // find chart area Node
        Node chartArea = lineChart.lookup(".chart-plot-background");
        Bounds chartAreaBounds = chartArea.localToScene(chartArea.getBoundsInLocal());

        // set x parameters of the valueMarker to chart area bounds
        xMarker.setStartY(chartAreaBounds.getMinY());
        xMarker.setEndY(chartAreaBounds.getMaxY());

        yMarker.setStartX(chartAreaBounds.getMinX());
        yMarker.setEndX(chartAreaBounds.getMaxX());
    }

    private void addMarkersToChart()
    {
        pane.getChildren().add(xMarker);
        pane.getChildren().add(yMarker);
    }

    private void setUpMarkersBehavior()
    {
        SizeListener sizeListener = new SizeListener();
        pane.widthProperty().addListener(sizeListener);
        pane.heightProperty().addListener(sizeListener);
    }
}
