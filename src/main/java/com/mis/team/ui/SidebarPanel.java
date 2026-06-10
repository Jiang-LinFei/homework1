package com.mis.team.ui;

import com.mis.team.util.UITheme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SidebarPanel extends JPanel {
    private final Map<String, RoundedButton> buttons = new LinkedHashMap<>();
    private Consumer<String> onNavigate;

    public SidebarPanel(String title, String subtitle) {
        setLayout(new BorderLayout());
        setBackground(UITheme.SIDEBAR_BG);
        setPreferredSize(new Dimension(200, 0));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 16, 24, 16));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SUBTITLE);
        titleLabel.setForeground(UITheme.SIDEBAR_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(UITheme.FONT_SMALL);
        subLabel.setForeground(new java.awt.Color(180, 200, 230));
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(6));
        header.add(subLabel);

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12));

        JLabel onlineLabel = new JLabel("当前在线：4人");
        onlineLabel.setFont(UITheme.FONT_SMALL);
        onlineLabel.setForeground(new java.awt.Color(180, 200, 230));
        onlineLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(header, BorderLayout.NORTH);
        add(navPanel, BorderLayout.CENTER);
        add(onlineLabel, BorderLayout.SOUTH);

        putClientProperty("navPanel", navPanel);
    }

    public void addNavItem(String key, String label) {
        JPanel navPanel = (JPanel) getClientProperty("navPanel");
        RoundedButton btn = new RoundedButton(label);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> {
            if (onNavigate != null) {
                onNavigate.accept(key);
            }
            highlight(key);
        });
        buttons.put(key, btn);
        navPanel.add(btn);
        navPanel.add(Box.createVerticalStrut(10));
    }

    public void setOnNavigate(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
    }

    public void highlight(String key) {
        buttons.forEach((k, btn) -> {
            if (k.equals(key)) {
                btn.setBgColor(UITheme.SIDEBAR_BTN_HOVER);
            } else {
                btn.setBgColor(UITheme.SIDEBAR_BTN);
            }
            btn.repaint();
        });
    }

    /** 逐步验收：模拟点击左侧导航 */
    public void clickNav(String key) {
        RoundedButton btn = buttons.get(key);
        if (btn != null) {
            btn.doClick();
        }
    }
}
