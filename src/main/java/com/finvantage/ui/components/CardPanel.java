package com.finvantage.ui.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/**
 * Modern card container panel with rounded corners and subtle border styling.
 */
public class CardPanel extends JPanel {

    private final int cornerRadius;
    private final Color backgroundColor;
    private final Color borderColor;

    public CardPanel(LayoutManager layout, int cornerRadius, Color bgColor, Color borderColor) {
        super(layout);
        this.cornerRadius = cornerRadius;
        this.backgroundColor = bgColor != null ? bgColor : Color.WHITE;
        this.borderColor = borderColor != null ? borderColor : new Color(225, 230, 238);
        setOpaque(false);
    }

    public CardPanel(LayoutManager layout) {
        this(layout, 12, Color.WHITE, new Color(225, 230, 238));
    }

    public CardPanel() {
        this(null, 12, Color.WHITE, new Color(225, 230, 238));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Background fill
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // Border outline
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
