/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.*;
import javax.swing.*;

public class RoundedPanel extends JPanel {
    private int cornerRadius;

    public RoundedPanel(int radius) {
        super();
        cornerRadius = radius;
        setOpaque(false); // important for custom painting
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Enable anti-aliasing for smooth corners
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background with rounded corners
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
