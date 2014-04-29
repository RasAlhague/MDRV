package com.rasalhague.mdrv.gui;

import com.rasalhague.mdrv.DeviceConnectionListener;
import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.Utils;
import com.rasalhague.mdrv.analysis.AnalysisKey;
import com.rasalhague.mdrv.analysis.AnalysisPerformedListener;
import com.rasalhague.mdrv.analysis.PacketAnalysis;
import com.rasalhague.mdrv.configuration.ConfigurationLoader;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import com.rasalhague.mdrv.logging.PacketLogger;
import com.rasalhague.mdrv.logging.TextAreaHandler;
import com.rasalhague.mdrv.replay.Replay;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainWindowController extends Application implements AnalysisPerformedListener
{
    private static          MainWindowController      instance;
    public                  LineChart<Number, Number> lineChart;
    public                  TextArea                  debugTextArea;
    public                  CheckBox                  maxCheckBox;
    public                  Button                    refreshChartButton;
    public                  GridPane                  tooltipPane;
    public                  TitledPane                chartLegendPane;
    public                  VBox                      chartLegendVbox;
    public                  javafx.scene.shape.Line   horizontalLine;
    public                  Line                      verticalLine;
    public                  Button                    showDebugInfoBnt;
    public                  VBox                      controlBntsVBox;
    public                  Slider                    replaySlider;
    public                  CheckBox                  replayModeSwitcher;
    public                  Button                    openReplayBtn;
    public                  Button                    analyseCollectedData;
    public                  CheckBox                  modeCheckBox;
    public                  CheckBox                  medianCheckBox;
    public                  TextField                 chartUpdateDelayTextField;
    private                 int                       replaySliderPreviousValue;
    private static volatile Boolean                   chartCanUpdate;
    private static int                      chartUpdateDelayMs  = 1000;
    private static ScheduledExecutorService chartCanUpdateTimer = Executors.newSingleThreadScheduledExecutor();

    public MainWindowController()
    {
        instance = this;
    }

    public static void main(String[] args)
    {
        launch();
    }

    private static MainWindowController getInstance()
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

            PacketLogger.getInstance().closeWriter();

            //Need to stop coz thread prevent exit program
            if (DeviceConnectionListener.isListening())
            {
                DeviceConnectionListener.stopListening();
            }

            //TODO correct program closing
            System.exit(1);
            //            Platform.exit();
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
        replaySlider = (Slider) scene.lookup("#replaySlider");
        replayModeSwitcher = (CheckBox) scene.lookup("#replayModeSwitcher");
        openReplayBtn = (Button) scene.lookup("#openReplayBtn");
        modeCheckBox = (CheckBox) scene.lookup("#modeCheckBox");
        medianCheckBox = (CheckBox) scene.lookup("#medianCheckBox");
        chartUpdateDelayTextField = (TextField) scene.lookup("#chartUpdateDelayTextField");

        bindTooltipToLineChart(lineChart, tooltipPane);
        initChartLegend(chartLegendVbox, lineChart);
        initXYLines(horizontalLine, verticalLine, lineChart);
        initPopupMenu(controlBntsVBox, scene);
        initReplaySlider(replaySlider);
        initChartUpdateDelayTextField(chartUpdateDelayTextField);
        initChartBlockingTimer();

        //Add listeners and handlers
        ApplicationLogger.addCustomHandler(new TextAreaHandler(debugTextArea));
        PacketAnalysis.getInstance().addListener(getInstance());

        //fake button press
        DeviceConnectionListener.startListening();
    }

    /**
     * INIT SECTION
     */

    private void initChartBlockingTimer()
    {
        chartCanUpdateTimer.shutdown();
        chartCanUpdateTimer = Executors.newSingleThreadScheduledExecutor();

        chartCanUpdateTimer.scheduleAtFixedRate(() -> {

            chartCanUpdate = true;

        }, 0, chartUpdateDelayMs, TimeUnit.MILLISECONDS);
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
     * @param lineChart
     *
     * @throws InterruptedException
     */
    private void initChartLegend(VBox chartLegendVbox, LineChart<Number, Number> lineChart)
    {
        double selectedOpacity = 1.0;
        double unSelectedOpacity = 0.7;

        //this
        //                VBox vBox = (VBox) chartLegendPane.getContent();
        //or this

        boolean chartLegendPaneTrigger = false;
        if (chartLegendPane != null && chartLegendPaneTrigger)
        {
            /**
             * TitledPane Behavior OnMouseEntered
             */
            chartLegendPane.setOnMouseEntered(mouseEvent -> {

                chartLegendPane.setOpacity(selectedOpacity);
                chartLegendPane.setExpanded(true);
            });

            /**
             * TitledPane Behavior OnMouseExited
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
            chartLegendVbox.setOnMouseClicked(mouseEvent -> {

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
             * TitledPane Auto expand on vBox item added
             */
            chartLegendVbox.getChildren().addListener((Observable observable1) -> {

                //Set visible false when nothing to show
                if (chartLegendVbox.getChildren().size() == 0) { chartLegendPane.setVisible(false); }
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

        /**
         * Update legend list according to lineChart series
         */
        lineChart.getData().addListener((Observable observable) -> {

            /**
             * Delay needed for TODO Color wont work coz first series return wrong data
             */
            int updateDelayMs = 50;
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.schedule(() -> Platform.runLater(() -> {

                chartLegendVbox.getChildren().clear();
                lineChart.getData().forEach(series -> {

                    //Get numberSeries color and set it to text
                    String numberSeriesString = series.getNode().toString();
                    int indexOf = numberSeriesString.indexOf("stroke=");
                    String colorValue = numberSeriesString.substring(indexOf + 7, indexOf + 17);

                    HBox hBox = new HBox();
                    CheckBox checkBox = new CheckBox();
                    checkBox.setOnMouseClicked(event -> series.getNode().setVisible(checkBox.isSelected()));
                    hBox.getChildren().add(checkBox);

                    checkBox.setSelected(true);
                    checkBox.setText(series.getName());
                    checkBox.setTextFill(Paint.valueOf(colorValue));

                    chartLegendVbox.getChildren().add(hBox);
                });
            }), updateDelayMs, TimeUnit.MILLISECONDS);
        });

    }

    private void initReplaySlider(Slider replaySlider)
    {
        replaySlider.setOnMouseDragged((MouseEvent event) -> {

            replayModeSwitcher.setSelected(true);

            if (replaySlider.getValue() == replaySlider.getMax())
            {
                replayModeSwitcher.setSelected(false);
            }

            if (replayModeSwitcher.isSelected() && !chartInUse)
            {
                PacketAnalysis packetAnalysis = PacketAnalysis.getInstance();
                analysisPerformedEvent(packetAnalysis.getTimedAnalysisResults());
            }
        });

        replaySlider.setOnMouseClicked((MouseEvent event) -> {

            replayModeSwitcher.setSelected(true);

            if (replaySlider.getValue() == replaySlider.getMax())
            {
                replayModeSwitcher.setSelected(false);
            }

            if (replayModeSwitcher.isSelected() && !chartInUse)
            {
                PacketAnalysis packetAnalysis = PacketAnalysis.getInstance();
                analysisPerformedEvent(packetAnalysis.getTimedAnalysisResults());
            }
        });

        //        replaySlider.valueProperty().addListener(observable -> {
        //
        //            if (replayModeSwitcher.isSelected() && !chartInUse)
        //            {
        //                PacketAnalysis packetAnalysis = PacketAnalysis.getInstance();
        //                analysisPerformedEvent(packetAnalysis.getTimedAnalysisResultsClone());
        //            }
        //        });
    }

    private void initChartUpdateDelayTextField(TextField chartUpdateDelayTextField)
    {
        chartUpdateDelayTextField.textProperty().addListener(observable -> {

            try
            {
                int number = Integer.parseInt(chartUpdateDelayTextField.getText());
                if (number >= 300)
                {
                    chartUpdateDelayMs = number;
                    initChartBlockingTimer();
                }
                else
                {
                    ApplicationLogger.LOGGER.info("Delay must be a number that higher than 300 ms.");
                }
            }
            catch (NumberFormatException e)
            {
                ApplicationLogger.LOGGER.info("Delay must be a number that higher than 300 ms.");
            }

        });
    }

    /**
     * EVENTS SECTION
     */

    /**
     * Vertical line switch event.
     *
     * @param event
     *         the event
     */
    public void verticalLineSwitchEvent(ActionEvent event)
    {
        verticalLine.setVisible(!verticalLine.isVisible());

        CheckBox checkBox = (CheckBox) event.getSource();
        checkBox.setSelected(verticalLine.isVisible());
    }

    public void horizontalLineSwitchEvent(ActionEvent event)
    {
        horizontalLine.setVisible(!horizontalLine.isVisible());

        CheckBox checkBox = (CheckBox) event.getSource();
        checkBox.setSelected(horizontalLine.isVisible());
    }

    public void showDebugInfoBntClick()
    {
        if (debugTextArea.getHeight() != 0) { debugTextArea.setMaxHeight(0); }
        else { debugTextArea.setMaxHeight(Region.USE_COMPUTED_SIZE); }
    }

    public void refreshChartButtonClickEvent(Event event)
    {
        refreshChart();
    }

    public void openReplayBtnClick(ActionEvent actionEvent)
    {
        refreshChart();
        Replay.getInstance().loadReplay();
    }

    public void analyseCollectedDataBntAction(ActionEvent actionEvent)
    {
        PacketAnalysis.getInstance().analyseAndSaveCollectedData();
    }

    /**
     * HELPERS
     */
    void refreshChart()
    {
        replaySliderPreviousValue = 0;
        replaySlider.setValue(0);
        PacketAnalysis.getInstance().getTimedAnalysisResults().clear();
        lineChart.getData().clear();
    }

    /**
     * GUI UPDATE SECTION
     */

    boolean chartInUse = false;

    @Override
    public synchronized void analysisPerformedEvent(final LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>>> analysisResult)
    {
        if (chartCanUpdate)
        {
            chartInUse = true;
            chartCanUpdate = false;

            Platform.runLater(() -> {

                /**
                 * replaySlider behavior
                 */
                replaySlider.setMax(analysisResult.size() - 1);
                if (!replayModeSwitcher.isSelected())
                {
                    replaySlider.setValue(replaySlider.getMax());
                }

                updateChartSeries(analysisResult);

                //update replaySliderPreviousValue in the end
                replaySliderPreviousValue = (int) replaySlider.getValue();

                chartInUse = false;
            });
        }
    }

    private synchronized void updateChartSeries(final LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>>> analysisResult)
    {
        int replaySliderValue = (int) replaySlider.getValue();

        //when user click at replaySlider field we will get queryArray!
        ArrayList<Integer> queryArray = new ArrayList<>();
        boolean isToLeft = (replaySliderPreviousValue > replaySliderValue);
        //        System.out.println("replaySliderPreviousValue -- " + replaySliderPreviousValue);
        //        System.out.println("replaySliderValue -- " + replaySliderValue);
        if (isToLeft)
        {
            for (int i = replaySliderPreviousValue; i >= replaySliderValue; i--)
            {
                queryArray.add(i);
            }
        }
        else
        {
            for (int i = replayModeSwitcher.isSelected() ? replaySliderPreviousValue : replaySliderPreviousValue +
                    1; i <= replaySliderValue; i++)
            {
                queryArray.add(i);
            }
        }
        //counter out of range exc
        if (analysisResult.size() < queryArray.size())
        {
            queryArray.clear();
        }
        //        System.out.println("queryArray.size() -- " + queryArray.size());
        //        System.out.println("queryArray.size() -- " + queryArray);

        /**
         * Speed up algorithm!
         * Create combined HashMap with final values for every device
         */
        HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Integer>>> combinedAnalysisResult = new HashMap<>();
        ArrayList<Long> timeKeys = new ArrayList<>(analysisResult.keySet());
        for (Integer que : queryArray)
        {
            Long timeKey = timeKeys.get(que);

            Set<DeviceInfo> deviceInfoKeys = analysisResult.get(timeKey).keySet();
            for (DeviceInfo deviceInfo : deviceInfoKeys)
            {
                if (combinedAnalysisResult.containsKey(deviceInfo))
                {
                    //TODO AnalysisKey.NEW_SERIES in combinedAnalysisResult
                    //                    boolean isNewSeries = combinedAnalysisResult.get(deviceInfo).containsKey(AnalysisKey.NEW_SERIES);
                    //                    if (isNewSeries) System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                    combinedAnalysisResult.remove(deviceInfo);
                    combinedAnalysisResult.put(deviceInfo, analysisResult.get(timeKey).get(deviceInfo));

                    //                    if (isNewSeries) combinedAnalysisResult.get(deviceInfo).put(AnalysisKey.NEW_SERIES, null);
                }
                else
                {
                    combinedAnalysisResult.put(deviceInfo, analysisResult.get(timeKey).get(deviceInfo));
                }
            }
        }
        //        System.out.println("combinedAnalysisResult -- " + combinedAnalysisResult);

        /**
         * For every device that processed by Analysis class update his line on chart
         */
        Set<DeviceInfo> deviceInfoKeys = combinedAnalysisResult.keySet();
        for (DeviceInfo deviceInfo : deviceInfoKeys)
        {
            HashMap<AnalysisKey, ArrayList<Integer>> analysisForDevice = combinedAnalysisResult.get(deviceInfo);

            Set<AnalysisKey> analysisForDeviceKeys = analysisForDevice.keySet();
            for (AnalysisKey analysisForDeviceKey : analysisForDeviceKeys)
            {
                /**
                 * Generate XYChart.Series
                 */
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                String seriesName = deviceInfo.getName() + analysisForDeviceKey.toString();
                series.setName(seriesName);

                if ((analysisForDeviceKey == AnalysisKey.MAX && maxCheckBox.isSelected()) ||
                        (analysisForDeviceKey == AnalysisKey.MODE && modeCheckBox.isSelected()) ||
                        (analysisForDeviceKey == AnalysisKey.MEDIAN && medianCheckBox.isSelected()))
                {
                    ArrayList<Integer> listMax = new ArrayList<>(analysisForDevice.get(analysisForDeviceKey));

                    //get seriesData from XYChart.Series to work with
                    ObservableList<XYChart.Data<Number, Number>> seriesData = series.getData();

                    final float initialFrequency = deviceInfo.getInitialFrequency();
                    final float channelSpacingKHz = deviceInfo.getChannelSpacing() / 1000;

                    //set every point to the seriesData
                    float xAxisCounter = initialFrequency;
                    for (Integer value : listMax)
                    {
                        XYChart.Data<Number, Number> data = new XYChart.Data<>(xAxisCounter, value);
                        seriesData.add(data);

                        xAxisCounter += channelSpacingKHz;
                    }

                    /**
                     * Use XYChart.Series
                     * Update series
                     */
                    ObservableList<XYChart.Series<Number, Number>> lineChartData = lineChart.getData();

                    if (Utils.isSeriesExist(lineChartData, seriesName))
                    {
                        for (XYChart.Series<Number, Number> numberSeries : lineChartData)
                        {
                            if (numberSeries.getName().equals(seriesName))
                            {
                                ObservableList<XYChart.Data<Number, Number>> data = numberSeries.getData();
                                for (int i = 0; i < data.size(); i++)
                                {
                                    XYChart.Data<Number, Number> numberData = data.get(i);
                                    numberData.setYValue(series.getData().get(i).getYValue());
                                }

                                //set opacity to non created series
                                if (analysisForDevice.containsKey(AnalysisKey.NEW_SERIES) &&
                                        replaySliderValue != replaySliderPreviousValue)
                                {
                                    double opacity = 0.3;
                                    Node numberSeriesNode = numberSeries.getNode();
                                    if (replaySliderValue > replaySliderPreviousValue)
                                    {
                                        numberSeriesNode.setOpacity(0.9);
                                    }
                                    else
                                    {
                                        numberSeriesNode.setOpacity(opacity);
                                    }
                                }
                            }
                        }
                    }
                    //or create if does not exist
                    else
                    {
                        //disable stupid series creating animation
                        lineChart.setAnimated(false);
                        lineChartData.add(series);
                        lineChart.setAnimated(true);
                    }
                }
            }
        }
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

            HashMap<ArrayList<Text>, Boolean> rowsToView = new HashMap<>();

            /**
             * Mouse coordinates
             */
            double xAxisValueForDisplay = (double) xAxis.getValueForDisplay(mouseEvent.getX());
            //            Double yAxisValueForDisplay = (Double) yAxis.getValueForDisplay(mouseEvent.getY());

            ObservableList<XYChart.Series<Number, Number>> lineChartData = lineChart.getData();
            for (XYChart.Series<Number, Number> numberSeries : lineChartData)
            {
                ObservableList<XYChart.Data<Number, Number>> data = numberSeries.getData();
                for (XYChart.Data<Number, Number> numberData : data)
                {
                    float xValue = (float) numberData.getXValue();
                    Number yValue = numberData.getYValue();
                    float range = 0.25f;
                    ArrayList<Text> rowTexts = new ArrayList<>();

                    if (xValue > xAxisValueForDisplay - range && xValue < xAxisValueForDisplay + range)
                    {
                        /**
                         * Create Text object, configure and add it to row array
                         */
                        Text dBm = new Text(String.valueOf(yValue));
                        Text dBmText = new Text(" dBm");
                        Text hz = new Text('\t'/* + "2 4"*/ + String.valueOf(xValue));
                        Text hzText = new Text(" kHz");

                        rowTexts.add(dBm);
                        rowTexts.add(dBmText);
                        rowTexts.add(hz);
                        rowTexts.add(hzText);
                        if (numberSeries.getNode().isVisible())
                        {
                            rowsToView.put(rowTexts, numberSeries.getNode().isVisible());
                        }

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
                        if (numberSeries.getNode().isVisible())
                        {
                            numberData.getNode().setVisible(true);
                            numberData.getNode().setEffect(new DropShadow());
                        }
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
            //if (rowsToView.size() > 0) is needed for tooltipPane always visibility
            if (rowsToView.size() > 0)
            {
                tooltipPane.getChildren().clear();
                ArrayList<ArrayList<Text>> rowsToViewKeySet = new ArrayList<>(rowsToView.keySet());
                //sorting in 1 line xD
                rowsToViewKeySet.sort((o1, o2) -> Integer.compare(Integer.parseInt(o1.get(0).getText()),
                                                                  Integer.parseInt(o2.get(0).getText())));

                for (int row = 0; row < rowsToViewKeySet.size(); row++)
                {
                    ArrayList<Text> texts = rowsToViewKeySet.get(row);
                    if (rowsToView.get(texts))
                    {
                        for (int column = 0; column < texts.size(); column++)
                        {
                            tooltipPane.add(texts.get(column), column, row);

                            //set up alignment for first column
                            if (column == 0)
                            {
                                tooltipPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
                            }
                        }
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
