package com.rasalhague.mdrv.gui;

import com.rasalhague.mdrv.wirelessadapter.WirelessAdapterData;
import com.rasalhague.mdrv.wirelessadapter.WirelessAdapterDataListener;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WirelessAdapterDataVisualizer implements WirelessAdapterDataListener
{
    private static WirelessAdapterDataVisualizer           ourInstance        = new WirelessAdapterDataVisualizer();
    private        HashMap<Byte, Polygon>                  channelToG         = new HashMap<>();
    private        HashMap<String, HashMap<Byte, Polygon>> standartToChannel  = new HashMap<>();
    private        double                                  fadeOutPerTick     = 0.01;
    private        double                                  fadeUpPerPacket    = 0.0005;
    private        double                                  maxOpacity         = 0.6;
    private        int                                     fadeOutFrequencyMs = 100;

    public static WirelessAdapterDataVisualizer getInstance()
    {
        return ourInstance;
    }

    private WirelessAdapterDataVisualizer()
    {
    }

    public void init(GridPane spectralMasksGridPane) throws IOException
    {
        String spectralMaskG = "/com/rasalhague/mdrv/gui/view/Spectral_Mask_g.fxml";
        Polygon spectralMaskGPolygon;

        for (byte b = 1; b <= 14; b++)
        {
            spectralMaskGPolygon = FXMLLoader.load(getClass().getResource(spectralMaskG));
            spectralMaskGPolygon.setOpacity(0.5);

            if (b != 14)
            {
                spectralMasksGridPane.add(spectralMaskGPolygon, b + 1, 1);
            }
            else
            {
                spectralMasksGridPane.add(spectralMaskGPolygon, 16, 1);
            }

            channelToG.put(b, spectralMaskGPolygon);
        }

        standartToChannel.put("g", channelToG);
        //TODO
        standartToChannel.put("b", channelToG);
        standartToChannel.put("n", channelToG);

        //        maxOpacity = 1;
        //        fadeOutPerTick = maxOpacity / (Float.valueOf(2) * 1000 / fadeOutFrequencyMs);
        startFadeOut();
    }

    private void startFadeOut()
    {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

            Platform.runLater(() -> {
                channelToG.values().forEach(polygon -> {

                    double polygonOpacity = polygon.getOpacity();
                    if (polygonOpacity >= 0)
                    {
                        polygon.setOpacity(polygonOpacity - fadeOutPerTick);
                    }
                });
            });

        }, 0, fadeOutFrequencyMs, TimeUnit.MILLISECONDS);
    }

    public void setUpSettings(VBox controlBntsVBox)
    {
        //containers
        VBox mainVBox = new VBox();
        HBox freqHBox = new HBox();
        HBox maxOpacityHBox = new HBox();
        HBox fadeUpOpacityHBox = new HBox();

        Label freqLabel = new Label();
        Label maxOpacityLabel = new Label();
        Label fadeUpOpacityLabel = new Label();

        TextField freqTextField = new TextField();
        TextField maxOpacityTextField = new TextField();
        TextField fadeUpOpacityTextField = new TextField();

        //containing
        mainVBox.getChildren().add(freqHBox);
        mainVBox.getChildren().add(maxOpacityHBox);
        mainVBox.getChildren().add(fadeUpOpacityHBox);

        freqHBox.getChildren().add(freqLabel);
        freqHBox.getChildren().add(freqTextField);
        freqHBox.setAlignment(Pos.CENTER_LEFT);

        maxOpacityHBox.getChildren().add(maxOpacityLabel);
        maxOpacityHBox.getChildren().add(maxOpacityTextField);
        maxOpacityHBox.setAlignment(Pos.CENTER_LEFT);

        fadeUpOpacityHBox.getChildren().add(fadeUpOpacityLabel);
        fadeUpOpacityHBox.getChildren().add(fadeUpOpacityTextField);
        fadeUpOpacityHBox.setAlignment(Pos.CENTER_LEFT);

        //configuring
        mainVBox.setStyle("-fx-border-color: rgba(200, 200, 200, 1);" + "-fx-border-width: 1;");
        mainVBox.setPadding(new Insets(3, 3, 3, 3));
        mainVBox.setSpacing(3);

        freqLabel.setText("Fade out after");
        maxOpacityLabel.setText("Max opacity");
        fadeUpOpacityLabel.setText("Fade up opacity");

        freqHBox.setSpacing(3);
        maxOpacityHBox.setSpacing(3);
        fadeUpOpacityHBox.setSpacing(3);

        freqTextField.setPrefWidth(50);
        freqTextField.setText("5");
        maxOpacityTextField.setPrefWidth(50);
        maxOpacityTextField.setText("0.6");
        fadeUpOpacityTextField.setPrefWidth(100);
        fadeUpOpacityTextField.setText("0.0005");

        freqTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            if (!newValue.equals(""))
            {
                fadeOutPerTick = maxOpacity / (Float.valueOf(newValue) * 1000 / fadeOutFrequencyMs);
            }
        });

        maxOpacityTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            Platform.runLater(() -> {

                Float valueOf = Float.valueOf(newValue);

                if (!newValue.equals("") && valueOf <= 1)
                {
                    maxOpacity = valueOf;
                    fadeOutPerTick = maxOpacity / (Float.valueOf(freqTextField.getText()) * 1000 / fadeOutFrequencyMs);
                }
            });
        });

        fadeUpOpacityTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            if (!newValue.equals(""))
            {
                fadeUpPerPacket = Float.valueOf(newValue);
            }
        });

        controlBntsVBox.getChildren().add(mainVBox);
    }

    @Override
    public void wirelessAdapterDataEvent(WirelessAdapterData wirelessAdapterData)
    {
        processWirelessAdapterData(wirelessAdapterData);
    }

    private void processWirelessAdapterData(WirelessAdapterData wirelessAdapterData)
    {
        Platform.runLater(() -> {

            HashMap<Byte, Polygon> standartPolygons = standartToChannel.get(wirelessAdapterData.getStandart());

            if (standartPolygons != null)
            {
                Polygon polygon = standartPolygons.get(wirelessAdapterData.getChannel());
                if (polygon.getOpacity() <= maxOpacity)
                {
                    polygon.setOpacity(polygon.getOpacity() + fadeUpPerPacket);
                }
            }
        });
    }
}
