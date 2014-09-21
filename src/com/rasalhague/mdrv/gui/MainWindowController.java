package com.rasalhague.mdrv.gui;

import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.analysis.AnalysisKey;
import com.rasalhague.mdrv.analysis.AnalysisPerformedListener;
import com.rasalhague.mdrv.analysis.PacketAnalysis;
import com.rasalhague.mdrv.configuration.ConfigurationLoader;
import com.rasalhague.mdrv.connectionlistener.DeviceConnectionListener;
import com.rasalhague.mdrv.device.core.Device;
import com.rasalhague.mdrv.device.core.DeviceInfo;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import com.rasalhague.mdrv.logging.PacketLogger;
import com.rasalhague.mdrv.logging.TextAreaHandler;
import com.rasalhague.mdrv.replay.Replay;
import com.rasalhague.mdrv.wirelessadapter.WirelessAdapterCommunication;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * App entry point. Contains GUI control code..
 */
public class MainWindowController extends Application implements AnalysisPerformedListener
{
    private static          MainWindowController      instance;
    /**
     * The Line chart.
     */
    public                  LineChart<Number, Number> lineChart;
    /**
     * The Debug text area.
     */
    public                  TextArea                  debugTextArea;
    /**
     * The Refresh chart button.
     */
    public                  Button                    refreshChartButton;
    /**
     * The Tooltip pane.
     */
    public                  GridPane                  tooltipPane;
    /**
     * The Chart legend vbox.
     */
    public                  VBox                      chartLegendVbox;
    /**
     * The Horizontal line.
     */
    public                  javafx.scene.shape.Line   horizontalLine;
    /**
     * The Vertical line.
     */
    public                  Line                      verticalLine;
    /**
     * The Show debug info bnt.
     */
    public                  Button                    showDebugInfoBnt;
    /**
     * The Control bnts v box.
     */
    public                  VBox                      controlBntsVBox;
    /**
     * The Replay slider.
     */
    public                  Slider                    replaySlider;
    /**
     * The Replay mode switcher.
     */
    public                  CheckBox                  replayModeSwitcher;
    /**
     * The Open replay btn.
     */
    public                  Button                    openReplayBtn;
    /**
     * The Chart update delay text field.
     */
    public                  TextField                 chartUpdateDelayTextField;
    /**
     * The Setting button.
     */
    public                  Button                    settingButton;
    /**
     * The Spectral masks grid pane.
     */
    public                  GridPane                  spectralMasksGridPane;
    public                  Button                    addDummyButton;
    public                  CheckBox                  enableAnimationChBx;
    private                 int                       replaySliderPreviousValue;
    private static volatile Boolean                   chartCanUpdate;
    private static int                      chartUpdateDelayMs  = 1000;
    private static ScheduledExecutorService chartCanUpdateTimer = Executors.newSingleThreadScheduledExecutor();
    private static boolean                  showDebugInfo       = true;

    /**
     * Instantiates a new Main window controller.
     */
    public MainWindowController()
    {
        instance = this;
    }

    /**
     * The entry point of application.
     *
     * @param args
     *         the input arguments
     */
    public static void main(String[] args)
    {
        launch();
    }

    public static MainWindowController getInstance()
    {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        ApplicationLogger.setup();
        //        com.rasalhague.mdrv.logging.LogOutputStream.setup();
        ConfigurationLoader.initialize();

        final String chartStyleCssPath = "/ChartStyle.css";
        final String rootPath = "/com/rasalhague/mdrv/gui/view/MainWindow.fxml";
        Parent root = FXMLLoader.load(getClass().getResource(rootPath));
        final Scene scene = new Scene(root);
        scene.getStylesheets().add(chartStyleCssPath);

        /**
         * Stage behavior
         */
        primaryStage.setOnCloseRequest(windowEvent -> {
            //Correctly close file handlers
            ApplicationLogger.closeHandlers();

            PacketLogger.getInstance().closeWriter();

            DeviceConnectionListener deviceConnectionListener = DeviceConnectionListener.getInstance();
            //Need to stop coz thread prevent exit program
            if (deviceConnectionListener.isListening())
            {
                deviceConnectionListener.stopListening();
            }

            //TODO correct program closing
            System.exit(1);
            //            Platform.exit();
        });

        //        primaryStage.setMaxWidth(1000);
        //        primaryStage.setMinWidth(500);
        //        primaryStage.setMinHeight(500);
        //        primaryStage.setResizable(false);

        primaryStage.setScene(scene);
        primaryStage.show();

        //init GUI objects
        lineChart = (LineChart<Number, Number>) scene.lookup("#lineChart");
        debugTextArea = (TextArea) scene.lookup("#debugTextArea");
        tooltipPane = (GridPane) scene.lookup("#tooltipPane");
        chartLegendVbox = (VBox) scene.lookup("#chartLegendVbox");
        controlBntsVBox = (VBox) scene.lookup("#controlBntsVBox");
        replaySlider = (Slider) scene.lookup("#replaySlider");
        replayModeSwitcher = (CheckBox) scene.lookup("#replayModeSwitcher");
        openReplayBtn = (Button) scene.lookup("#openReplayBtn");
        chartUpdateDelayTextField = (TextField) scene.lookup("#chartUpdateDelayTextField");
        settingButton = (Button) scene.lookup("#settingButton");
        spectralMasksGridPane = (GridPane) scene.lookup("#spectralMasksGridPane");
        addDummyButton = (Button) scene.lookup("#addDummyButton");

        //initialization
        WirelessAdapterDataVisualizer.getInstance().init(spectralMasksGridPane);
        WirelessAdapterDataVisualizer.getInstance().setUpSettings(controlBntsVBox);
        SettingMenu.getInstance().initSettingMenu(settingButton, controlBntsVBox);
        bindTooltipToLineChart(lineChart, tooltipPane);
        ChartLegend.getInstance().initChartLegend(chartLegendVbox, lineChart);
        initXYLines(lineChart);
        initReplaySlider(replaySlider);
        initChartUpdateDelayTextField(chartUpdateDelayTextField);
        initChartBlockingTimer();

        //Add listeners and handlers
        ApplicationLogger.addCustomHandler(new TextAreaHandler(debugTextArea));
        PacketAnalysis.getInstance().addListener(getInstance());
        PacketAnalysis.getInstance().addListener(SettingMenu.getInstance());

        //Connect WirelessAdapter
        WirelessAdapterCommunication wirelessAdapterCommunication = WirelessAdapterCommunication.getInstance();
        wirelessAdapterCommunication.addListener(WirelessAdapterDataVisualizer.getInstance());
        Thread wirelessAdapterCommunicationThread = new Thread(wirelessAdapterCommunication);
        wirelessAdapterCommunicationThread.start();

        //fake button press
        DeviceConnectionListener.getInstance().startListening();
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

    private void initXYLines(LineChart<Number, Number> lineChart)
    {
        new ChartValueMarker(lineChart);
    }

    private void initReplaySlider(Slider replaySlider)
    {
        replaySlider.setOnMouseDragged((MouseEvent event) -> {

            replayModeSwitcher.setSelected(true);

            if (replaySlider.getValue() == replaySlider.getMax())
            {
                replayModeSwitcher.setSelected(false);
            }

            if (replayModeSwitcher.isSelected()/* && !chartInUse*/)
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

            if (replayModeSwitcher.isSelected() /*&& !chartInUse*/)
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
                if (number >= 100)
                {
                    chartUpdateDelayMs = number;
                    initChartBlockingTimer();
                }
                else
                {
                    chartUpdateDelayTextField.setText("100");
                    ApplicationLogger.LOGGER.info("Delay must be a number that higher than 100 ms.");
                }
            }
            catch (NumberFormatException e)
            {
                ApplicationLogger.LOGGER.info("Delay must be a number that higher than 100 ms.");
            }

        });
    }

    /**
     * EVENTS SECTION
     */

    public void animationChBxOnAction(ActionEvent actionEvent)
    {
        lineChart.setAnimated(enableAnimationChBx.isSelected());
    }

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

    /**
     * Horizontal line switch event.
     *
     * @param event
     *         the event
     */
    public void horizontalLineSwitchEvent(ActionEvent event)
    {
        horizontalLine.setVisible(!horizontalLine.isVisible());

        CheckBox checkBox = (CheckBox) event.getSource();
        checkBox.setSelected(horizontalLine.isVisible());
    }

    /**
     * Show debug info.
     */
    public void showDebugInfoBntClick()
    {
        debugTextArea.setMaxHeight(showDebugInfo ? Region.USE_COMPUTED_SIZE : 0);
        spectralMasksGridPane.setGridLinesVisible(showDebugInfo);
        debugTextArea.setVisible(showDebugInfo);

        showDebugInfo = !showDebugInfo;
    }

    /**
     * Refresh chart button click event.
     *
     * @param event
     *         the event
     */
    public void refreshChartButtonClickEvent(Event event)
    {
        refreshChart();
    }

    /**
     * Open replay btn click.
     *
     * @param actionEvent
     *         the action event
     */
    public void openReplayBtnClick(ActionEvent actionEvent)
    {
        refreshChart();
        Replay.getInstance().loadReplay();
    }

    public void addDummyButtonOnAction(ActionEvent actionEvent)
    {
        DeviceConnectionListener.getInstance().addDummyDevice();
    }

    /**
     * HELPERS
     */
    void refreshChart()
    {
        replaySliderPreviousValue = 0;
        replaySlider.setValue(0);
        PacketAnalysis.getInstance().getTimedAnalysisResults().clear();
        ChartLegend.getInstance().clearChartLegend();
        lineChart.getData().clear();
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
            float scanRange = 0.25f;

            lineChart.getData().forEach(numberSeries -> {

                numberSeries.getData().forEach(numberData -> {

                    float xValue = (float) numberData.getXValue();
                    Number yValue = numberData.getYValue();
                    ArrayList<Text> rowTexts = new ArrayList<>();

                    if (xValue > xAxisValueForDisplay - scanRange && xValue < xAxisValueForDisplay + scanRange)
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
                        Node numberSeriesNode = numberSeries.getNode();
                        if (numberSeriesNode.isVisible())
                        {
                            rowsToView.put(rowTexts, numberSeriesNode.isVisible());
                        }

                        //TODO MEMORY LEAK
                        //Get numberSeries color and set it to text
                        String numberSeriesString = numberSeriesNode.toString();
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
                        if (lineChart.getCreateSymbols() && numberSeriesNode.isVisible())
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
                        if (lineChart.getCreateSymbols())
                        {
                            numberData.getNode().setVisible(false);
                            numberData.getNode().setEffect(null);
                        }
                    }

                });
            });

            /**
             * Add rowsToView with text objects to the tooltipPane
             */
            //if (rowsToView.size() > 0) is needed for tooltipPane always visibility
            if (rowsToView.size() > 0)
            {
                tooltipPane.getChildren().clear();
                ArrayList<ArrayList<Text>> rowsToViewKeySet = new ArrayList<>(rowsToView.keySet());
                //sorting in 1 line xD
                rowsToViewKeySet.sort((o1, o2) -> Float.compare(Float.parseFloat(o1.get(0).getText()),
                                                                Float.parseFloat(o2.get(0).getText())));

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

    /**
     * GUI UPDATE SECTION
     */

    @Override
    public synchronized void analysisPerformedEvent(final LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>>> analysisResult)
    {
        updateChartIfCan(analysisResult);
    }

    /**
     * Update chart if can.
     *
     * @param analysisResult
     *         the analysis result
     */
    synchronized void updateChartIfCan(final LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>>> analysisResult)
    {
        if (chartCanUpdate)
        {
            updateChart(analysisResult);

            //turn true in the timer
            chartCanUpdate = false;
        }
    }

    public synchronized void updateDeviceRSSI(DeviceInfo deviceInfo, Integer newValueF, Integer oldValueF)
    {
        ObservableList<XYChart.Series<Number, Number>> lineChartData = lineChart.getData();
        for (XYChart.Series<Number, Number> series : lineChartData)
        {
            String regex = "(" +
                    deviceInfo.getFriendlyNameWithId() +
                    ").*(" +
                    deviceInfo.getPortName() +
                    ").*";

            regex = regex.replace("/", "\\/");

            if (series.getName().matches(regex))
            {
                ObservableList<XYChart.Data<Number, Number>> seriesDatas = series.getData();
                for (XYChart.Data<Number, Number> seriesData : seriesDatas)
                {
                    seriesData.setYValue(seriesData.getYValue().floatValue() - oldValueF + newValueF);
                }
            }
        }
    }

    /**
     * Force update chart.
     */
    public synchronized static void forceUpdateChart()
    {
        getInstance().updateChart(PacketAnalysis.getInstance().getTimedAnalysisResults());
    }

    private void updateChart(final LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>>> analysisResult)
    {
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
        });
    }

    private synchronized void updateChartSeries(final LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>>> analysisResult)
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
        HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>> combinedAnalysisResult = new HashMap<>();
        ArrayList<Long> timeKeys = new ArrayList<>(analysisResult.keySet());
        queryArray.forEach(que -> {

            Long timeKey = timeKeys.get(que);

            Set<DeviceInfo> deviceInfoKeys = analysisResult.get(timeKey).keySet();
            for (DeviceInfo deviceInfo : deviceInfoKeys)
            {
                HashMap<AnalysisKey, ArrayList<Byte>> myHM = new HashMap<>();
                Set<AnalysisKey> analysisKeys = analysisResult.get(timeKey).get(deviceInfo).keySet();
                //sort for fix chart legend unsorted out bug
                List<AnalysisKey> sortedAnalysisKeys = Utils.asSortedList(analysisKeys);

                if (combinedAnalysisResult.containsKey(deviceInfo))
                {
                    for (AnalysisKey analysisKey : sortedAnalysisKeys)
                    {
                        combinedAnalysisResult.get(deviceInfo).remove(analysisKey);
                        combinedAnalysisResult.get(deviceInfo)
                                              .put(analysisKey,
                                                   analysisResult.get(timeKey).get(deviceInfo).get(analysisKey));
                    }

                    //                    analysisKeys.forEach(analysisKey -> {
                    //
                    //                        combinedAnalysisResult.get(deviceInfo).remove(analysisKey);
                    //                        combinedAnalysisResult.get(deviceInfo)
                    //                                              .put(analysisKey,
                    //                                                   analysisResult.get(timeKey).get(deviceInfo).get(analysisKey));
                    //                    });
                }
                else
                {
                    sortedAnalysisKeys.forEach(analysisKey -> {

                        myHM.put(analysisKey, analysisResult.get(timeKey).get(deviceInfo).get(analysisKey));
                    });
                    combinedAnalysisResult.put(deviceInfo, myHM);
                }
            }
        });

        /**
         * For every device that processed by Analysis class update his line on chart
         */
        Set<DeviceInfo> deviceInfoKeys = combinedAnalysisResult.keySet();
        for (DeviceInfo deviceInfo : deviceInfoKeys)
        {
            HashMap<AnalysisKey, ArrayList<Byte>> analysisForDevice = combinedAnalysisResult.get(deviceInfo);

            Set<AnalysisKey> analysisForDeviceKeys = analysisForDevice.keySet();
            for (AnalysisKey analysisForDeviceKey : analysisForDeviceKeys)
            {
                if ((analysisForDeviceKey == AnalysisKey.MAX) ||
                        (analysisForDeviceKey == AnalysisKey.MODE) ||
                        (analysisForDeviceKey == AnalysisKey.MEDIAN) ||
                        (analysisForDeviceKey == AnalysisKey.CURRENT) ||
                        (analysisForDeviceKey == AnalysisKey.AVR))
                {

                    /**
                     * Generate XYChart.Series
                     */
                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    String seriesName = deviceInfo.getFriendlyNameWithId() +
                            " on " +
                            deviceInfo.getPortName() +
                            " " +
                            analysisForDeviceKey.toString().toLowerCase();
                    series.setName(seriesName);

                    ArrayList<Byte> listMax = new ArrayList<>(analysisForDevice.get(analysisForDeviceKey));

                    //get seriesData from XYChart.Series to work with
                    ObservableList<XYChart.Data<Number, Number>> seriesData = series.getData();

                    final float initialFrequency = deviceInfo.getInitialFrequency();
                    final float channelSpacingKHz = deviceInfo.getChannelSpacing() / 1000;

                    //set every point to the seriesData
                    float xAxisCounter = initialFrequency;
                    for (Byte value : listMax)
                    {
                        XYChart.Data<Number, Number> data = new XYChart.Data<>(xAxisCounter, value);
                        seriesData.add(data);

                        xAxisCounter += channelSpacingKHz;
                    }

                    /**
                     * Use XYChart.Series
                     * Update series
                     */
                    HashMap<Device, Integer> devToRssiShiftMap = SettingMenu.getInstance().getDevToRssiShiftMap();
                    ObservableList<XYChart.Series<Number, Number>> lineChartData = lineChart.getData();
                    if (Utils.isSeriesExist(lineChartData, seriesName))
                    {
                        lineChartData.forEach(numberSeries -> {

                            if (numberSeries.getName().equals(seriesName))
                            {
                                XYChart.Data<Number, Number> numberData;
                                ObservableList<XYChart.Data<Number, Number>> data = numberSeries.getData();
                                for (int i = 0; i < data.size(); i++)
                                {
                                    numberData = data.get(i);

                                    //TODO it uses around 25% of CPU
                                    numberData.setYValue(seriesData.get(i).getYValue().byteValue() +
                                                                 devToRssiShiftMap.get(deviceInfo.getDevice()));
                                    //---

                                    if (!numberData.getXValue().equals(seriesData.get(i).getXValue()))
                                    {
                                        numberData.setXValue(seriesData.get(i).getXValue());
                                    }
                                }
                                //--------

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
                        });
                    }
                    //or create if does not exist
                    else
                    {
                        //disable stupid series creating animation
                        lineChart.setAnimated(false);
                        lineChartData.add(series);
                        if (enableAnimationChBx.isSelected())
                        {
                            lineChart.setAnimated(true);
                        }
                    }
                }
            }
        }
    }
}
