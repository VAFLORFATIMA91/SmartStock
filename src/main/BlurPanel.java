package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class BlurPanel extends JPanel {

    private Image background;

    public BlurPanel(String path) {
        background = new ImageIcon(getClass().getResource(path)).getImage();
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (background != null) {
            Graphics2D g2d = (Graphics2D) g.create();

            // Draw background image
            g2d.drawImage(background, 0, 0, getWidth(), getHeight(), this);

            // Blur overlay (transparent white layer)
            g2d.setColor(new Color(255,255,255,120)); 
            g2d.fillRect(0,0,getWidth(),getHeight());

            g2d.dispose();
        }
    }
}