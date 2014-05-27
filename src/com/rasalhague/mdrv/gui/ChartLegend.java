package com.rasalhague.mdrv.gui;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that control chart legend generation
 */
public class ChartLegend
{
    private static final ChartLegend ourInstance = new ChartLegend();
    private VBox chartLegendVbox;
    private final HashMap<String, ArrayList<CheckBox>> devNameToCheckBoxMap = new HashMap<>();

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static ChartLegend getInstance()
    {
        return ourInstance;
    }

    private ChartLegend()
    {
    }

    /**
     * Init chart legend.
     *
     * @param chartLegendVbox
     *         the chart legend vbox
     * @param lineChart
     *         the line chart
     */
    public void initChartLegend(VBox chartLegendVbox, LineChart<Number, Number> lineChart)
    {
        this.chartLegendVbox = chartLegendVbox;

        /**
         * Update legend list according to lineChart series
         */
        lineChart.getData().addListener((Observable observable) -> {

            ArrayList<XYChart.Series<Number, Number>> seriesArrayList = new ArrayList<>((java.util.Collection<? extends XYChart.Series<Number, Number>>) observable);

            /**
             * Delay needed for _TODO Color wont work coz first series return wrong data
             */
            int updateDelayMs = 50;
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.schedule(() -> Platform.runLater(() -> {

                XYChart.Series<Number, Number> series = seriesArrayList.get(seriesArrayList.size() - 1);

                //get dev name
                String seriesName = series.getName();
                Matcher matcher = Pattern.compile("(?<devName>.*) (?<type>.*)").matcher(seriesName);
                String devName = "";
                String type = "";

                while (matcher.find())
                {
                    devName = matcher.group("devName");
                    type = matcher.group("type");
                }

                generateChartLegendVBox(series, devName, type);

            }), updateDelayMs, TimeUnit.MILLISECONDS);
        });
    }

    /**
     * Clear chart legend.
     */
    public void clearChartLegend()
    {
        chartLegendVbox.getChildren().clear();
    }

    private void generateChartLegendVBox(XYChart.Series<Number, Number> series, String devName, String type)
    {
        //Get numberSeries color
        String numberSeriesString = series.getNode().toString();
        int indexOf = numberSeriesString.indexOf("stroke=");
        String colorValue = numberSeriesString.substring(indexOf + 7, indexOf + 17);

        CheckBox checkBox = new CheckBox();
        VBox vBox;
        HBox hBox;

        int generalCheckboxIndex = findGeneralCheckbox(devName);
        ObservableList<Node> chartLegendVboxChildren = chartLegendVbox.getChildren();
        if (generalCheckboxIndex != -1)
        {
            vBox = (VBox) chartLegendVboxChildren.get(generalCheckboxIndex);
            hBox = (HBox) vBox.getChildren().get(1);
        }
        else
        {
            //create container
            vBox = new VBox();
            hBox = new HBox();
            CheckBox generalCheckBox = new CheckBox();

            //containing
            vBox.getChildren().add(generalCheckBox);
            vBox.getChildren().add(hBox);

            //configuring
            vBox.setId(devName);
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(3, 3, 3, 3));
            vBox.setSpacing(3);
            vBox.setStyle("-fx-border-color: rgba(200, 200, 200, 1);" + "-fx-border-width: 1;");

            hBox.setSpacing(3);

            generalCheckBox.setText(devName);
            generalCheckBox.selectedProperty().addListener(observable -> {

                for (CheckBox box : devNameToCheckBoxMap.get(devName))
                {
                    box.setSelected(generalCheckBox.isSelected());
                }

            });

            chartLegendVbox.getChildren().add(vBox);
            devNameToCheckBoxMap.put(devName, new ArrayList<>());
        }

        checkBox.setText(type);
        checkBox.setTextFill(Paint.valueOf(colorValue));

        if (type.equals("max"))
        {
            checkBox.setSelected(true);
        }

        series.getNode().setVisible(checkBox.isSelected());
        checkBox.selectedProperty().addListener(observable -> {

            series.getNode().setVisible(checkBox.isSelected());
        });

        hBox.getChildren().add(checkBox);

        devNameToCheckBoxMap.get(devName).add(checkBox);
    }

    private int findGeneralCheckbox(String devName)
    {
        ObservableList<Node> chartLegendVboxChildren = chartLegendVbox.getChildren();
        for (int i = 0; i < chartLegendVboxChildren.size(); i++)
        {
            Node chartLegendVboxChild = chartLegendVboxChildren.get(i);
            if (chartLegendVboxChild.getId().equals(devName))
            {
                return i;
            }
        }
        return -1;
    }
}
