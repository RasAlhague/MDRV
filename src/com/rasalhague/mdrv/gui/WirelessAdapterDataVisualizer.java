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

/**
 * Class that receive wirelessAdapterDataEvent and visualize it data
 */
public class WirelessAdapterDataVisualizer implements WirelessAdapterDataListener
{
    private static final WirelessAdapterDataVisualizer           ourInstance        = new WirelessAdapterDataVisualizer();
    private final        HashMap<Byte, Polygon>                  channelToG         = new HashMap<>();
    private final HashMap<Byte, Polygon> channelMasks    = new HashMap<>();
    private final        HashMap<String, HashMap<Byte, Polygon>> standartToChannel  = new HashMap<>();
    private       float                  fadeOutPerTick  = 0.01f;
    private float fadeUpPerPacket = 0.008f;
    private       float                  maxOpacity      = 0.6f;
    private final        int                                     fadeOutFrequencyMs = 100;
    private int   fullFadeAfter   = 30;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static WirelessAdapterDataVisualizer getInstance()
    {
        return ourInstance;
    }

    private WirelessAdapterDataVisualizer()
    {
    }

    /**
     * Init void.
     *
     * @param spectralMasksGridPane
     *         the spectral masks grid pane
     *
     * @throws IOException
     *         the iO exception
     */
    public void init(GridPane spectralMasksGridPane) throws IOException
    {
        //        String spectralMaskG = "/com/rasalhague/mdrv/gui/view/Spectral_Mask_g.fxml";
        String channelMask = "/com/rasalhague/mdrv/gui/view/ChannelMask.fxml";
        Polygon spectralMaskPolygon;

        for (byte b = 1; b <= 14; b++)
        {
            spectralMaskPolygon = FXMLLoader.load(getClass().getResource(channelMask));
            spectralMaskPolygon.setOpacity(0.5);
            spectralMaskPolygon.setVisible(true);

            if (b != 14)
            {
                spectralMasksGridPane.add(spectralMaskPolygon, b + 1, 1);
            }
            else
            {
                spectralMasksGridPane.add(spectralMaskPolygon, 16, 1);
            }

            //            channelToG.put(b, spectralMaskPolygon);
            channelMasks.put(b, spectralMaskPolygon);
        }

        standartToChannel.put("g", channelMasks);
        standartToChannel.put("b", channelMasks);
        standartToChannel.put("n", channelMasks);

        //        maxOpacity = 1;
        //        fadeOutPerTick = maxOpacity / (Float.valueOf(2) * 1000 / fadeOutFrequencyMs);
        startFadeOut();
    }

    private void startFadeOut()
    {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

            Platform.runLater(() -> {
                channelMasks.values().forEach(polygon -> {

                    double polygonOpacity = polygon.getOpacity();
                    if (polygonOpacity >= 0)
                    {
                        polygon.setOpacity(polygonOpacity - fadeOutPerTick);
                    }
                });
            });

        }, 0, fadeOutFrequencyMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Create buttons and labels
     *
     * @param controlBntsVBox
     *         the control bnts v box
     */
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
        freqTextField.setText(String.valueOf(fullFadeAfter));
        maxOpacityTextField.setPrefWidth(50);
        maxOpacityTextField.setText(String.valueOf(maxOpacity));
        fadeUpOpacityTextField.setPrefWidth(100);
        fadeUpOpacityTextField.setText(String.valueOf(fadeUpPerPacket));

        fadeOutPerTick = maxOpacity / (Float.valueOf(freqTextField.getText()) * 1000 / fadeOutFrequencyMs);

        freqTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            if (!newValue.equals(""))
            {
                fadeOutPerTick = maxOpacity / (Float.valueOf(newValue) * 1000 / fadeOutFrequencyMs);
                System.out.println(fadeOutPerTick);
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
