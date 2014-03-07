package com.rasalhague.mdrv.gui;

import com.rasalhague.mdrv.DeviceConnectionListener;
import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.analysis.AnalysisKey;
import com.rasalhague.mdrv.analysis.AnalysisPerformedListener;
import com.rasalhague.mdrv.analysis.PacketAnalysis;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import com.rasalhague.mdrv.logging.TextAreaHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
    public Button refreshChartButton;

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

        final String CSS_RESOURCE_PATH = "/com/rasalhague/mdrv/gui/resources/style.css";

        Parent root = FXMLLoader.load(getClass().getResource("/com/rasalhague/mdrv/gui/view/MainWindow.fxml"));
        final Scene scene = new Scene(root);
        scene.getStylesheets().add(CSS_RESOURCE_PATH);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent windowEvent)
            {
                Platform.exit();
            }
        });

        stage.setScene(scene);
        stage.show();

        lineChart = (LineChart<Number, Number>) scene.lookup("#lineChart");
        debugTextArea = (TextArea) scene.lookup("#debugTextArea");

        ApplicationLogger.addCustomHandler(new TextAreaHandler(debugTextArea));
        PacketAnalysis.getInstance().addListener(getInstance());
    }

    public void startListeningButtonMouseClickedEvent(Event event)
    {
        DeviceConnectionListener devConnListener = new DeviceConnectionListener();
        Thread devConnListenerThread = new Thread(devConnListener);
        devConnListenerThread.setDaemon(true);
        devConnListenerThread.start();
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
                lineChart.getData().clear();

                Set<DeviceInfo> keySet = analysisResult.keySet();

                for (DeviceInfo deviceInfo : keySet)
                {
                    ArrayList<Integer> list = analysisResult.get(deviceInfo).get(AnalysisKey.MAX);

                    final XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();

                    int xAxisCounter = 1;
                    for (Integer value : list)
                    {
                        XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>(xAxisCounter, value);
                        series.getData().add(data);

                        xAxisCounter++;
                    }

                    lineChart.getData().add(series);
                }
            }
        });
    }
}
