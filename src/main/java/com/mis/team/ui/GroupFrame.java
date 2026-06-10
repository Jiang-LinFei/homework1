package com.mis.team.ui;

import com.mis.team.model.ChatMessage;
import com.mis.team.model.GroupFile;
import com.mis.team.model.GroupTask;
import com.mis.team.model.Student;
import com.mis.team.model.StudyGroup;
import com.mis.team.service.TeamService;
import com.mis.team.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class GroupFrame extends JFrame {
    private final Student currentStudent;
    private final StudyGroup group;
    private final TeamService teamService = new TeamService();

    private final JPanel contentPanel = new JPanel(new BorderLayout());
    private final JPanel chatMessagesPanel = new JPanel();
    private final JTextField messageField = new JTextField();
    private final DefaultTableModel taskModel = new DefaultTableModel(
            new String[]{"任务", "负责人", "状态", "描述"}, 0);
    private final DefaultTableModel fileModel = new DefaultTableModel(
            new String[]{"文件名", "上传者", "时间"}, 0);
    private JPanel membersListPanel;
    private JLabel pageTitle;
    private SidebarPanel sidebar;
    private JTable taskTable;

    public GroupFrame(Student student, StudyGroup group) {
        this.currentStudent = student;
        this.group = group;
        setTitle(group.getCourseName() + " - " + group.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        buildUI();
        showChat();
        refreshChat();
        refreshRightPanel();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.MAIN_BG);

        sidebar = new SidebarPanel(group.getCourseName(), group.getName());
        sidebar.addNavItem("chat", "讨论区");
        sidebar.addNavItem("files", "文件管理");
        sidebar.addNavItem("tasks", "任务管理");
        sidebar.addNavItem("upload", "上传文件");
        sidebar.addNavItem("submit", "提交作业");
        sidebar.addNavItem("home", "返回主页");
        sidebar.setOnNavigate(this::navigate);
        sidebar.highlight("chat");

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 16, 20, 8));

        pageTitle = new JLabel(group.getCourseName() + " - " + group.getName() + "讨论区");
        pageTitle.setFont(UITheme.FONT_TITLE);
        pageTitle.setForeground(UITheme.TEXT_PRIMARY);
        pageTitle.setBorder(new EmptyBorder(0, 8, 16, 0));

        contentPanel.setOpaque(false);
        center.add(pageTitle, BorderLayout.NORTH);
        center.add(contentPanel, BorderLayout.CENTER);

        JPanel rightPanel = buildRightPanel();

        root.add(sidebar, BorderLayout.WEST);
        root.add(center, BorderLayout.CENTER);
        root.add(rightPanel, BorderLayout.EAST);
        setContentPane(root);
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.RIGHT_PANEL_BG);
        panel.setPreferredSize(new Dimension(240, 0));
        panel.setBorder(new EmptyBorder(20, 12, 20, 16));

        JPanel membersCard = createInfoCard("小组成员");
        JPanel membersList = new JPanel();
        membersList.setLayout(new BoxLayout(membersList, BoxLayout.Y_AXIS));
        membersList.setOpaque(false);
        membersCard.add(membersList, BorderLayout.CENTER);
        this.membersListPanel = membersList;

        JPanel announceCard = createInfoCard("课程公告");
        JPanel announceList = new JPanel();
        announceList.setLayout(new BoxLayout(announceList, BoxLayout.Y_AXIS));
        announceList.setOpaque(false);
        try {
            List<com.mis.team.model.Notification> notes = teamService.getNotifications(currentStudent.getId());
            for (com.mis.team.model.Notification n : notes) {
                if ("课程公告".equals(n.getTitle())) {
                    announceList.add(createBullet(n.getContent()));
                }
            }
        } catch (SQLException ignored) {
        }
        if (announceList.getComponentCount() == 0) {
            announceList.add(createBullet("周五提交需求分析"));
            announceList.add(createBullet("下周课堂展示"));
            announceList.add(createBullet("注意命名规范"));
        }
        announceCard.add(announceList, BorderLayout.CENTER);

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        wrap.add(membersCard);
        wrap.add(Box.createVerticalStrut(16));
        wrap.add(announceCard);
        panel.add(wrap, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createInfoCard(String title) {
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(UITheme.CARD_BG);
        card.setBorderColor(UITheme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SUBTITLE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(titleLabel, BorderLayout.NORTH);
        return card;
    }

    private JLabel createBullet(String text) {
        JLabel label = new JLabel("• " + text);
        label.setFont(UITheme.FONT_BODY);
        label.setForeground(UITheme.TEXT_SECONDARY);
        label.setBorder(new EmptyBorder(4, 0, 4, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createMemberChip(String name, boolean leader) {
        RoundedPanel chip = new RoundedPanel(10);
        chip.setBackground(new Color(241, 245, 249));
        chip.setLayout(new BorderLayout());
        chip.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        chip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        chip.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(leader ? name + "（组长）" : name);
        label.setFont(UITheme.FONT_BODY);
        chip.add(label, BorderLayout.CENTER);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(chip, BorderLayout.CENTER);
        wrap.setBorder(new EmptyBorder(0, 0, 8, 0));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        return wrap;
    }

    private void refreshRightPanel() {
        if (membersListPanel == null) {
            return;
        }
        JPanel membersList = membersListPanel;
        membersList.removeAll();
        try {
            List<Student> members = teamService.getGroupMembers(group.getId());
            Map<Integer, String> roles = teamService.getMemberRoles(group.getId());
            for (Student s : members) {
                boolean leader = "leader".equals(roles.get(s.getId()));
                membersList.add(createMemberChip(s.getName(), leader));
            }
        } catch (SQLException ex) {
            membersList.add(createBullet("加载成员失败"));
        }
        membersList.revalidate();
        membersList.repaint();
    }

    private void navigate(String key) {
        if ("home".equals(key)) {
            goHome();
            return;
        }
        sidebar.highlight(key);
        switch (key) {
            case "chat" -> {
                setPageTitle("讨论区");
                showChat();
            }
            case "files" -> {
                setPageTitle("文件管理");
                showFiles();
            }
            case "tasks" -> {
                setPageTitle("任务管理");
                showTasks();
            }
            case "upload" -> {
                setPageTitle("上传文件");
                showUpload();
            }
            case "submit" -> {
                setPageTitle("提交作业");
                showSubmit();
            }
            default -> {
            }
        }
    }

    private void setPageTitle(String section) {
        pageTitle.setText(group.getCourseName() + " - " + group.getName() + section);
    }

    private void showChat() {
        contentPanel.removeAll();
        RoundedPanel chatCard = new RoundedPanel(16);
        chatCard.setBackground(UITheme.CARD_BG);
        chatCard.setBorderColor(UITheme.BORDER);
        chatCard.setLayout(new BorderLayout());
        chatCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        chatMessagesPanel.setLayout(new BoxLayout(chatMessagesPanel, BoxLayout.Y_AXIS));
        chatMessagesPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(chatMessagesPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new EmptyBorder(12, 0, 0, 0));
        messageField.setFont(UITheme.FONT_BODY);
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        RoundedButton sendBtn = new RoundedButton("发送");
        sendBtn.setPreferredSize(new Dimension(80, 42));
        sendBtn.addActionListener(e -> sendMessage());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        chatCard.add(scroll, BorderLayout.CENTER);
        chatCard.add(inputPanel, BorderLayout.SOUTH);
        contentPanel.add(chatCard, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
        refreshChat();
    }

    private void refreshChat() {
        chatMessagesPanel.removeAll();
        try {
            List<ChatMessage> messages = teamService.getMessages(group.getId(), currentStudent.getId());
            for (ChatMessage msg : messages) {
                chatMessagesPanel.add(createMessageBubble(msg));
                chatMessagesPanel.add(Box.createVerticalStrut(12));
            }
            SwingUtilities.invokeLater(() -> {
                if (contentPanel.getComponentCount() > 0
                        && contentPanel.getComponent(0) instanceof RoundedPanel chatCard
                        && chatCard.getComponentCount() > 0
                        && chatCard.getComponent(0) instanceof JScrollPane scroll) {
                    scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
                }
            });
        } catch (SQLException ex) {
            chatMessagesPanel.add(new JLabel("加载消息失败：" + ex.getMessage()));
        }
        chatMessagesPanel.revalidate();
        chatMessagesPanel.repaint();
    }

    private JPanel createMessageBubble(ChatMessage msg) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        if (msg.isOwn()) {
            RoundedPanel bubble = new RoundedPanel(14);
            bubble.setBackground(UITheme.CHAT_BUBBLE_OUT);
            bubble.setLayout(new BorderLayout());
            bubble.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            JLabel text = new JLabel("<html><body style='width:280px;color:white'>" + escapeHtml(msg.getContent()) + "</body></html>");
            text.setFont(UITheme.FONT_BODY);
            bubble.add(text, BorderLayout.CENTER);
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            right.setOpaque(false);
            right.add(bubble);
            row.add(right, BorderLayout.EAST);
        } else {
            JPanel left = new JPanel();
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.setOpaque(false);
            JLabel name = new JLabel(msg.getSenderName());
            name.setFont(UITheme.FONT_SMALL);
            name.setForeground(UITheme.TEXT_SECONDARY);
            name.setAlignmentX(Component.LEFT_ALIGNMENT);
            RoundedPanel bubble = new RoundedPanel(14);
            bubble.setBackground(UITheme.CHAT_BUBBLE_IN_BG);
            bubble.setLayout(new BorderLayout());
            bubble.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            JLabel text = new JLabel("<html><body style='width:280px'>" + escapeHtml(msg.getContent()) + "</body></html>");
            text.setFont(UITheme.FONT_BODY);
            bubble.add(text, BorderLayout.CENTER);
            bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
            left.add(name);
            left.add(Box.createVerticalStrut(4));
            left.add(bubble);
            row.add(left, BorderLayout.WEST);
        }
        return row;
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void sendMessage() {
        String content = messageField.getText().trim();
        if (content.isEmpty()) {
            return;
        }
        try {
            if (!teamService.sendMessage(group.getId(), currentStudent.getId(), content)) {
                return;
            }
            messageField.setText("");
            refreshChat();
        } catch (SQLException | IllegalStateException ex) {
            DialogGateway.showError(this, "发送失败：" + ex.getMessage(), "错误");
        }
    }

    private void showFiles() {
        contentPanel.removeAll();
        contentPanel.add(wrapTable("小组文件", fileModel, null), BorderLayout.CENTER);
        refreshFiles();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showTasks() {
        contentPanel.removeAll();
        JPanel panel = wrapTable("小组任务进度", taskModel, e -> updateTaskStatus());
        contentPanel.add(panel, BorderLayout.CENTER);
        refreshTasks();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel wrapTable(String title, DefaultTableModel model, java.awt.event.ActionListener action) {
        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(UITheme.CARD_BG);
        card.setBorderColor(UITheme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel label = new JLabel(title);
        label.setFont(UITheme.FONT_SUBTITLE);
        label.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(label, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(36);
        if (model == taskModel) {
            taskTable = table;
        }
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        if (action != null) {
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnPanel.setOpaque(false);
            RoundedButton btn = new RoundedButton("更新选中任务状态");
            btn.addActionListener(action);
            btnPanel.add(btn);
            card.add(btnPanel, BorderLayout.SOUTH);
        }
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.CENTER);
        wrap.putClientProperty("tableModel", model);
        return wrap;
    }

    private void refreshTasks() {
        taskModel.setRowCount(0);
        try {
            for (GroupTask t : teamService.getTasks(group.getId())) {
                taskModel.addRow(new Object[]{
                        t.getTitle(),
                        t.getAssigneeName() != null ? t.getAssigneeName() : "未分配",
                        t.getStatus(),
                        t.getDescription() != null ? t.getDescription() : ""
                });
            }
        } catch (SQLException ex) {
            DialogGateway.showError(this, "加载任务失败", "错误");
        }
    }

    private void updateTaskStatus() {
        try {
            List<GroupTask> tasks = teamService.getTasks(group.getId());
            if (tasks.isEmpty()) {
                DialogGateway.showInfo(this, "暂无任务", "提示");
                return;
            }
            int index = taskTable != null ? taskTable.getSelectedRow() : -1;
            if (index < 0) {
                DialogGateway.showWarning(this, "请先在表格中选中一条任务", "提示");
                return;
            }
            String[] statuses = {"待开始", "进行中", "已完成"};
            String status = DialogGateway.showInputChoice(this, "选择新状态：", "更新任务",
                    statuses, tasks.get(index).getStatus());
            if (status != null) {
                teamService.updateTaskStatus(tasks.get(index).getId(), status);
                refreshTasks();
            }
        } catch (Exception ex) {
            DialogGateway.showError(this, ex.getMessage(), "更新失败");
        }
    }

    private void refreshFiles() {
        fileModel.setRowCount(0);
        try {
            for (GroupFile f : teamService.getFiles(group.getId())) {
                fileModel.addRow(new Object[]{f.getFileName(), f.getUploaderName(), f.getUploadedAt()});
            }
        } catch (SQLException ex) {
            DialogGateway.showError(this, "加载文件失败", "错误");
        }
    }

    private void showUpload() {
        contentPanel.removeAll();
        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(UITheme.CARD_BG);
        card.setBorderColor(UITheme.BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel label = new JLabel("选择文件上传到小组共享区");
        label.setFont(UITheme.FONT_SUBTITLE);
        RoundedButton uploadBtn = new RoundedButton("选择并上传文件");
        uploadBtn.setPreferredSize(new Dimension(180, 42));
        uploadBtn.addActionListener(e -> doUpload());
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(label);
        center.add(Box.createVerticalStrut(20));
        center.add(uploadBtn);
        card.add(center, BorderLayout.CENTER);
        contentPanel.add(card, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void doUpload() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                teamService.uploadFile(group.getId(), currentStudent.getId(), chooser.getSelectedFile().toPath());
                int choice = DialogGateway.showConfirm(this, "上传成功！是否查看文件列表？",
                        "成功");
                if (choice == javax.swing.JOptionPane.YES_OPTION) {
                    navigate("files");
                }
            } catch (Exception ex) {
                DialogGateway.showError(this, "上传失败：" + ex.getMessage(), "错误");
            }
        }
    }

    private void showSubmit() {
        contentPanel.removeAll();
        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(UITheme.CARD_BG);
        card.setBorderColor(UITheme.BORDER);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel label = new JLabel("提交作业");
        label.setFont(UITheme.FONT_SUBTITLE);
        JTextField titleField = new JTextField();
        titleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        JTextArea descArea = new JTextArea(4, 30);
        descArea.setLineWrap(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        RoundedButton submitBtn = new RoundedButton("提交作业文件");
        submitBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    teamService.uploadFile(group.getId(), currentStudent.getId(), chooser.getSelectedFile().toPath());
                    String title = titleField.getText().trim();
                    if (!title.isEmpty()) {
                        teamService.addTask(group.getId(), currentStudent.getId(), "作业：" + title,
                                descArea.getText().trim(), "已提交", currentStudent.getId());
                    }
                    DialogGateway.showInfo(this, "作业提交成功！", "成功");
                } catch (Exception ex) {
                    DialogGateway.showError(this, ex.getMessage(), "提交失败");
                }
            }
        });

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(new JLabel("作业标题"));
        form.add(Box.createVerticalStrut(6));
        form.add(titleField);
        form.add(Box.createVerticalStrut(12));
        form.add(new JLabel("说明"));
        form.add(Box.createVerticalStrut(6));
        form.add(descArea);
        form.add(Box.createVerticalStrut(20));
        form.add(submitBtn);

        card.add(label, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        contentPanel.add(card, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void goHome() {
        dispose();
        new HomeFrame(currentStudent).setVisible(true);
    }

    // ── 逐步验收辅助 ──

    public void clickNav(String key) {
        sidebar.clickNav(key);
    }

    public String getPageTitleText() {
        return pageTitle.getText();
    }

    public int countChatItems() {
        return chatMessagesPanel.getComponentCount();
    }

    public boolean chatContainsText(String text) {
        return findTextInContainer(chatMessagesPanel, text);
    }

    private boolean findTextInContainer(java.awt.Container container, String text) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel label) {
                String t = label.getText();
                if (t != null && t.contains(text)) {
                    return true;
                }
            }
            if (c instanceof java.awt.Container child) {
                if (findTextInContainer(child, text)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void sendChatMessageForTest(String text) {
        messageField.setText(text);
        sendMessage();
    }

    public void trySendEmptyMessage() {
        messageField.setText("");
        sendMessage();
    }

    public int getTaskRowCount() {
        return taskModel.getRowCount();
    }

    public int getFileRowCount() {
        return fileModel.getRowCount();
    }

    public void selectTaskRow(int row) {
        if (taskTable != null) {
            taskTable.setRowSelectionInterval(row, row);
        }
    }

    public void clickUpdateTaskStatus() {
        updateTaskStatus();
    }

    public void uploadFileForTest(Path file) {
        try {
            teamService.uploadFile(group.getId(), currentStudent.getId(), file);
            int choice = DialogGateway.showConfirm(this, "上传成功！是否查看文件列表？", "成功");
            if (choice == javax.swing.JOptionPane.YES_OPTION) {
                navigate("files");
            }
        } catch (Exception ex) {
            DialogGateway.showError(this, "上传失败：" + ex.getMessage(), "错误");
        }
    }

    public void submitHomeworkForTest(String title, String desc, Path file) {
        try {
            teamService.uploadFile(group.getId(), currentStudent.getId(), file);
            if (title != null && !title.isBlank()) {
                teamService.addTask(group.getId(), currentStudent.getId(), "作业：" + title,
                        desc != null ? desc : "", "已提交", currentStudent.getId());
            }
            DialogGateway.showInfo(this, "作业提交成功！", "成功");
        } catch (Exception ex) {
            DialogGateway.showError(this, ex.getMessage(), "提交失败");
        }
    }

    public int countMemberChips() {
        return membersListPanel != null ? membersListPanel.getComponentCount() : 0;
    }
}
