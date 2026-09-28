package com.finvantage;

import com.finvantage.config.DatabaseConfig;
import com.finvantage.service.DatabaseConnectionService;
import com.finvantage.ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * FinVantage Application Main Entry Point.
 * 
 * Features:
 * 1. Automatic Look & Feel initialization (FlatLaf with native Swing fallback).
 * 2. Automatic Oracle Database connectivity health check with seamless offline demo fallback.
 * 3. Thread-safe Swing Event Dispatch Thread (EDT) invocation.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println(" FinVantage - Personal & Business Finance Management System");
        System.out.println(" Academic Capstone: OOP & DBMS with Java Swing & Oracle DB");
        System.out.println("==========================================================");

        // 1. Configure Modern Swing Look & Feel
        setupLookAndFeel();

        // 2. Database Connectivity Diagnostic
        setupDatabaseMode();

        // 3. Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    private static void setupLookAndFeel() {
        try {
            // Attempt FlatLaf modern styling
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flatLafClass.getMethod("setup").invoke(null);
            System.out.println("[UI] FlatLaf Modern Theme initialized.");
        } catch (Exception e) {
            // Graceful fallback to System or Nimbus Look & Feel
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
                System.out.println("[UI] Nimbus Look & Feel applied as fallback.");
            } catch (Exception ex) {
                System.out.println("[UI] Standard Look & Feel applied.");
            }
        }
    }

    private static void setupDatabaseMode() {
        DatabaseConfig config = DatabaseConfig.getInstance();
        System.out.println("[DB] Testing connectivity to Oracle Database at: " + config.getUrl());

        boolean oracleConnected = false;
        try {
            oracleConnected = DatabaseConnectionService.getInstance().testConnection();
        } catch (Exception e) {
            oracleConnected = false;
        }

        if (oracleConnected) {
            config.setMode("ORACLE");
            System.out.println("[DB] SUCCESS: Connected to live Oracle Database!");
        } else {
            config.setMode("MOCK");
            System.out.println("[DB] NOTICE: Oracle Database not currently reachable.");
            System.out.println("[DB] NOTICE: Automatically enabling In-Memory Demo Mode for seamless evaluation.");
            System.out.println("[DB] NOTICE: You can configure Oracle settings via the UI (Oracle DB Setup button).");
        }
    }
}
