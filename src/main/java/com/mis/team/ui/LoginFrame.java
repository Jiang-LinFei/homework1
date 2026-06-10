package com.mis.team.ui;

import com.mis.team.model.Student;
import com.mis.team.service.TeamService;
import com.mis.team.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private final TeamService teamService = new TeamService();
    private final JTextField studentNoField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    public LoginFrame() {
        setTitle("学生课程小组作业系统 - 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(560, 580));
        setSize(640, 620);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.MAIN_BG);

        JPanel header = new JPanel();
        header.setBackground(UITheme.SIDEBAR_BG);
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        header.setPreferredSize(new Dimension(0, 72));
        JLabel title = new JLabel("学生课程小组作业系统");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.SIDEBAR_TEXT);
        header.add(title);
        root.add(header, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(UITheme.CARD_BG);
        card.setBorderColor(UITheme.BORDER);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(36, 48, 36, 48));
        card.setPreferredSize(new Dimension(480, 420));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel hint = new JLabel("请使用学号和密码登录");
        hint.setFont(UITheme.FONT_BODY);
        hint.setForeground(UITheme.TEXT_SECONDARY);
        card.add(hint, gbc);

        gbc.gridy++;
        JLabel noLabel = new JLabel("学号");
        noLabel.setFont(UITheme.FONT_BODY);
        card.add(noLabel, gbc);

        gbc.gridy++;
        styleField(studentNoField);
        studentNoField.setPreferredSize(new Dimension(0, 44));
        studentNoField.setText("2021001");
        card.add(studentNoField, gbc);

        gbc.gridy++;
        JLabel pwdLabel = new JLabel("密码");
        pwdLabel.setFont(UITheme.FONT_BODY);
        card.add(pwdLabel, gbc);

        gbc.gridy++;
        styleField(passwordField);
        passwordField.setPreferredSize(new Dimension(0, 44));
        passwordField.setText("123456");
        card.add(passwordField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(24, 0, 4, 0);
        RoundedButton loginBtn = new RoundedButton("登 录");
        loginBtn.setPreferredSize(new Dimension(0, 48));
        loginBtn.addActionListener(e -> doLogin());
        card.add(loginBtn, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(12, 0, 0, 0);
        JLabel demo = new JLabel("<html>测试账号：2021001~2021004，密码均为 123456</html>");
        demo.setFont(UITheme.FONT_SMALL);
        demo.setForeground(UITheme.TEXT_SECONDARY);
        card.add(demo, gbc);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.MAIN_BG);
        center.setBorder(BorderFactory.createEmptyBorder(32, 48, 40, 48));
        center.add(card, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        setContentPane(root);

        getRootPane().setDefaultButton(loginBtn);
    }

    private void styleField(javax.swing.JComponent field) {
        field.setFont(UITheme.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
    }

    /** 逐步验收：填写并触发登录 */
    public void fillAndLogin(String studentNo, String password) {
        studentNoField.setText(studentNo);
        passwordField.setText(password);
        doLogin();
    }

    private void doLogin() {
        String studentNo = studentNoField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (studentNo.isEmpty() || password.isEmpty()) {
            DialogGateway.showWarning(this, "请输入学号和密码", "提示");
            return;
        }
        try {
            Student student = teamService.login(studentNo, password)
                    .orElse(null);
            if (student == null) {
                DialogGateway.showError(this, "学号或密码错误", "登录失败");
                return;
            }
            dispose();
            new HomeFrame(student).setVisible(true);
        } catch (SQLException ex) {
            DialogGateway.showError(this, "数据库连接失败：" + ex.getMessage(), "错误");
        }
    }
}
