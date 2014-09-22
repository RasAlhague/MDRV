package com.rasalhague.mdrv.gui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class ChartTooltip implements LineChartMousePointsListener
{
    private static ChartTooltip ourInstance = new ChartTooltip();
    private TableView                 chartTooltip;
    private LineChart<Number, Number> lineChart;
    private double chartTooltipXShift = 30;
    private double chartTooltipYShift = -30;

    public static ChartTooltip getInstance()
    {
        if (ourInstance == null)
        {
            ourInstance = new ChartTooltip();
        }

        return ourInstance;
    }

    private ChartTooltip()
    { }

    public void init(LineChart<Number, Number> lineChart)
    {
        this.lineChart = lineChart;

        LineChartMouseTrigger.addLineChartMousePointsListener(this);

        createChartTooltip();
    }

    public class Point
    {
        private SimpleDoubleProperty x;
        private SimpleDoubleProperty y;

        public Point(double x, double y)
        {
            this.x = new SimpleDoubleProperty(x);
            this.y = new SimpleDoubleProperty(y);
        }

        public double getX()
        {
            return x.get();
        }

        public SimpleDoubleProperty xProperty()
        {
            return x;
        }

        public double getY()
        {
            return y.get();
        }

        public SimpleDoubleProperty yProperty()
        {
            return y;
        }
    }

    @Override
    public void lineChartMousePointEvent(Axis axis,
                                         double displayPosition,
                                         ArrayList<XYChart.Data<Number, Number>> points,
                                         MouseEvent mouseEvent)
    {
        if (axis == Axis.X)
        {
            chartTooltip.setLayoutX(mouseEvent.getSceneX() + chartTooltipXShift);
            chartTooltip.setLayoutY(mouseEvent.getSceneY() + chartTooltipYShift);

            ArrayList<Point> pointArrayList = new ArrayList<>();
            for (XYChart.Data<Number, Number> point : points)
            {
                pointArrayList.add(new Point(point.getXValue().doubleValue(), point.getYValue().doubleValue()));
            }

            ObservableList<Point> observableList = FXCollections.observableArrayList(pointArrayList);

            chartTooltip.setItems(observableList);
        }
    }

    private void createChartTooltip()
    {
        chartTooltip = new TableView();
        chartTooltip.setMouseTransparent(true);
        chartTooltip.setPrefHeight(150);
        chartTooltip.setPrefWidth(180);
        chartTooltip.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn firstColumn = new TableColumn("RSSI, dBm");
        TableColumn secondColumn = new TableColumn("F, kHz");

        firstColumn.setCellValueFactory(new PropertyValueFactory<Point, Double>("x"));
        secondColumn.setCellValueFactory(new PropertyValueFactory<Point, Double>("y"));

        chartTooltip.getColumns().addAll(firstColumn, secondColumn);

        getPane().getChildren().add(chartTooltip);
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

}
