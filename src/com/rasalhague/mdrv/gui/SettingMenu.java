package com.rasalhague.mdrv.gui;

import com.rasalhague.mdrv.Utility.Utils;
import com.rasalhague.mdrv.analysis.AnalysisKey;
import com.rasalhague.mdrv.analysis.AnalysisPerformedListener;
import com.rasalhague.mdrv.configuration.ConfigurationLoader;
import com.rasalhague.mdrv.device.core.Device;
import com.rasalhague.mdrv.device.core.DeviceInfo;
import com.rasalhague.mdrv.wirelessadapter.RoundVar;
import com.rasalhague.mdrv.wirelessadapter.WirelessAdapter;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Class that generates setting menu
 */
public class SettingMenu implements AnalysisPerformedListener
{
    private static final SettingMenu ourInstance = new SettingMenu();
    private Button settingButton;
    private VBox   controlBntsVBox;
    private final HashMap<Device, Float>     devToRssiShiftMap               = new HashMap<>();
    private final HashMap<Device, TextField> devToTextFieldChannelSpacingMap = new HashMap<>();
    private final double                     spacing                         = 5;
    private final double                     padding                         = 3;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static SettingMenu getInstance()
    {
        return ourInstance;
    }

    /**
     * Gets dev to rssi shift map.
     *
     * @return the dev to rssi shift map
     */
    public HashMap<Device, Float> getDevToRssiShiftMap()
    {
        return devToRssiShiftMap;
    }

    private SettingMenu()
    {}

    /**
     * Init setting menu.
     *
     * @param settingButton
     *         the setting button
     * @param controlBntsVBox
     *         the control bnts v box
     */
    public void initSettingMenu(Button settingButton, VBox controlBntsVBox)
    {
        this.settingButton = settingButton;
        this.controlBntsVBox = controlBntsVBox;

        setUpBehavior();
    }

    @Override
    public void analysisPerformedEvent(LinkedHashMap<Long, HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>>> analysisResult)
    {
        ArrayList<Long> longs = new ArrayList<>(analysisResult.keySet());
        HashMap<DeviceInfo, HashMap<AnalysisKey, ArrayList<Byte>>> lastMap = analysisResult.get(longs.get(longs.size() -
                                                                                                                  1));

        ArrayList<DeviceInfo> deviceInfos = new ArrayList<>(lastMap.keySet());

        for (DeviceInfo connectedDevice : deviceInfos)
        {
            if (!devToRssiShiftMap.containsKey(connectedDevice.getDevice()))
            {
                //create container, labelChannelSpacing, TextField
                VBox vBoxContainer = new VBox();
                HBox hBoxChannelSpacing = new HBox();
                HBox hBoxRssiShift = new HBox();
                Label labelDeviceName = new Label();
                Label labelChannelSpacing = new Label();
                Label labelRssiShift = new Label();
                TextField textFieldChannelSpacing = new TextField();
                TextField textFieldRssiShift = new TextField();

                //containing
                vBoxContainer.getChildren().add(labelDeviceName);
                vBoxContainer.getChildren().add(hBoxChannelSpacing);
                vBoxContainer.getChildren().add(hBoxRssiShift);

                hBoxChannelSpacing.getChildren().add(labelChannelSpacing);
                hBoxChannelSpacing.getChildren().add(textFieldChannelSpacing);

                hBoxRssiShift.getChildren().add(labelRssiShift);
                hBoxRssiShift.getChildren().add(textFieldRssiShift);

                //configure
                vBoxContainer.setStyle("-fx-border-color: rgba(200, 200, 200, 1);" + "-fx-border-width: 1;");
                vBoxContainer.setPadding(new Insets(padding, padding, padding, padding));
                vBoxContainer.setSpacing(spacing);

                hBoxChannelSpacing.setAlignment(Pos.CENTER_LEFT);
                hBoxChannelSpacing.setSpacing(spacing);
                hBoxRssiShift.setAlignment(Pos.CENTER_LEFT);
                hBoxRssiShift.setSpacing(spacing);

                textFieldChannelSpacing.setPrefWidth(75);
                textFieldRssiShift.setPrefWidth(50);
                textFieldChannelSpacing.setText(String.valueOf(connectedDevice.getChannelSpacing()));
                textFieldRssiShift.setText("0");

                labelChannelSpacing.setText("Channel spacing, kHz");
                labelRssiShift.setText("RSSI shift");
                labelDeviceName.setText(connectedDevice.getFriendlyNameWithId() +
                                                " on " +
                                                connectedDevice.getPortName());

                //behavior
                devToTextFieldChannelSpacingMap.put(connectedDevice.getDevice(), textFieldChannelSpacing);
                textFieldChannelSpacing.textProperty().addListener(new ChangeListener<String>()
                {
                    String defaultChannelSpacing;

                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                    {
                        if (defaultChannelSpacing == null) defaultChannelSpacing = oldValue;

                        if (newValue.matches("((\\d){2,4}.?(\\d){0,4})"))
                        //                    if (Float.valueOf(newValue) >= 100)
                        {
                            connectedDevice.setChannelSpacing(Float.parseFloat(newValue));
                            MainWindowController.forceUpdateChart();
                        }
                        if (newValue.matches("( *)"))
                        {
                            //recursive execute this listener with correct value
                            textFieldChannelSpacing.setText(defaultChannelSpacing);
                        }
                    }
                });

                devToRssiShiftMap.put(connectedDevice.getDevice(), 0f);
                textFieldRssiShift.textProperty().addListener((observable, oldValue, newValue) -> {

                    if (newValue.matches("(-?\\d{1,2})"))
                    {
                        devToRssiShiftMap.put(connectedDevice.getDevice(), Float.valueOf(newValue));
                        MainWindowController.forceUpdateChart();
                    }
                    if (newValue.matches("( *)"))
                    {
                        //recursive execute this listener with correct value
                        textFieldRssiShift.setText("0");
                    }
                });

                //add container
                Platform.runLater(() -> controlBntsVBox.getChildren().add(vBoxContainer));
            }
        }
    }

    public void generateFieldFowChSw(WirelessAdapter wirelessAdapter)
    {
        String channelDefaultValue = ConfigurationLoader.getConfiguration()
                                                        .getApplicationConfiguration()
                                                        .getChannelsToScan();

        HBox container = new HBox();
        container.setStyle("-fx-border-color: rgba(200, 200, 200, 1);" + "-fx-border-width: 1;");
        container.setPadding(new Insets(padding, padding, padding, padding));
        container.setSpacing(spacing);
        container.setAlignment(Pos.CENTER_LEFT);

        TextField textFieldFowChSw = new TextField();
        textFieldFowChSw.setPrefWidth(50);
        textFieldFowChSw.setText(channelDefaultValue);

        Label currentChanelNumberLabel = new Label();

        container.getChildren().add(textFieldFowChSw);
        container.getChildren().add(currentChanelNumberLabel);

        wirelessAdapter.addChannelSwitchingListener(channelNumber -> {
            Platform.runLater(() -> {
                currentChanelNumberLabel.setText(String.valueOf(channelNumber));
            });
        });

        textFieldFowChSw.textProperty().addListener((observable, oldValue, newValue) -> {

            if (textFieldFowChSw.getText()
                                .matches("(?<channelStart>\\d{1,2}[^15-99]*?)(-(?<channelEnd>\\d{1,2}[^15-99]))?"))
            {
                wirelessAdapter.setChannelRoundSwitcher(new RoundVar(Utils.generateArrayToRound(textFieldFowChSw.getText())));
            }
            if (newValue.matches("( *)"))
            {
                //recursive execute this listener with correct value
                textFieldFowChSw.setText(channelDefaultValue);
            }
        });

        Platform.runLater(() -> controlBntsVBox.getChildren().add(container));
    }

    private void setUpBehavior()
    {
        setEvents();
    }

    private void setEvents()
    {
        settingButton.setOnMouseEntered(mouseEvent -> {

            controlBntsVBox.setVisible(true);
            controlBntsVBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        });

        controlBntsVBox.setOnMouseExited(event -> {

            controlBntsVBox.setVisible(false);
            controlBntsVBox.setPrefWidth(0);
        });
    }
}
