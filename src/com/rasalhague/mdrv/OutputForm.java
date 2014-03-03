package com.rasalhague.mdrv;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class OutputForm implements Observer
{
    private JPanel    panel;
    private JTextArea textArea;
    private JFrame    frame;

    public OutputForm startGUI()
    {
        frame = new JFrame("OutputForm");
        OutputForm outputForm = new OutputForm();

        textArea = outputForm.textArea;
        panel = outputForm.panel;

        frame.setContentPane(outputForm.panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        return this;
    }

    public void appendTextToTextArea(String textToSet)
    {
        textArea.append(textToSet);
        textArea.append("\r\n");
        textArea.append("\r\n");
    }

    @Override
    public void update(Observable o, Object arg)
    {
        //TODO setChanged(); Wont work
        //        if (o.hasChanged())
        {
            if (arg instanceof ArrayList)
            {
                ArrayList<DataPacket> dataPackets = (ArrayList<DataPacket>) arg;
                appendTextToTextArea(dataPackets.get(dataPackets.size() - 1).getRawDataPacketValue());
            }
        }
    }
}
