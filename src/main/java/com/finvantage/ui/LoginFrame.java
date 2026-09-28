package com.finvantage.ui;

import com.finvantage.config.DatabaseConfig;
import com.finvantage.service.AuthenticationService;
import com.finvantage.service.DatabaseConnectionService;
import com.finvantage.ui.components.CardPanel;
import com.finvantage.ui.dialogs.DatabaseConfigDialog;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Modern Login and User Authentication frame for FinVantage.
 */
public class LoginFrame extends JFrame {

    private final JTextField txtEmail = new JTextField("alex.vance@finvantage.com", 20);
    private final JPasswordField txtPassword = new JPasswordField("Admin@123", 20);
    private final JLabel lblEngineStatus = new JLabel();

    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final DatabaseConfig config = DatabaseConfig.getInstance();

    public LoginFrame() {
        super("FinVantage - Authentication & Access Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 560);
        setResizable(false);
        setLocationRelativeTo(null);

        initComponents();
        refreshDbStatus();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(241, 245, 249));

        JPanel mainCard = new CardPanel(new BorderLayout(), 16, Color.WHITE, new Color(226, 232, 240));
        mainCard.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));

        // Top Brand Header
        JPanel brandGroup = new JPanel();
        brandGroup.setLayout(new BoxLayout(brandGroup, BoxLayout.Y_AXIS));
        brandGroup.setOpaque(false);

        JLabel lblLogo = new JLabel("FinVantage", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblLogo.setForeground(new Color(15, 23, 42));
        lblLogo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Intelligent Finance & Banking Management System", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 116, 139));
        lblSubtitle.setAlignmentX(CENTER_ALIGNMENT);

        brandGroup.add(lblLogo);
        brandGroup.add(Box.createVerticalStrut(4));
        brandGroup.add(lblSubtitle);
        brandGroup.add(Box.createVerticalStrut(25));

        mainCard.add(brandGroup, BorderLayout.NORTH);

        // Center Input Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;
        JLabel lblEmail = new JLabel("Email Address");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEmail.setForeground(new Color(51, 65, 85));
        gbc.gridy = row++;
        form.add(lblEmail, gbc);

        txtEmail.setPreferredSize(new Dimension(0, 36));
        gbc.gridy = row++;
        form.add(txtEmail, gbc);

        gbc.gridy = row++;
        form.add(Box.createVerticalStrut(6), gbc);

        JLabel lblPass = new JLabel("Security Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(new Color(51, 65, 85));
        gbc.gridy = row++;
        form.add(lblPass, gbc);

        txtPassword.setPreferredSize(new Dimension(0, 36));
        gbc.gridy = row++;
        form.add(txtPassword, gbc);

        gbc.gridy = row++;
        form.add(Box.createVerticalStrut(12), gbc);

        // Action Buttons
        JButton btnLogin = new JButton("Sign In to Portal");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setBackground(new Color(37, 99, 235));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(0, 40));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> handleLogin());
        gbc.gridy = row++;
        form.add(btnLogin, gbc);

        JButton btnDemoFill = new JButton("Quick Fill Demo Admin Credentials");
        btnDemoFill.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDemoFill.setForeground(new Color(71, 85, 105));
        btnDemoFill.setBackground(new Color(241, 245, 249));
        btnDemoFill.setFocusPainted(false);
        btnDemoFill.addActionListener(e -> {
            txtEmail.setText("alex.vance@finvantage.com");
            txtPassword.setText("Admin@123");
        });
        gbc.gridy = row++;
        form.add(btnDemoFill, gbc);

        mainCard.add(form, BorderLayout.CENTER);

        // Footer & Oracle Configuration Button
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        lblEngineStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.add(lblEngineStatus, BorderLayout.WEST);

        JButton btnDbSettings = new JButton("Oracle DB Setup");
        btnDbSettings.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDbSettings.setFocusPainted(false);
        btnDbSettings.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDbSettings.addActionListener(e -> {
            new DatabaseConfigDialog(this).setVisible(true);
            refreshDbStatus();
        });
        footer.add(btnDbSettings, BorderLayout.EAST);

        mainCard.add(footer, BorderLayout.SOUTH);

        JPanel outerPadding = new JPanel(new BorderLayout());
        outerPadding.setOpaque(false);
        outerPadding.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        outerPadding.add(mainCard, BorderLayout.CENTER);

        add(outerPadding, BorderLayout.CENTER);
    }

    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String pass = new String(txtPassword.getPassword());

        try {
            boolean success = authService.login(email, pass);
            if (success) {
                dispose();
                new MainFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Invalid email or password.\nUse demo credentials (alex.vance@finvantage.com / Admin@123)\nor check database connection.", 
                    "Authentication Failed", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Login error: " + ex.getMessage() + "\n\nTip: Click 'Oracle DB Setup' to switch to Demo Mode if Oracle DB is not running.", 
                "Connection Notice", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void refreshDbStatus() {
        if (config.isMockMode()) {
            lblEngineStatus.setText("Engine: In-Memory Demo Mode");
            lblEngineStatus.setForeground(new Color(217, 119, 6));
        } else {
            lblEngineStatus.setText("Engine: Oracle Database");
            lblEngineStatus.setForeground(new Color(22, 163, 74));
        }
    }
}
