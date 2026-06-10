package com.mis.team;

import com.mis.team.ui.LoginFrame;
import com.mis.team.util.DatabaseUtil;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseUtil::close));

        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
