package com.rasalhague.mdrv;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainWindow
{
    private JButton button;
    private JPanel jPanel;

    public static void initialize()
    {
        JFrame frame = new JFrame("MainWindow");

        frame.setContentPane(new MainWindow().jPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private MainWindow()
    {
        button.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);

                System.exit(1);
            }
        });
    }
}
