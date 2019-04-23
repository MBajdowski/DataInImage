package com.mbajdowski;

import com.mbajdowski.ui.EncoderPanel;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Steganography encoder");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(new EncoderPanel().mainPanel);
        frame.pack();
        frame.setVisible(true);
    }
}
