package com.rasalhague.mdrv.gui;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SettingMenu
{
    private Button settingButton;
    private VBox   controlBntsVBox;

    public SettingMenu(Button settingButton, VBox controlBntsVBox)
    {
        this.settingButton = settingButton;
        this.controlBntsVBox = controlBntsVBox;

        setUpBehavior();
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
