package com.rasalhague.mdrv.gui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

public class ChartValueMarker
{
    private LineChart<Number, Number> lineChart;
    private Line xMarker = new Line();
    private Line yMarker = new Line();
    private double xMarkerShift;
    private double yMarkerShift;
    private Pane   pane;

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

    public ChartValueMarker(LineChart<Number, Number> lineChart)
    {
        this.lineChart = lineChart;
        this.pane = getPane();

        configureMarkers();
        addMarkersToChart();
        setUpMarkersBehavior();
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

        // remember scene position of chart area
        xMarkerShift = chartAreaBounds.getMinX();
        yMarkerShift = chartAreaBounds.getMinY();

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
        lineChart.setOnMouseMoved(new EventHandler<MouseEvent>()
        {
            double mouseSceneX;
            double mouseSceneY;
            double shiftedMouseSceneX;
            double shiftedMouseSceneY;
            Axis<Number> xAxis;
            Axis<Number> yAxis;
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

                for (XYChart.Series<Number, Number> series : lineChartData)
                {
                    ObservableList<XYChart.Data<Number, Number>> seriesData = series.getData();
                    for (XYChart.Data<Number, Number> point : seriesData)
                    {
                        if (shiftedMouseSceneX == xAxis.getDisplayPosition(point.getXValue()))
                        {
                            //TODO JavaFX uses CPU - 10%
                            xMarker.setStartX(mouseSceneX);
                            xMarker.setEndX(mouseSceneX);
                        }
                        if (shiftedMouseSceneY == yAxis.getDisplayPosition(point.getYValue()))
                        {
                            //TODO JavaFX uses CPU - 10%
                            yMarker.setStartY(mouseSceneY);
                            yMarker.setEndY(mouseSceneY);
                        }
                    }
                }
            }
        });

        SizeListener sizeListener = new SizeListener();
        pane.widthProperty().addListener(sizeListener);
        pane.heightProperty().addListener(sizeListener);
    }
}
