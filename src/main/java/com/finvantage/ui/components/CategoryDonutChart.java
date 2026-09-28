package com.finvantage.ui.components;

import com.finvantage.service.ReportService.CategoryMetric;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom Java2D Donut Chart rendering category expense allocations.
 * Demonstrates high-resolution 2D graphics rendering in Java Swing.
 */
public class CategoryDonutChart extends JPanel {

    private final List<CategorySlice> slices = new ArrayList<>();
    private BigDecimal totalExpense = BigDecimal.ZERO;

    private static final Color[] PALETTE = {
        new Color(231, 76, 60),  // Red
        new Color(230, 126, 34), // Orange
        new Color(155, 89, 182), // Purple
        new Color(52, 152, 219), // Blue
        new Color(46, 204, 113), // Green
        new Color(241, 196, 15), // Yellow
        new Color(52, 73, 94)    // Slate
    };

    public CategoryDonutChart() {
        setPreferredSize(new Dimension(420, 240));
        setOpaque(false);
    }

    public void updateData(Map<String, CategoryMetric> categoryMetrics) {
        slices.clear();
        totalExpense = BigDecimal.ZERO;
        if (categoryMetrics != null && !categoryMetrics.isEmpty()) {
            int colorIdx = 0;
            for (CategoryMetric metric : categoryMetrics.values()) {
                Color c = PALETTE[colorIdx % PALETTE.length];
                slices.add(new CategorySlice(metric.getCategoryName(), metric.getTotalAmount(), metric.getPercentage(), c));
                totalExpense = totalExpense.add(metric.getTotalAmount());
                colorIdx++;
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Header Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(51, 65, 85));
        g2.drawString("Expense Breakdown by Category", 15, 20);

        if (slices.isEmpty() || totalExpense.compareTo(BigDecimal.ZERO) <= 0) {
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            g2.setColor(new Color(148, 163, 184));
            g2.drawString("No expense transactions recorded yet.", 30, 120);
            g2.dispose();
            return;
        }

        // Geometry for Donut
        int diameter = Math.min(150, Math.min(getWidth() / 2 - 20, getHeight() - 50));
        int donutX = 20;
        int donutY = 40;

        int currentAngle = 0;
        for (CategorySlice slice : slices) {
            int arcAngle = (int) Math.round((slice.percentage / 100.0) * 360.0);
            if (arcAngle <= 0) arcAngle = 1;
            g2.setColor(slice.color);
            g2.fillArc(donutX, donutY, diameter, diameter, currentAngle, arcAngle);
            currentAngle += arcAngle;
        }

        // Cut out center hole
        int holeDiameter = (int) (diameter * 0.58);
        int holeX = donutX + (diameter - holeDiameter) / 2;
        int holeY = donutY + (diameter - holeDiameter) / 2;
        g2.setColor(Color.WHITE);
        g2.fillOval(holeX, holeY, holeDiameter, holeDiameter);

        // Draw Center Total
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(30, 41, 59));
        String totalStr = String.format("$%.0f", totalExpense.doubleValue());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(totalStr, holeX + (holeDiameter - fm.stringWidth(totalStr)) / 2, holeY + (holeDiameter / 2) + 4);

        // Draw Legend on the right side
        int legendX = donutX + diameter + 30;
        int legendY = 45;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        for (int i = 0; i < Math.min(slices.size(), 6); i++) {
            CategorySlice slice = slices.get(i);
            // Color dot
            g2.setColor(slice.color);
            g2.fillRoundRect(legendX, legendY, 10, 10, 3, 3);

            // Name and percentage
            g2.setColor(new Color(51, 65, 85));
            String text = String.format("%s (%.1f%%)", truncate(slice.name, 18), slice.percentage);
            g2.drawString(text, legendX + 16, legendY + 9);

            legendY += 22;
        }

        g2.dispose();
    }

    private String truncate(String str, int max) {
        if (str == null) return "";
        return str.length() > max ? str.substring(0, max - 3) + "..." : str;
    }

    private static class CategorySlice {
        final String name;
        final BigDecimal amount;
        final double percentage;
        final Color color;

        CategorySlice(String name, BigDecimal amount, double percentage, Color color) {
            this.name = name;
            this.amount = amount;
            this.percentage = percentage;
            this.color = color;
        }
    }
}
