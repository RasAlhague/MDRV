package com.rasalhague.mdrv.gui;

import com.rasalhague.mdrv.DeviceInfo;
import com.rasalhague.mdrv.connectionlistener.DeviceConnectionListenerI;
import com.rasalhague.mdrv.connectionlistener.DeviceConnectionStateEnum;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.HashMap;

/**
 * Class that generates setting menu
 */
public class SettingMenu implements DeviceConnectionListenerI
{
    private static final SettingMenu ourInstance = new SettingMenu();
    private Button settingButton;
    private VBox   controlBntsVBox;
    private final HashMap<String, Float>     devToRssiShiftMap               = new HashMap<>();
    private final HashMap<String, TextField> devToTextFieldChannelSpacingMap = new HashMap<>();

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
    public HashMap<String, Float> getDevToRssiShiftMap()
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
    public void deviceConnectionEvent(DeviceInfo connectedDevice, DeviceConnectionStateEnum deviceConnectionStateEnum)
    {
        if (deviceConnectionStateEnum == DeviceConnectionStateEnum.CONNECTED &&
                connectedDevice.getChannelSpacing() != 0)
        {
            /**
             * Settings
             */
            if (!devToRssiShiftMap.containsKey(connectedDevice.getName()))
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
                vBoxContainer.setPadding(new Insets(3, 3, 3, 3));

                hBoxChannelSpacing.setAlignment(Pos.CENTER_LEFT);
                hBoxRssiShift.setAlignment(Pos.CENTER_LEFT);

                textFieldChannelSpacing.setPrefWidth(75);
                textFieldRssiShift.setPrefWidth(50);
                textFieldChannelSpacing.setText(String.valueOf(connectedDevice.getChannelSpacing()));
                textFieldRssiShift.setText("0");

                labelChannelSpacing.setText("Channel spacing, kHz");
                labelRssiShift.setText("RSSI shift");
                labelDeviceName.setText(connectedDevice.getName());

                //behavior
                devToTextFieldChannelSpacingMap.put(connectedDevice.getName(), textFieldChannelSpacing);
                textFieldChannelSpacing.textProperty().addListener((observable, oldValue, newValue) -> {

                    if (Float.valueOf(newValue) >= 100)
                    {
                        connectedDevice.setChannelSpacing(Float.parseFloat(newValue));
                    }
                });

                devToRssiShiftMap.put(connectedDevice.getName(), 0f);
                textFieldRssiShift.textProperty().addListener((observable, oldValue, newValue) -> {

                    if (!newValue.equals("") && !newValue.equals("-"))
                    {
                        devToRssiShiftMap.put(connectedDevice.getName(), Float.valueOf(newValue));
                        MainWindowController.forceUpdateChart();
                    }
                });

                //add container
                Platform.runLater(() -> {

                    controlBntsVBox.getChildren().add(vBoxContainer);
                });
            }
            else
            {
                //behavior
                TextField textFieldChannelSpacing = devToTextFieldChannelSpacingMap.get(connectedDevice.getName());
                textFieldChannelSpacing.textProperty().addListener((observable, oldValue, newValue) -> {

                    if (Float.valueOf(newValue) >= 100)
                    {
                        connectedDevice.setChannelSpacing(Float.parseFloat(newValue));
                    }
                });
            }
        }
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
