package com.mis.team.ui;

import com.mis.team.util.UITheme;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {
    private Color bgColor = UITheme.SIDEBAR_BTN;
    private Color hoverColor = UITheme.SIDEBAR_BTN_HOVER;
    private Color textColor = Color.WHITE;
    private boolean hovered;

    public RoundedButton(String text) {
        super(text);
        setFont(UITheme.FONT_BTN);
        setForeground(textColor);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public void setHoverColor(Color hoverColor) {
        this.hoverColor = hoverColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        setForeground(textColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(hovered ? hoverColor : bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}
