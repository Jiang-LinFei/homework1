package com.mis.team.ui;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.ArrayDeque;
import java.util.Queue;

/** 逐步验收用：按队列返回输入，消息框自动忽略，确认框默认同意 */
public class TestDialogHandler implements DialogGateway.Handler {
    private final Queue<Object> queue = new ArrayDeque<>();
    private int defaultConfirm = JOptionPane.YES_OPTION;

    public TestDialogHandler queueInput(String value) {
        queue.add(value);
        return this;
    }

    public TestDialogHandler queueChoice(String value) {
        queue.add(value);
        return this;
    }

    public TestDialogHandler queueConfirmYes() {
        defaultConfirm = JOptionPane.YES_OPTION;
        return this;
    }

    public TestDialogHandler queueConfirmNo() {
        defaultConfirm = JOptionPane.NO_OPTION;
        return this;
    }

    @Override
    public void showMessage(Component parent, String message, String title, int messageType) {
        System.out.println("    [对话框-消息] " + title + ": " + message);
    }

    @Override
    public String showInput(Component parent, Object message, String title) {
        Object v = queue.isEmpty() ? null : queue.poll();
        System.out.println("    [对话框-输入] " + title + " -> " + v);
        return v != null ? v.toString() : null;
    }

    @Override
    public String showInputChoice(Component parent, Object message, String title,
                                  Object[] options, Object initial) {
        Object v = queue.isEmpty() ? initial : queue.poll();
        System.out.println("    [对话框-选择] " + title + " -> " + v);
        return v != null ? v.toString() : null;
    }

    @Override
    public int showConfirm(Component parent, Object message, String title) {
        System.out.println("    [对话框-确认] " + title + " -> " + (defaultConfirm == JOptionPane.YES_OPTION ? "是" : "否"));
        return defaultConfirm;
    }
}
