package com.mis.team.ui;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * 对话框网关：正常运行走 JOptionPane；逐步验收测试时可注入自动应答，避免阻塞 EDT。
 */
public final class DialogGateway {
    public interface Handler {
        void showMessage(Component parent, String message, String title, int messageType);

        String showInput(Component parent, Object message, String title);

        String showInputChoice(Component parent, Object message, String title, Object[] options, Object initial);

        int showConfirm(Component parent, Object message, String title);
    }

    private static Handler handler = new DefaultHandler();

    private DialogGateway() {
    }

    public static void setHandler(Handler newHandler) {
        handler = newHandler != null ? newHandler : new DefaultHandler();
    }

    public static void resetHandler() {
        handler = new DefaultHandler();
    }

    public static void showMessage(Component parent, String message, String title, int messageType) {
        handler.showMessage(parent, message, title, messageType);
    }

    public static void showInfo(Component parent, String message, String title) {
        showMessage(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message, String title) {
        showMessage(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message, String title) {
        showMessage(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public static String showInput(Component parent, Object message, String title) {
        return handler.showInput(parent, message, title);
    }

    public static String showInputChoice(Component parent, Object message, String title,
                                           Object[] options, Object initial) {
        return handler.showInputChoice(parent, message, title, options, initial);
    }

    public static int showConfirm(Component parent, Object message, String title) {
        return handler.showConfirm(parent, message, title);
    }

    private static final class DefaultHandler implements Handler {
        @Override
        public void showMessage(Component parent, String message, String title, int messageType) {
            JOptionPane.showMessageDialog(parent, message, title, messageType);
        }

        @Override
        public String showInput(Component parent, Object message, String title) {
            return JOptionPane.showInputDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE);
        }

        @Override
        public String showInputChoice(Component parent, Object message, String title,
                                      Object[] options, Object initial) {
            Object selected = JOptionPane.showInputDialog(parent, message, title,
                    JOptionPane.PLAIN_MESSAGE, null, options, initial);
            return selected != null ? selected.toString() : null;
        }

        @Override
        public int showConfirm(Component parent, Object message, String title) {
            return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
        }
    }
}
