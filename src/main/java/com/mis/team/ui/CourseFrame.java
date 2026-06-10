package com.mis.team.ui;

import com.mis.team.model.Course;
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
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class CourseFrame extends JFrame {
    private final Student currentStudent;
    private final Course course;
    private final TeamService teamService = new TeamService();
    private final DefaultListModel<String> myGroupModel = new DefaultListModel<>();
    private final DefaultListModel<String> allGroupModel = new DefaultListModel<>();
    private List<StudyGroup> myGroups;
    private List<StudyGroup> allGroups;
    private JList<String> myGroupList;
    private RoundedButton createBtn;
    private RoundedButton joinBtn;
    private RoundedButton backBtn;

    public CourseFrame(Student student, Course course) {
        this.currentStudent = student;
        this.course = course;
        setTitle(course.getName() + " - 课程小组");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
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
        JLabel title = new JLabel(course.getName() + " · 任课教师：" + course.getTeacher());
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.SIDEBAR_TEXT);
        backBtn = new RoundedButton("返回主页");
        backBtn.setPreferredSize(new Dimension(100, 36));
        backBtn.addActionListener(e -> goHome());
        topBar.add(title, BorderLayout.WEST);
        topBar.add(backBtn, BorderLayout.EAST);
        root.add(topBar, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(16, 16));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionPanel.setOpaque(false);
        createBtn = new RoundedButton("创建小组");
        createBtn.addActionListener(e -> createGroup());
        joinBtn = new RoundedButton("加入小组");
        joinBtn.addActionListener(e -> joinGroup());
        actionPanel.add(createBtn);
        actionPanel.add(joinBtn);

        JPanel myPanel = createCard("我已加入的小组");
        myGroupList = new JList<>(myGroupModel);
        myGroupList.setFont(UITheme.FONT_BODY);
        myGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myGroupList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && myGroupList.getSelectedIndex() >= 0) {
                openGroup(myGroups.get(myGroupList.getSelectedIndex()));
            }
        });
        myPanel.add(new JScrollPane(myGroupList), BorderLayout.CENTER);

        JPanel allPanel = createCard("课程全部小组");
        JList<String> allList = new JList<>(allGroupModel);
        allList.setFont(UITheme.FONT_BODY);
        allPanel.add(new JScrollPane(allList), BorderLayout.CENTER);

        main.add(actionPanel, BorderLayout.NORTH);
        main.add(myPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(allPanel, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);
        allPanel.setPreferredSize(new Dimension(0, 180));

        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createCard(String title) {
        RoundedPanel panel = new RoundedPanel(12);
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorderColor(UITheme.BORDER);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JLabel label = new JLabel(title);
        label.setFont(UITheme.FONT_SUBTITLE);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private void refreshData() {
        try {
            myGroups = teamService.getMyGroupsInCourse(currentStudent.getId(), course.getId());
            allGroups = teamService.getCourseGroups(course.getId());
            myGroupModel.clear();
            allGroupModel.clear();
            for (StudyGroup g : myGroups) {
                myGroupModel.addElement(g.getName() + "（组长：" + g.getLeaderName() + "，" + g.getMemberCount() + "人）");
            }
            for (StudyGroup g : allGroups) {
                allGroupModel.addElement(g.getName() + "（组长：" + g.getLeaderName() + "，" + g.getMemberCount() + "人）");
            }
        } catch (SQLException ex) {
            DialogGateway.showError(this, "加载失败：" + ex.getMessage(), "错误");
        }
    }

    private void createGroup() {
        String groupName = DialogGateway.showInput(this, "请输入小组名称：", "创建小组");
        if (groupName == null || groupName.trim().isEmpty()) {
            return;
        }
        String members = DialogGateway.showInput(this,
                "请输入组员学号（多个用逗号分隔）：\n例如：2021002,2021003",
                "邀请组员");
        if (members == null) {
            return;
        }
        try {
            List<String> memberNos = Arrays.stream(members.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            int groupId = teamService.createGroup(course.getId(), groupName.trim(), currentStudent.getId(), memberNos);
            DialogGateway.showInfo(this, "小组创建成功，已发送邀请！", "成功");
            refreshData();
        } catch (Exception ex) {
            DialogGateway.showError(this, ex.getMessage(), "创建失败");
        }
    }

    private void joinGroup() {
        if (allGroups == null || allGroups.isEmpty()) {
            DialogGateway.showInfo(this, "暂无可加入的小组", "提示");
            return;
        }
        List<StudyGroup> joinable = allGroups.stream()
                .filter(g -> myGroups.stream().noneMatch(m -> m.getId() == g.getId()))
                .toList();
        if (joinable.isEmpty()) {
            DialogGateway.showInfo(this, "你已在全部小组中，无需重复申请", "提示");
            return;
        }
        String[] options = joinable.stream()
                .map(g -> g.getName() + "（组长：" + g.getLeaderName() + "）")
                .toArray(String[]::new);
        String selected = DialogGateway.showInputChoice(this,
                "选择要申请加入的小组：", "加入小组", options, options[0]);
        if (selected == null) {
            return;
        }
        int index = java.util.Arrays.asList(options).indexOf(selected);
        StudyGroup group = joinable.get(index);
        try {
            teamService.requestJoinGroup(group.getId(), currentStudent.getId(), group);
            DialogGateway.showInfo(this, "申请已发送，请等待组长审核", "成功");
        } catch (Exception ex) {
            DialogGateway.showError(this, ex.getMessage(), "申请失败");
        }
    }

    public int getMyGroupListSize() {
        return myGroupModel.size();
    }

    public int getAllGroupListSize() {
        return allGroupModel.size();
    }

    public void clickCreateGroup() {
        createBtn.doClick();
    }

    public void clickJoinGroup() {
        joinBtn.doClick();
    }

    public void openMyGroupAt(int index) {
        myGroupList.setSelectedIndex(index);
    }

    public void clickBackHome() {
        backBtn.doClick();
    }

    public String getJoinableOptionLabel(int index) {
        List<StudyGroup> joinable = allGroups.stream()
                .filter(g -> myGroups.stream().noneMatch(m -> m.getId() == g.getId()))
                .toList();
        if (index < 0 || index >= joinable.size()) {
            return null;
        }
        StudyGroup g = joinable.get(index);
        return g.getName() + "（组长：" + g.getLeaderName() + "）";
    }

    private void openGroup(StudyGroup group) {
        dispose();
        new GroupFrame(currentStudent, group).setVisible(true);
    }

    private void goHome() {
        dispose();
        new HomeFrame(currentStudent).setVisible(true);
    }
}
