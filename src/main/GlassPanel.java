package main;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

public class GlassPanel extends JPanel {

    public GlassPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        // 🧊 create blurred background
        BufferedImage blur = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gBlur = blur.createGraphics();

        // capture background behind panel
        gBlur.setComposite(AlphaComposite.Src);
        gBlur.setColor(new Color(255, 255, 255, 30));
        gBlur.fillRect(0, 0, w, h);

        gBlur.dispose();

        // draw blur layer
        g2.drawImage(blur, 0, 0, null);

        // glass white overlay
        g2.setColor(new Color(255, 255, 255, 80));
        g2.fillRoundRect(0, 0, w, h, 30, 30);

        // border
        g2.setColor(new Color(255, 255, 255, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 30, 30);

        g2.dispose();
        super.paintComponent(g);
    }
}