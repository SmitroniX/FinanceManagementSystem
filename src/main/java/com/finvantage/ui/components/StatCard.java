package com.finvantage.ui.components;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

/**
 * Metric summary card displaying key financial performance indicators.
 */
public class StatCard extends CardPanel {

    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtext;
    private final Color accentColor;

    public StatCard(String title, String value, String subtext, Color accentColor) {
        super(new BorderLayout(10, 5), 14, Color.WHITE, new Color(228, 233, 242));
        this.accentColor = accentColor != null ? accentColor : new Color(41, 128, 185);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 18));

        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(new Color(110, 120, 135));

        lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(new Color(30, 41, 59));

        lblSubtext = new JLabel(subtext);
        lblSubtext.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtext.setForeground(new Color(100, 116, 139));

        GridLayout grid = new GridLayout(3, 1, 0, 3);
        CardPanel contentPanel = new CardPanel(grid, 0, Color.WHITE, Color.WHITE);
        contentPanel.add(lblTitle);
        contentPanel.add(lblValue);
        contentPanel.add(lblSubtext);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void updateData(String value, String subtext) {
        lblValue.setText(value);
        lblSubtext.setText(subtext);
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Paint left accent indicator pill
        g2.setColor(accentColor);
        g2.fillRoundRect(8, 14, 5, getHeight() - 28, 4, 4);
        g2.dispose();
    }
}
