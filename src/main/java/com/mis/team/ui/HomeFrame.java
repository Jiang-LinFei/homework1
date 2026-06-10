package com.mis.team.ui;

import com.mis.team.model.Course;
import com.mis.team.model.Invitation;
import com.mis.team.model.Notification;
import com.mis.team.model.Student;
import com.mis.team.model.StudyGroup;
import com.mis.team.service.TeamService;
import com.mis.team.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;

public class HomeFrame extends JFrame {
    private final Student currentStudent;
    private final TeamService teamService = new TeamService();
    private final DefaultListModel<String> groupModel = new DefaultListModel<>();
    private final DefaultListModel<String> notifyModel = new DefaultListModel<>();
    private List<StudyGroup> myGroups;
    private List<Notification> notifications;
    private List<Invitation> pendingInvitations;
    private JPanel courseBtnPanel;
    private JList<String> groupList;
    private RoundedButton logoutBtn;
    private RoundedButton handleBtn;

    public HomeFrame(Student student) {
        this.currentStudent = student;
        setTitle("学生课程小组作业系统 - 主页");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        buildUI();
        refreshData();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.MAIN_BG);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.SIDEBAR_BG);
        topBar.setBorder(new EmptyBorder(16, 24, 16, 24));
        JLabel welcome = new JLabel("欢迎，" + currentStudent.getName() + "（" + currentStudent.getStudentNo() + "）");
        welcome.setFont(UITheme.FONT_SUBTITLE);
        welcome.setForeground(UITheme.SIDEBAR_TEXT);
        logoutBtn = new RoundedButton("退出登录");
        logoutBtn.setPreferredSize(new Dimension(100, 36));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        topBar.add(welcome, BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);
        root.add(topBar, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(16, 0));
        main.setBackground(UITheme.MAIN_BG);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel leftPanel = createSectionPanel("我的课程小组");
        groupList = new JList<>(groupModel);
        groupList.setFont(UITheme.FONT_BODY);
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.setFixedCellHeight(56);
        groupList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value);
            label.setOpaque(true);
            label.setFont(UITheme.FONT_BODY);
            label.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            if (isSelected) {
                label.setBackground(new java.awt.Color(219, 234, 254));
            } else {
                label.setBackground(UITheme.CARD_BG);
            }
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return label;
        });
        groupList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && groupList.getSelectedIndex() >= 0) {
                openGroup(myGroups.get(groupList.getSelectedIndex()));
            }
        });
        leftPanel.add(new JScrollPane(groupList), BorderLayout.CENTER);

        JPanel coursePanel = createSectionPanel("我的课程");
        courseBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        courseBtnPanel.setOpaque(false);
        coursePanel.add(courseBtnPanel, BorderLayout.CENTER);

        JPanel rightPanel = createSectionPanel("消息通知");
        JList<String> notifyList = new JList<>(notifyModel);
        notifyList.setFont(UITheme.FONT_BODY);
        notifyList.setFixedCellHeight(48);
        rightPanel.add(new JScrollPane(notifyList), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        handleBtn = new RoundedButton("处理邀请/申请");
        handleBtn.addActionListener(e -> showInvitationDialog());
        actionPanel.add(handleBtn);
        rightPanel.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setResizeWeight(0.65);
        split.setBorder(null);
        split.setOpaque(false);

        JPanel centerWrap = new JPanel(new BorderLayout(0, 16));
        centerWrap.setOpaque(false);
        centerWrap.add(coursePanel, BorderLayout.NORTH);
        centerWrap.add(split, BorderLayout.CENTER);

        main.add(centerWrap, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createSectionPanel(String title) {
        RoundedPanel panel = new RoundedPanel(12);
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorderColor(UITheme.BORDER);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SUBTITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        return panel;
    }

    private void refreshData() {
        try {
            myGroups = teamService.getMyGroups(currentStudent.getId());
            groupModel.clear();
            for (StudyGroup g : myGroups) {
                groupModel.addElement(g.getCourseName() + " · " + g.getName() + "（" + g.getMemberCount() + "人）");
            }

            notifications = teamService.getNotifications(currentStudent.getId());
            notifyModel.clear();
            for (Notification n : notifications) {
                String prefix = n.isRead() ? "" : "● ";
                notifyModel.addElement(prefix + n.getTitle() + "：" + n.getContent());
            }

            pendingInvitations = teamService.getPendingInvitations(currentStudent.getId());
            pendingInvitations.addAll(teamService.getPendingJoinRequestsForLeader(currentStudent.getId()));

            courseBtnPanel.removeAll();
            List<Course> courses = teamService.getStudentCourses(currentStudent.getId());
            for (Course c : courses) {
                RoundedButton btn = new RoundedButton(c.getName());
                btn.setPreferredSize(new Dimension(160, 40));
                btn.addActionListener(e -> openCourse(c));
                courseBtnPanel.add(btn);
            }
            courseBtnPanel.revalidate();
            courseBtnPanel.repaint();
        } catch (SQLException ex) {
            DialogGateway.showError(this, "加载数据失败：" + ex.getMessage(), "错误");
        }
    }

    private void openCourse(Course course) {
        dispose();
        new CourseFrame(currentStudent, course).setVisible(true);
    }

    private void openGroup(StudyGroup group) {
        dispose();
        new GroupFrame(currentStudent, group).setVisible(true);
    }

    private void showInvitationDialog() {
        try {
            List<Invitation> invites = new java.util.ArrayList<>();
            java.util.Set<Integer> seen = new java.util.HashSet<>();
            for (Invitation inv : teamService.getPendingInvitations(currentStudent.getId())) {
                if (seen.add(inv.getId())) {
                    invites.add(inv);
                }
            }
            for (Invitation inv : teamService.getPendingJoinRequestsForLeader(currentStudent.getId())) {
                if (seen.add(inv.getId())) {
                    invites.add(inv);
                }
            }
            if (invites.isEmpty()) {
                DialogGateway.showInfo(this, "暂无待处理的邀请或申请", "提示");
                return;
            }
            String[] options = invites.stream().map(this::formatInvitation).toArray(String[]::new);
            String selected = DialogGateway.showInputChoice(this,
                    "选择要处理的邀请/申请：", "消息处理", options, options[0]);
            if (selected == null) {
                return;
            }
            int index = java.util.Arrays.asList(options).indexOf(selected);
            Invitation inv = invites.get(index);
            int choice = DialogGateway.showConfirm(this,
                    formatInvitation(inv) + "\n是否同意？",
                    "处理邀请");
            if (choice == javax.swing.JOptionPane.YES_OPTION) {
                teamService.acceptInvitation(inv.getId(), currentStudent.getId());
            } else if (choice == javax.swing.JOptionPane.NO_OPTION) {
                teamService.rejectInvitation(inv.getId(), currentStudent.getId());
            }
            refreshData();
        } catch (Exception ex) {
            DialogGateway.showError(this, ex.getMessage(), "处理失败");
        }
    }

    public int getGroupListSize() {
        return groupModel.size();
    }

    public int getNotifyListSize() {
        return notifyModel.size();
    }

    public void clickCourseButton(String courseName) {
        for (Component c : courseBtnPanel.getComponents()) {
            if (c instanceof RoundedButton btn && courseName.equals(btn.getText())) {
                btn.doClick();
                return;
            }
        }
        throw new IllegalStateException("找不到课程按钮: " + courseName);
    }

    public void openGroupAt(int index) {
        groupList.setSelectedIndex(index);
    }

    public void clickLogout() {
        logoutBtn.doClick();
    }

    public void clickHandleInvitations() {
        handleBtn.doClick();
    }

    private String formatInvitation(Invitation inv) {
        if ("invite".equals(inv.getType())) {
            return inv.getInviterName() + " 邀请你加入「" + inv.getGroupName() + "」";
        }
        // join_request: inviter=申请人, invitee=组长
        return inv.getInviterName() + " 申请加入「" + inv.getGroupName() + "」";
    }
}
