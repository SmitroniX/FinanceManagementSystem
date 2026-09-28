package com.finvantage.util;

import com.finvantage.config.DatabaseConfig;
import com.finvantage.service.AuthenticationService;
import com.finvantage.service.MockDatabaseService;
import com.finvantage.ui.LoginFrame;
import com.finvantage.ui.MainFrame;
import com.finvantage.ui.dialogs.AddAccountDialog;
import com.finvantage.ui.dialogs.AddBudgetDialog;
import com.finvantage.ui.dialogs.AddTransactionDialog;
import com.finvantage.ui.dialogs.DatabaseConfigDialog;
import com.finvantage.ui.dialogs.TransferDialog;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;

/**
 * Utility to capture high-resolution screenshots of FinVantage UI windows,
 * input forms, and analytical output dashboards for academic documentation.
 */
public class ScreenshotGenerator {

    private static final File OUTPUT_DIR = new File("docs/screenshots");

    public static void main(String[] args) throws Exception {
        System.out.println("[Screenshot] Initializing FinVantage Screenshot Generator...");

        if (!OUTPUT_DIR.exists()) {
            OUTPUT_DIR.mkdirs();
        }

        // Setup Modern Look and Feel
        try {
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flatLafClass.getMethod("setup").invoke(null);
        } catch (Exception e) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }

        // Initialize Mock Data and Login
        DatabaseConfig.getInstance().setMode("MOCK");
        AuthenticationService.getInstance().setCurrentUser(MockDatabaseService.getInstance().getMockUser());

        // Run captures on Swing EDT
        SwingUtilities.invokeAndWait(() -> {
            try {
                captureLoginScreen();
                captureMainScreens();
                captureModalDialogs();
                System.out.println("[Screenshot] All screenshots successfully generated in: " + OUTPUT_DIR.getAbsolutePath());
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void captureLoginScreen() throws Exception {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.pack();
        loginFrame.setVisible(true);
        saveComponentScreenshot(loginFrame, "01_login_portal.png");
        loginFrame.dispose();
    }

    private static void captureMainScreens() throws Exception {
        MainFrame mainFrame = new MainFrame();
        mainFrame.pack();
        mainFrame.setSize(1240, 800);
        mainFrame.setVisible(true);

        // Find the CardLayout content panel
        Field contentCardsField = MainFrame.class.getDeclaredField("contentCards");
        contentCardsField.setAccessible(true);
        Container contentCards = (Container) contentCardsField.get(mainFrame);
        CardLayout cardLayout = (CardLayout) contentCards.getLayout();

        // 1. Dashboard
        cardLayout.show(contentCards, "DASHBOARD");
        mainFrame.validate();
        mainFrame.repaint();
        Thread.sleep(100);
        saveComponentScreenshot(mainFrame, "02_dashboard_overview.png");

        // 2. Accounts
        cardLayout.show(contentCards, "ACCOUNTS");
        mainFrame.validate();
        mainFrame.repaint();
        Thread.sleep(100);
        saveComponentScreenshot(mainFrame, "03_accounts_portfolio.png");

        // 3. Transactions Ledger
        cardLayout.show(contentCards, "TRANSACTIONS");
        mainFrame.validate();
        mainFrame.repaint();
        Thread.sleep(100);
        saveComponentScreenshot(mainFrame, "04_transactions_ledger.png");

        // 4. Budgets & Goals
        cardLayout.show(contentCards, "BUDGETS");
        mainFrame.validate();
        mainFrame.repaint();
        Thread.sleep(100);
        saveComponentScreenshot(mainFrame, "05_budgets_monitoring.png");

        // 5. Analytics
        cardLayout.show(contentCards, "ANALYTICS");
        mainFrame.validate();
        mainFrame.repaint();
        Thread.sleep(100);
        saveComponentScreenshot(mainFrame, "06_analytics_reports.png");

        // 6. Audit Trail
        cardLayout.show(contentCards, "AUDIT");
        mainFrame.validate();
        mainFrame.repaint();
        Thread.sleep(100);
        saveComponentScreenshot(mainFrame, "07_audit_trail.png");

        // Keep mainFrame open as parent for dialogs, then dispose later
        captureDialogsWithParent(mainFrame);
        mainFrame.dispose();
    }

    private static void captureDialogsWithParent(JFrame parent) throws Exception {
        // 8. Add Transaction Modal
        AddTransactionDialog txDialog = new AddTransactionDialog(parent);
        txDialog.setModal(false);
        txDialog.pack();
        txDialog.setVisible(true);
        txDialog.validate();
        txDialog.repaint();
        Thread.sleep(100);
        saveComponentScreenshot(txDialog, "08_record_transaction_input.png");
        txDialog.dispose();

        // 9. Inter-Account Transfer Modal
        TransferDialog transferDialog = new TransferDialog(parent);
        transferDialog.setModal(false);
        transferDialog.pack();
        transferDialog.setVisible(true);
        transferDialog.validate();
        transferDialog.repaint();
        Thread.sleep(100);
        saveComponentScreenshot(transferDialog, "09_inter_account_transfer_input.png");
        transferDialog.dispose();

        // 10. Add Account Modal
        AddAccountDialog accDialog = new AddAccountDialog(parent);
        accDialog.setModal(false);
        accDialog.pack();
        accDialog.setVisible(true);
        accDialog.validate();
        accDialog.repaint();
        Thread.sleep(100);
        saveComponentScreenshot(accDialog, "10_add_account_input.png");
        accDialog.dispose();

        // 11. Add Budget Modal
        AddBudgetDialog budgetDialog = new AddBudgetDialog(parent);
        budgetDialog.setModal(false);
        budgetDialog.pack();
        budgetDialog.setVisible(true);
        budgetDialog.validate();
        budgetDialog.repaint();
        Thread.sleep(100);
        saveComponentScreenshot(budgetDialog, "11_set_budget_input.png");
        budgetDialog.dispose();

        // 12. Oracle DB Setup Modal
        DatabaseConfigDialog dbDialog = new DatabaseConfigDialog(parent);
        dbDialog.setModal(false);
        dbDialog.pack();
        dbDialog.setVisible(true);
        dbDialog.validate();
        dbDialog.repaint();
        Thread.sleep(100);
        saveComponentScreenshot(dbDialog, "12_oracle_db_setup_input.png");
        dbDialog.dispose();
    }

    private static void captureModalDialogs() {
        // Handled in captureDialogsWithParent
    }

    private static void saveComponentScreenshot(Component comp, String fileName) throws Exception {
        int width = Math.max(comp.getWidth(), 400);
        int height = Math.max(comp.getHeight(), 300);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        comp.paint(g2);
        g2.dispose();

        File target = new File(OUTPUT_DIR, fileName);
        ImageIO.write(img, "PNG", target);
        System.out.println("[Screenshot] Saved: " + target.getName() + " (" + width + "x" + height + ")");
    }
}
