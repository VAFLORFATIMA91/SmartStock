package main;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {

    private Image image;

    public ImagePanel(String path) {
        try {
            image = new ImageIcon(getClass().getResource(path)).getImage();
        } catch (Exception e) {
            System.out.println("Image not found: " + path);
        }
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
    }
}