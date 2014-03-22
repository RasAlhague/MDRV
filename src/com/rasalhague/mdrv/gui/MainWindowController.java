package com.rasalhague.mdrv.gui;

import com.rasalhague.mdrv.DeviceConnectionListener;
import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.Utils;
import com.rasalhague.mdrv.analysis.AnalysisKey;
import com.rasalhague.mdrv.analysis.AnalysisPerformedListener;
import com.rasalhague.mdrv.analysis.PacketAnalysis;
import com.rasalhague.mdrv.configuration.ConfigurationLoader;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import com.rasalhague.mdrv.logging.TextAreaHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class MainWindowController extends Application implements AnalysisPerformedListener
{
    private static MainWindowController      instance;
    public         LineChart<Number, Number> lineChart;
    public         TextArea                  debugTextArea;
    public         CheckBox                  maxCheckBox;
    public         Button                    startListeningButton;
    public         Button                    refreshChartButton;

    public MainWindowController()
    {
        instance = this;
    }

    public static void main(String[] args)
    {
        launch();
    }

    public static MainWindowController getInstance()
    {
        if (instance == null)
        {
            //            return instance = new MainWindowController();
            return null;
        }

        return instance;
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        ApplicationLogger.setup();
        ConfigurationLoader.initialize();

        final String tooltipCssPath = "/ToolTip.css";
        final String lineChartCssPath = "/LineChart.css";

        Parent root = FXMLLoader.load(getClass().getResource("/com/rasalhague/mdrv/gui/view/MainWindow.fxml"));
        final Scene scene = new Scene(root);
        scene.getStylesheets().add(tooltipCssPath);
        scene.getStylesheets().add(lineChartCssPath);

        /**
         * On close actions
         */
        stage.setOnCloseRequest(windowEvent -> {
            //Correctly close file handlers
            ApplicationLogger.closeHandlers();

            //Need to stop coz thread prevent exit program
            if (DeviceConnectionListener.isListening())
            {
                DeviceConnectionListener.stopListening();
            }

            Platform.exit();
        });

        stage.setScene(scene);
        stage.show();

        //init GUI objects
        lineChart = (LineChart<Number, Number>) scene.lookup("#lineChart");
        debugTextArea = (TextArea) scene.lookup("#debugTextArea");
        maxCheckBox = (CheckBox) scene.lookup("#maxCheckBox");

        bindTooltipToLineChart(lineChart);

        //Add listeners and handlers
        ApplicationLogger.addCustomHandler(new TextAreaHandler(debugTextArea));
        PacketAnalysis.getInstance().addListener(getInstance());

        //fake button press
        startListeningButtonMouseClickedEvent(new Event(EventType.ROOT));
    }

    public void startListeningButtonMouseClickedEvent(Event event)
    {
        DeviceConnectionListener.startListening();
    }

    public void maxCheckBoxChangedEvent(ActionEvent event)
    {
        CheckBox checkBox = (CheckBox) event.getSource();
        System.out.println(maxCheckBox.isSelected());
    }

    public void refreshChartButtonClickEvent(Event event)
    {
        PacketAnalysis.getInstance().getAnalysisResultsMap().clear();
    }

    @Override
    public synchronized void analysisPerformedEvent(final HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> analysisResult)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public synchronized void run()
            {

                ObservableList<XYChart.Series<Number, Number>> lineChartData = lineChart.getData();

                /**
                 * For every device that processed by Analysis class
                 */
                Set<DeviceInfo> keySet = analysisResult.keySet();
                for (DeviceInfo deviceInfo : keySet)
                {
                    if (maxCheckBox.isSelected())
                    {

                        //Generate XYChart.Series
                        ArrayList<Integer> listMax = analysisResult.get(deviceInfo).get(AnalysisKey.MAX);
                        int points = analysisResult.get(deviceInfo).get(AnalysisKey.MAX).size();

                        XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
                        ObservableList<XYChart.Data<Number, Number>> seriesData = series.getData();
                        series.setName(deviceInfo.getName());

                        final double spacing = (85.0 / points)/*(double)Math.round((85.0 / points) * 1000) / 1000*/;
                        double xAxisCounter = 0.0;
                        for (Integer value : listMax)
                        {
                            XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>((double) Math.round((xAxisCounter) * 1000) / 1000,
                                                                                                 value);
                            seriesData.add(data);

                            xAxisCounter += spacing;
                        }

                        //Use XYChart.Series
                        if (Utils.isSeriesExist(lineChartData, deviceInfo.getName()))
                        {
                            for (XYChart.Series<Number, Number> numberSeries : lineChartData)
                            {
                                if (numberSeries.getName().equals(deviceInfo.getName()))
                                {
                                    ObservableList<XYChart.Data<Number, Number>> data = numberSeries.getData();
                                    for (int i = 0; i < data.size(); i++)
                                    {
                                        XYChart.Data<Number, Number> numberData = data.get(i);
                                        numberData.setYValue(series.getData().get(i).getYValue());
                                    }
                                }
                            }
                        }
                        else
                        {
                            lineChartData.add(series);
                        }
                    }
                }
            }
        });
    }

    private void bindTooltipToLineChart(final LineChart<Number, Number> lineChart)
    {
        final Axis<Number> xAxis = lineChart.getXAxis();
        final Axis<Number> yAxis = lineChart.getYAxis();

        final Tooltip tooltip = new Tooltip();

        final Node chartBackground = lineChart.lookup(".chart-plot-background");
        for (Node n : chartBackground.getParent().getChildrenUnmodifiable())
        {
            if (n != chartBackground && n != xAxis && n != yAxis)
            {
                n.setMouseTransparent(true);
            }
        }

        chartBackground.setOnMouseEntered(mouseEvent -> {
            tooltip.show(lineChart, mouseEvent.getScreenX(), mouseEvent.getScreenY());
        });

        chartBackground.setOnMouseMoved(mouseEvent -> {
            StringBuilder sToOut = new StringBuilder();

            Double xAxisValueForDisplay = (Double) xAxis.getValueForDisplay(mouseEvent.getX());
            Double yAxisValueForDisplay = (Double) yAxis.getValueForDisplay(mouseEvent.getY());

            sToOut.append("X: ");
            sToOut.append(xAxisValueForDisplay);
            sToOut.append('\t');
            sToOut.append("Y: ");
            sToOut.append(yAxisValueForDisplay);
            sToOut.append('\n');
            sToOut.append('\n');

            ObservableList<XYChart.Series<Number, Number>> lineChartData = lineChart.getData();
            for (XYChart.Series<Number, Number> numberSeries : lineChartData)
            {
                ObservableList<XYChart.Data<Number, Number>> data = numberSeries.getData();
                for (XYChart.Data<Number, Number> numberData : data)
                {
                    Double xValue = (Double) numberData.getXValue();
                    //                        Double yValue = (Double) numberData.getYValue();
                    double range = 0.25;
                    Effect effect = null;

                    if (xValue > xAxisValueForDisplay - range && xValue < xAxisValueForDisplay + range)
                    {
                        sToOut.append("RSSI: ");
                        sToOut.append(String.valueOf(numberData.getYValue()));
                        sToOut.append('\t');
                        sToOut.append("Freq: ");
                        sToOut.append(String.valueOf(numberData.getXValue()));
                        sToOut.append(" - ");
                        sToOut.append(numberSeries.getName());
                        sToOut.append('\n');

                        effect = new DropShadow();
                        numberData.getNode().setVisible(true);
                        numberData.getNode().setEffect(effect);
                    }
                    else
                    {
                        numberData.getNode().setVisible(false);
                        numberData.getNode().setEffect(effect);
                    }
                }
            }

            //Wont refresh into null string
            if (sToOut.length() != 0)
            {
                tooltip.setText(sToOut.toString());
            }
            tooltip.setX(mouseEvent.getScreenX() + 30);
            tooltip.setY(mouseEvent.getScreenY() + 10);

            //Manage visibility when empty string
            if (tooltip.getText().length() == 0 && tooltip.isShowing())
            {
                tooltip.hide();
            }
            else if (tooltip.getText().length() > 0 && !tooltip.isShowing())
            {
                tooltip.show(lineChart, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
        });

        chartBackground.setOnMouseExited(mouseEvent -> {
            tooltip.hide();
        });

        //        xAxis.setOnMouseEntered(new EventHandler<MouseEvent>()
        //        {
        //            @Override
        //            public void handle(MouseEvent mouseEvent)
        //            {
        //                tooltip.show(lineChart, mouseEvent.getScreenX(), mouseEvent.getScreenY());
        //            }
        //        });
        //
        //        xAxis.setOnMouseMoved(new EventHandler<MouseEvent>()
        //        {
        //            @Override
        //            public void handle(MouseEvent mouseEvent)
        //            {
        //                tooltip.setText(String.format("x = %.2f", xAxis.getValueForDisplay(mouseEvent.getX())));
        //                tooltip.setX(mouseEvent.getScreenX() + 30);
        //                tooltip.setY(mouseEvent.getScreenY());
        //            }
        //        });
        //
        //        xAxis.setOnMouseExited(new EventHandler<MouseEvent>()
        //        {
        //            @Override
        //            public void handle(MouseEvent mouseEvent)
        //            {
        //                tooltip.hide();
        //            }
        //        });
        //
        //        yAxis.setOnMouseEntered(new EventHandler<MouseEvent>()
        //        {
        //            @Override
        //            public void handle(MouseEvent mouseEvent)
        //            {
        //                tooltip.show(lineChart, mouseEvent.getScreenX(), mouseEvent.getScreenY());
        //            }
        //        });
        //
        //        yAxis.setOnMouseMoved(new EventHandler<MouseEvent>()
        //        {
        //            @Override
        //            public void handle(MouseEvent mouseEvent)
        //            {
        //                tooltip.setText(String.format("y = %.2f", yAxis.getValueForDisplay(mouseEvent.getY())));
        //                tooltip.setX(mouseEvent.getScreenX() + 30);
        //                tooltip.setY(mouseEvent.getScreenY());
        //            }
        //        });
        //
        //        yAxis.setOnMouseExited(new EventHandler<MouseEvent>()
        //        {
        //            @Override
        //            public void handle(MouseEvent mouseEvent)
        //            {
        //                tooltip.hide();
        //            }
        //        });

        //        return cursorCoords;
    }
}
