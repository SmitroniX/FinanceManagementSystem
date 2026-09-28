package com.finvantage.ui.components;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * CustomTable provides high-contrast, modern flat table styling with
 * custom cell renderers for financial badges, amounts, and statuses.
 */
public class CustomTable extends JTable {

    public CustomTable(DefaultTableModel model) {
        super(model);
        initStyle();
    }

    private void initStyle() {
        setRowHeight(36);
        setFont(new Font("Segoe UI", Font.PLAIN, 12));
        setShowGrid(false);
        setIntercellSpacing(new java.awt.Dimension(0, 0));
        setSelectionBackground(new Color(236, 242, 255));
        setSelectionForeground(new Color(30, 41, 59));

        // Modern Header
        JTableHeader header = getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        header.setPreferredSize(new java.awt.Dimension(0, 36));

        // Default cell padding
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                                                           boolean isSelected, boolean hasFocus, 
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        setDefaultRenderer(Object.class, defaultRenderer);
    }

    /**
     * Cell renderer for color-coded financial amounts (+Green, -Red, ⇆Blue).
     */
    public static TableCellRenderer getAmountRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 15));

                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }

                String text = value != null ? value.toString() : "";
                if (text.startsWith("+")) {
                    label.setForeground(new Color(39, 174, 96));
                } else if (text.startsWith("-")) {
                    label.setForeground(new Color(231, 76, 60));
                } else {
                    label.setForeground(new Color(41, 128, 185));
                }
                return label;
            }
        };
    }

    /**
     * Cell renderer for transaction type badges.
     */
    public static TableCellRenderer getTypeBadgeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 10));

                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }

                String type = value != null ? value.toString().toUpperCase() : "";
                if ("INCOME".equals(type)) {
                    label.setForeground(new Color(39, 174, 96));
                    label.setText("● INCOME");
                } else if ("EXPENSE".equals(type)) {
                    label.setForeground(new Color(231, 76, 60));
                    label.setText("● EXPENSE");
                } else {
                    label.setForeground(new Color(41, 128, 185));
                    label.setText("● TRANSFER");
                }
                return label;
            }
        };
    }
}
