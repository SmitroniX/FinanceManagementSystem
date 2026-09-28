package com.finvantage.ui;

import com.finvantage.config.DatabaseConfig;
import com.finvantage.model.User;
import com.finvantage.service.AuthenticationService;
import com.finvantage.service.DatabaseConnectionService;
import com.finvantage.ui.dialogs.DatabaseConfigDialog;
import com.finvantage.ui.panels.AccountsPanel;
import com.finvantage.ui.panels.AnalyticsPanel;
import com.finvantage.ui.panels.AuditLogPanel;
import com.finvantage.ui.panels.BudgetsPanel;
import com.finvantage.ui.panels.DashboardPanel;
import com.finvantage.ui.panels.TransactionsPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window for FinVantage with modern dark-accented sidebar,
 * top status bar, and CardLayout navigation.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final List<JButton> navButtons = new ArrayList<>();
    private final JLabel lblDbStatus = new JLabel("● Database: Checking...");

    public MainFrame() {
        super("FinVantage - Intelligent Finance & Banking Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);

        initComponents();
        updateDatabaseStatusPill();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Sidebar (West)
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Top Bar (North)
        JPanel topBar = createTopBar();

        // 3. Central Panels (Cards)
        contentCards.add(new DashboardPanel(this), "DASHBOARD");
        contentCards.add(new AccountsPanel(this), "ACCOUNTS");
        contentCards.add(new TransactionsPanel(this), "TRANSACTIONS");
        contentCards.add(new BudgetsPanel(this), "BUDGETS");
        contentCards.add(new AnalyticsPanel(this), "ANALYTICS");
        contentCards.add(new AuditLogPanel(this), "AUDIT");

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(topBar, BorderLayout.NORTH);
        centerWrapper.add(contentCards, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(15, 23, 42)); // Modern Deep Slate
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 16, 20, 16));

        // Brand Logo & Title
        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandPanel.setOpaque(false);
        brandPanel.setMaximumSize(new Dimension(208, 50));

        JLabel lblBrand = new JLabel("FinVantage");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBrand.setForeground(new Color(248, 250, 252));

        JLabel lblTag = new JLabel("ENTERPRISE LEDGER & DBMS");
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTag.setForeground(new Color(56, 189, 248)); // Cyan accent

        brandPanel.add(lblBrand);
        brandPanel.add(lblTag);
        sidebar.add(brandPanel);
        sidebar.add(Box.createVerticalStrut(30));

        // Navigation Items
        addNavButton(sidebar, "  📊   Dashboard", "DASHBOARD", true);
        addNavButton(sidebar, "  💳   Accounts", "ACCOUNTS", false);
        addNavButton(sidebar, "  💸   Transactions", "TRANSACTIONS", false);
        addNavButton(sidebar, "  🎯   Budgets", "BUDGETS", false);
        addNavButton(sidebar, "  📈   Analytics", "ANALYTICS", false);
        addNavButton(sidebar, "  🛡️   Audit Trail", "AUDIT", false);

        sidebar.add(Box.createVerticalGlue());

        // Database Settings Button at bottom of sidebar
        JButton btnSettings = new JButton("  ⚙️   Oracle DB Setup");
        btnSettings.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSettings.setForeground(new Color(203, 213, 225));
        btnSettings.setBackground(new Color(30, 41, 59));
        btnSettings.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        btnSettings.setFocusPainted(false);
        btnSettings.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSettings.setMaximumSize(new Dimension(208, 40));
        btnSettings.setHorizontalAlignment(SwingConstants.LEFT);
        btnSettings.addActionListener(e -> {
            DatabaseConfigDialog dialog = new DatabaseConfigDialog(this);
            dialog.setVisible(true);
            updateDatabaseStatusPill();
        });

        sidebar.add(btnSettings);
        return sidebar;
    }

    private void addNavButton(JPanel sidebar, String text, String cardName, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setMaximumSize(new Dimension(208, 42));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (active) {
            btn.setBackground(new Color(37, 99, 235));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(15, 23, 42));
            btn.setForeground(new Color(148, 163, 184));
        }
        btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        btn.addActionListener(e -> {
            cardLayout.show(contentCards, cardName);
            for (JButton b : navButtons) {
                b.setBackground(new Color(15, 23, 42));
                b.setForeground(new Color(148, 163, 184));
            }
            btn.setBackground(new Color(37, 99, 235));
            btn.setForeground(Color.WHITE);
        });

        navButtons.add(btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(6));
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setPreferredSize(new Dimension(0, 56));
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(0, 25, 0, 25)
        ));

        // Left: Database Status Indicator Pill
        lblDbStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        topBar.add(lblDbStatus, BorderLayout.WEST);

        // Right: User Profile & Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        userPanel.setOpaque(false);

        User currentUser = AuthenticationService.getInstance().getCurrentUser();
        String displayName = currentUser != null ? currentUser.getFullName() : "Administrator";
        String role = currentUser != null ? currentUser.getRole().name() : "USER";

        JLabel lblUser = new JLabel(displayName + " [" + role + "]");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(new Color(30, 41, 59));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            AuthenticationService.getInstance().logout();
            dispose();
            new LoginFrame().setVisible(true);
        });

        userPanel.add(lblUser);
        userPanel.add(btnLogout);
        topBar.add(userPanel, BorderLayout.EAST);

        return topBar;
    }

    public void updateDatabaseStatusPill() {
        DatabaseConfig cfg = DatabaseConfig.getInstance();
        if (cfg.isMockMode()) {
            lblDbStatus.setText("● Active Engine: In-Memory Demo Mode (Offline)");
            lblDbStatus.setForeground(new Color(217, 119, 6));
        } else {
            lblDbStatus.setText("● Active Engine: Oracle Database (Connected)");
            lblDbStatus.setForeground(new Color(22, 163, 74));
        }
    }
}
