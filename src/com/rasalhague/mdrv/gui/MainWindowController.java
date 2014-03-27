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
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import javafx.scene.control.TitledPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainWindowController extends Application implements AnalysisPerformedListener
{
    private static MainWindowController      instance;
    public         LineChart<Number, Number> lineChart;
    public         TextArea                  debugTextArea;
    public         CheckBox                  maxCheckBox;
    public         Button                    refreshChartButton;
    public         GridPane                  tooltipPane;
    public         TitledPane                chartLegendPane;
    public VBox                    chartLegendVbox;
    public javafx.scene.shape.Line horizontalLine;
    public Line                    verticalLine;
    public Button                  showDebugInfoBnt;
    public VBox                    controlBntsVBox;

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

        final String chartStyleCssPath = "/ChartStyle.css";
        final String rootPath = "/com/rasalhague/mdrv/gui/view/MainWindow.fxml";
        //        final String popupMenuPath = "/com/rasalhague/mdrv/gui/view/PopupMenu.fxml";

        Parent root = FXMLLoader.load(getClass().getResource(rootPath));
        //        Parent popupMenu = FXMLLoader.load(getClass().getResource(popupMenuPath));
        final Scene scene = new Scene(root);
        scene.getStylesheets().add(chartStyleCssPath);

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
        tooltipPane = (GridPane) scene.lookup("#tooltipPane");
        chartLegendPane = (TitledPane) scene.lookup("#chartLegendPane");
        chartLegendVbox = (VBox) scene.lookup("#chartLegendVbox");
        horizontalLine = (javafx.scene.shape.Line) scene.lookup("#horizontalLine");
        verticalLine = (javafx.scene.shape.Line) scene.lookup("#verticalLine");
        controlBntsVBox = (VBox) scene.lookup("#controlBntsVBox");

        //init tooltip
        bindTooltipToLineChart(lineChart, tooltipPane);
        //init chartLegendPane
        initChartLegendPane(chartLegendPane, lineChart);
        //init horizontalLine
        initXYLines(horizontalLine, verticalLine, lineChart);
        //init popup
        initPopupMenu(controlBntsVBox, scene);

        //Add listeners and handlers
        ApplicationLogger.addCustomHandler(new TextAreaHandler(debugTextArea));
        PacketAnalysis.getInstance().addListener(getInstance());

        //fake button press
        DeviceConnectionListener.startListening();
    }

    private void initPopupMenu(VBox controlBntsVBox, Scene scene)
    {
        double width = controlBntsVBox.getWidth();
        double height = controlBntsVBox.getHeight();

        scene.setOnMouseMoved((MouseEvent mouseEvent) -> {

            if (mouseEvent.getSceneX() < width && mouseEvent.getSceneY() < height)
            {
                controlBntsVBox.setVisible(true);
            }
            else
            {
                controlBntsVBox.setVisible(false);
            }
        });
    }

    private void initXYLines(Line horizontalLine, Line verticalLine, LineChart<Number, Number> lineChart)
    {
        //        Rectangle clip = new Rectangle(lineChart.getWidth(), lineChart.getHeight());
        //        clip.setLayoutX(lineChart.getLayoutX());
        //        clip.setLayoutY(lineChart.getLayoutY());
        //        Rectangle clip2 = new Rectangle(lineChart.getWidth(), lineChart.getHeight());
        //        clip.setLayoutX(lineChart.getLayoutX());
        //        clip.setLayoutY(lineChart.getLayoutY());

        lineChart.setOnMouseMoved((MouseEvent mouseEvent) -> {

            horizontalLine.setLayoutX(lineChart.getLayoutX() + lineChart.getYAxis().getWidth());
            horizontalLine.setEndX(lineChart.getWidth() - (lineChart.getLayoutX() + lineChart.getYAxis().getWidth()));

            verticalLine.setLayoutY(lineChart.getLayoutY());
            verticalLine.setEndY(lineChart.getHeight() - lineChart.getXAxis().getHeight());

            horizontalLine.setLayoutY(mouseEvent.getSceneY());
            verticalLine.setLayoutX(mouseEvent.getSceneX());
        });
    }

    /**
     * ChartLegendPane behavior realization
     *
     * @param chartLegendPane
     * @param lineChart
     *
     * @throws InterruptedException
     */
    private void initChartLegendPane(TitledPane chartLegendPane, LineChart<Number, Number> lineChart)
    {
        double selectedOpacity = 1.0;
        double unSelectedOpacity = 0.7;

        //this
        VBox vBox = (VBox) chartLegendPane.getContent();

        //or this
        //        VBox vBox = chartLegendVbox;

        /**
         * Behavior OnMouseEntered
         */
        chartLegendPane.setOnMouseEntered(mouseEvent -> {

            chartLegendPane.setOpacity(selectedOpacity);
            chartLegendPane.setExpanded(true);
        });

        /**
         * Behavior OnMouseExited
         */
        chartLegendPane.setOnMouseExited(mouseEvent -> {

            if (chartLegendPane.isCollapsible())
            {
                chartLegendPane.setOpacity(unSelectedOpacity);
            }
            chartLegendPane.setExpanded(false);
        });

        /**
         * Set Collapsible realization
         * Click on vBox change chartLegendPane Collapsible state
         */
        vBox.setOnMouseClicked(mouseEvent -> {

            if (chartLegendPane.isCollapsible())
            {
                chartLegendPane.setOpacity(selectedOpacity);
                chartLegendPane.setCollapsible(false);
            }
            else
            {
                chartLegendPane.setOpacity(unSelectedOpacity);
                chartLegendPane.setCollapsible(true);
            }
        });

        /**
         * Update legend list according to lineChart series
         */
        lineChart.getData().addListener((Observable observable) -> {

            /**
             * Delay needed for TOD_O Color wont work coz first series return wrong data
             */
            int updateDelayMs = 50;
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.schedule(() -> Platform.runLater(() -> {

                vBox.getChildren().clear();
                lineChart.getData().forEach(series -> {

                    //Get numberSeries color and set it to text
                    String numberSeriesString = series.getNode().toString();
                    int indexOf = numberSeriesString.indexOf("stroke=");
                    String substring = numberSeriesString.substring(indexOf + 7, indexOf + 17);

                    Text seriesText = new Text(series.getName());
                    seriesText.setFill(Paint.valueOf(substring));
                    vBox.getChildren().add(seriesText);
                });
            }), updateDelayMs, TimeUnit.MILLISECONDS);
        });

        /**
         * Auto expand on vBox item added
         */
        vBox.getChildren().addListener((Observable observable1) -> {

            //Set visible false when nothing to show
            if (vBox.getChildren().size() == 0) { chartLegendPane.setVisible(false); }
            else { chartLegendPane.setVisible(true); }

            //Expand on item add/remove and shrink until shrinkDelayMs
            final int shrinkDelayMs = 2000;
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.schedule(() -> Platform.runLater(() -> {

                chartLegendPane.setOpacity(selectedOpacity);
                chartLegendPane.setExpanded(true);
            }), 0, TimeUnit.SECONDS);

            scheduledExecutorService.schedule(() -> Platform.runLater(() -> {

                if (chartLegendPane.isCollapsible())
                {
                    chartLegendPane.setOpacity(unSelectedOpacity);
                }
                chartLegendPane.setExpanded(false);
            }), shrinkDelayMs, TimeUnit.MILLISECONDS);
        });
    }

    public void maxCheckBoxChangedEvent(ActionEvent event)
    {
        System.out.println(maxCheckBox.isSelected());
    }

    public void showDebugInfoBntClick()
    {
        if (debugTextArea.getHeight() != 0) { debugTextArea.setMaxHeight(0); }
        else { debugTextArea.setMaxHeight(Region.USE_COMPUTED_SIZE); }
    }

    public void refreshChartButtonClickEvent(Event event)
    {
        PacketAnalysis.getInstance().getAnalysisResultsMap().clear();
        lineChart.getData().clear();
    }

    @Override
    public synchronized void analysisPerformedEvent(final HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> analysisResult)
    {
        Platform.runLater(() -> {

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

                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    ObservableList<XYChart.Data<Number, Number>> seriesData = series.getData();
                    series.setName(deviceInfo.getName());

                    final double spacing = (85.0 / points)/*(double)Math.round((85.0 / points) * 1000) / 1000*/;
                    //                    final double spacing = (95.0 / points)/*(double)Math.round((85.0 / points) * 1000) / 1000*/;
                    double xAxisCounter = 0.0;
                    for (Integer value : listMax)
                    {
                        XYChart.Data<Number, Number> data = new XYChart.Data<>((double) Math.round((xAxisCounter) *
                                                                                                           1000) / 1000,
                                                                               value
                        );
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
                        lineChart.setAnimated(false);
                        lineChartData.add(series);
                        lineChart.setAnimated(true);
                    }
                }
            }
        });
    }

    private void bindTooltipToLineChart(final LineChart<Number, Number> lineChart, GridPane tooltipPane)
    {
        Font tooltipFont = Font.font(Font.getDefault().getName(),
                                     FontWeight.BOLD,
                                     FontPosture.REGULAR,
                                     Font.getDefault().getSize());

        final Axis<Number> xAxis = lineChart.getXAxis();
        final Axis<Number> yAxis = lineChart.getYAxis();

        final Node chartBackground = lineChart.lookup(".chart-plot-background");
        for (Node n : chartBackground.getParent().getChildrenUnmodifiable())
        {
            if (n != chartBackground && n != xAxis && n != yAxis)
            {
                n.setMouseTransparent(true);
            }
        }

        chartBackground.setOnMouseEntered(mouseEvent -> tooltipPane.setVisible(true));

        chartBackground.setOnMouseMoved((MouseEvent mouseEvent) -> {

            ArrayList<ArrayList<Text>> rowsToView = new ArrayList<>();

            /**
             * Mouse coordinates
             */
            Double xAxisValueForDisplay = (Double) xAxis.getValueForDisplay(mouseEvent.getX());
            //            Double yAxisValueForDisplay = (Double) yAxis.getValueForDisplay(mouseEvent.getY());

            ObservableList<XYChart.Series<Number, Number>> lineChartData = lineChart.getData();
            for (XYChart.Series<Number, Number> numberSeries : lineChartData)
            {
                ObservableList<XYChart.Data<Number, Number>> data = numberSeries.getData();
                for (XYChart.Data<Number, Number> numberData : data)
                {
                    Double xValue = (Double) numberData.getXValue();
                    Number yValue = numberData.getYValue();
                    double range = 0.25;
                    ArrayList<Text> rowTexts = new ArrayList<>();

                    if (xValue > xAxisValueForDisplay - range && xValue < xAxisValueForDisplay + range)
                    {
                        /**
                         * Create Text object, configure and add it to row array
                         */
                        Text dBm = new Text(String.valueOf(yValue));
                        Text dBmText = new Text(" dBm");
                        Text hz = new Text('\t' + "2 4" + String.valueOf(xValue));
                        Text hzText = new Text(" Hz");

                        rowTexts.add(dBm);
                        rowTexts.add(dBmText);
                        rowTexts.add(hz);
                        rowTexts.add(hzText);
                        rowsToView.add(rowTexts);

                        //Get numberSeries color and set it to text
                        String numberSeriesString = numberSeries.getNode().toString();
                        int indexOf = numberSeriesString.indexOf("stroke=");
                        String substring = numberSeriesString.substring(indexOf + 7, indexOf + 17);

                        for (Text text : rowTexts)
                        {
                            text.setFill(Paint.valueOf(substring));
                            text.setFont(tooltipFont);
                        }

                        /**
                         * Highlight selected node
                         */
                        numberData.getNode().setVisible(true);
                        numberData.getNode().setEffect(new DropShadow());
                    }
                    else
                    {
                        /**
                         * Highlight down non selected node
                         */
                        numberData.getNode().setVisible(false);
                        numberData.getNode().setEffect(null);
                    }
                }
            }

            /**
             * Add rowsToView with text objects to the tooltipPane
             */
            //            tooltipPane.getRowConstraints().clear();
            //            tooltipPane.getColumnConstraints().clear();
            if (rowsToView.size() > 0)
            {
                tooltipPane.getChildren().clear();
                for (int row = 0; row < rowsToView.size(); row++)
                {
                    ArrayList<Text> texts = rowsToView.get(row);
                    for (int column = 0; column < texts.size(); column++)
                    {
                        tooltipPane.add(texts.get(column), column, row);
                    }
                }
            }

            /**
             * Update tooltipPane position with Position Correction
             */
            int tooltipXPosCorrection = 30;
            int tooltipYPosCorrection = -30;
            double activeZoneXStart = lineChart.getWidth() - tooltipPane.getWidth();
            double activeZoneYStart = tooltipPane.getHeight();
            if (mouseEvent.getSceneX() <= (activeZoneXStart - tooltipXPosCorrection))
            {
                tooltipPane.setLayoutX(mouseEvent.getSceneX() + tooltipXPosCorrection);
            }

            if (mouseEvent.getSceneY() >= (activeZoneYStart - tooltipYPosCorrection))
            {
                tooltipPane.setLayoutY(mouseEvent.getSceneY() + tooltipYPosCorrection);
            }
        });

        chartBackground.setOnMouseExited(mouseEvent -> tooltipPane.setVisible(false));

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
