package com.finvantage.ui.components;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;

/**
 * Custom-rendered Java2D bar chart displaying monthly cash flow metrics (Income, Expense, Savings).
 * Demonstrates OOP Custom Swing Component Design and Graphics2D rendering.
 */
public class CashFlowBarChart extends JPanel {

    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal expense = BigDecimal.ZERO;
    private BigDecimal savings = BigDecimal.ZERO;

    public CashFlowBarChart() {
        setPreferredSize(new Dimension(360, 240));
        setOpaque(false);
    }

    public void updateMetrics(BigDecimal income, BigDecimal expense) {
        this.income = income != null ? income : BigDecimal.ZERO;
        this.expense = expense != null ? expense : BigDecimal.ZERO;
        this.savings = this.income.subtract(this.expense);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int paddingBottom = 45;
        int paddingTop = 35;
        int chartHeight = height - paddingTop - paddingBottom;

        // Draw title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(51, 65, 85));
        g2.drawString("Cash Flow Summary (Current Month)", 15, 20);

        // Calculate max value for scaling
        double maxVal = Math.max(income.doubleValue(), Math.max(expense.doubleValue(), Math.max(savings.doubleValue(), 100.0)));
        maxVal = maxVal * 1.15; // 15% headroom

        // Draw baseline
        int baseY = height - paddingBottom;
        g2.setColor(new Color(226, 232, 240));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(20, baseY, width - 20, baseY);

        // Bar definitions
        String[] labels = {"Inflow", "Outflow", "Net Savings"};
        double[] values = {income.doubleValue(), expense.doubleValue(), savings.doubleValue()};
        Color[] topColors = {new Color(46, 204, 113), new Color(231, 76, 60), new Color(52, 152, 219)};
        Color[] botColors = {new Color(39, 174, 96), new Color(192, 57, 43), new Color(41, 128, 185)};

        int barWidth = Math.min(55, (width - 60) / 4);
        int spacing = (width - 40 - (3 * barWidth)) / 4;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < 3; i++) {
            int x = 25 + spacing + i * (barWidth + spacing);
            double val = Math.max(0, values[i]);
            int bHeight = (int) ((val / maxVal) * chartHeight);
            int y = baseY - bHeight;

            // Gradient bar fill
            GradientPaint gp = new GradientPaint(x, y, topColors[i], x, baseY, botColors[i]);
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, barWidth, bHeight, 8, 8);

            // Value text above bar
            String valStr = String.format("$%.0f", values[i]);
            int textX = x + (barWidth - fm.stringWidth(valStr)) / 2;
            g2.setColor(new Color(71, 85, 105));
            g2.drawString(valStr, textX, Math.max(paddingTop - 5, y - 6));

            // Label text below bar
            g2.setColor(new Color(100, 116, 139));
            int lblX = x + (barWidth - fm.stringWidth(labels[i])) / 2;
            g2.drawString(labels[i], lblX, baseY + 18);
        }

        g2.dispose();
    }
}
