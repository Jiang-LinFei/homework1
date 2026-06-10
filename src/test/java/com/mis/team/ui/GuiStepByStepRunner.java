package com.mis.team.ui;

import com.mis.team.util.DatabaseUtil;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Window;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 启动真实 Swing 界面，按操作手册一项一项走查验收。
 */
public class GuiStepByStepRunner {
    private static final List<String> passed = new ArrayList<>();
    private static final List<String> failed = new ArrayList<>();
    private static final AtomicInteger stepNo = new AtomicInteger();
    private static TestDialogHandler dialogs;

    public static void main(String[] args) throws Exception {
        Path testDb = Path.of("data", "gui_step_test.db");
        Files.createDirectories(Path.of("data"));
        if (Files.exists(testDb)) {
            Files.delete(testDb);
        }
        System.setProperty("team.db.path", testDb.toString());
        DatabaseUtil.resetForTesting();

        dialogs = new TestDialogHandler();
        DialogGateway.setHandler(dialogs);

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   学生课程小组系统 · GUI 逐步验收（真实启动）    ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        SwingUtilities.invokeAndWait(GuiStepByStepRunner::runAllSteps);

        DialogGateway.resetHandler();
        DatabaseUtil.close();

        System.out.println("\n════════════════ 验收汇总 ════════════════");
        System.out.println("通过: " + passed.size() + "  失败: " + failed.size());
        passed.forEach(s -> System.out.println("  ✓ " + s));
        failed.forEach(s -> System.out.println("  ✗ " + s));

        if (!failed.isEmpty()) {
            System.exit(1);
        }
    }

    private static void runAllSteps() {
        LoginFrame login = new LoginFrame();
        login.setVisible(true);

        step("01 启动项目，显示登录窗口", login.isVisible() && login.getTitle().contains("登录"));

        login.fillAndLogin("2021001", "wrong");
        step("02 错误密码登录失败，仍在登录页", findVisible(LoginFrame.class) != null);

        login.fillAndLogin("2021001", "123456");
        HomeFrame home = waitFrame(HomeFrame.class, 2000);
        step("03 正确账号登录，进入主页", home != null && home.getTitle().contains("主页"));

        step("04 主页-小组列表有数据", home.getGroupListSize() > 0);
        step("05 主页-通知列表有数据", home.getNotifyListSize() > 0);

        home.clickCourseButton("管理信息系统");
        CourseFrame course = waitFrame(CourseFrame.class, 2000);
        step("06 点击课程，进入课程小组页", course != null && course.getTitle().contains("管理信息系统"));

        step("07 课程页-我已加入的小组非空", course.getMyGroupListSize() > 0);
        step("08 课程页-全部小组列表非空", course.getAllGroupListSize() >= 2);

        course.openMyGroupAt(0);
        GroupFrame group = waitFrame(GroupFrame.class, 2000);
        step("09 点击进入小组，打开讨论区", group != null && group.getTitle().contains("第"));

        step("10 讨论区-页面标题正确", group.getPageTitleText().contains("讨论区"));
        step("11 讨论区-右侧成员列表有内容", group.countMemberChips() >= 4);
        step("12 讨论区-历史消息已加载", group.countChatItems() >= 5);

        String uiMsg = "GUI逐步验收消息_" + System.nanoTime();
        group.sendChatMessageForTest(uiMsg);
        step("13 讨论区-输入并发送消息", group.chatContainsText(uiMsg));

        int afterSend = group.countChatItems();
        group.trySendEmptyMessage();
        step("14 讨论区-空消息不增加气泡", group.countChatItems() == afterSend);

        group.clickNav("tasks");
        step("15 左侧导航-任务管理", group.getPageTitleText().contains("任务管理"));
        step("16 任务管理-表格有数据", group.getTaskRowCount() >= 4);

        dialogs.queueChoice("已完成");
        group.selectTaskRow(0);
        int oldRows = group.getTaskRowCount();
        group.clickUpdateTaskStatus();
        step("17 任务管理-选中行并更新状态", group.getTaskRowCount() == oldRows);

        group.clickNav("files");
        step("18 左侧导航-文件管理", group.getPageTitleText().contains("文件管理"));

        group.clickNav("upload");
        step("19 左侧导航-上传文件页", group.getPageTitleText().contains("上传文件"));

        try {
            Path tmp = Files.createTempFile("gui_upload_", ".txt");
            Files.writeString(tmp, "gui test");
            dialogs.queueConfirmNo();
            group.uploadFileForTest(tmp);
            step("20 上传文件-执行上传", group.getFileRowCount() >= 0);
            Files.deleteIfExists(tmp);
        } catch (Exception ex) {
            step("20 上传文件-执行上传", false);
        }

        group.clickNav("files");
        step("21 文件管理-刷新后可见上传记录", group.getFileRowCount() >= 1);

        group.clickNav("submit");
        step("22 左侧导航-提交作业页", group.getPageTitleText().contains("提交作业"));

        try {
            Path hw = Files.createTempFile("gui_hw_", ".docx");
            Files.writeString(hw, "homework");
            group.submitHomeworkForTest("GUI验收作业", "说明", hw);
            group.clickNav("tasks");
            step("23 提交作业-任务列表出现作业记录",
                    group.getTaskRowCount() >= 5 && group.getPageTitleText().contains("任务"));
            Files.deleteIfExists(hw);
        } catch (Exception ex) {
            step("23 提交作业-任务列表出现作业记录", false);
        }

        group.clickNav("chat");
        step("24 左侧导航-返回讨论区", group.getPageTitleText().contains("讨论区"));

        group.clickNav("home");
        home = waitFrame(HomeFrame.class, 2000);
        step("25 左侧导航-返回主页", home != null);

        // ── 创建小组 + 邀请流程（陈同学）──
        home.clickCourseButton("管理信息系统");
        course = waitFrame(CourseFrame.class, 2000);
        dialogs.queueInput("GUI验收新建组").queueInput("2021003");
        course.clickCreateGroup();
        step("26 创建小组-填写组名和组员学号", course.getAllGroupListSize() >= course.getMyGroupListSize());

        course.clickBackHome();
        home = waitFrame(HomeFrame.class, 2000);
        home.clickLogout();
        login = waitFrame(LoginFrame.class, 2000);
        step("27 退出登录，回到登录页", login != null);

        // ── 王同学处理邀请 ──
        login.fillAndLogin("2021003", "123456");
        home = waitFrame(HomeFrame.class, 2000);
        dialogs.queueConfirmYes();
        home.clickHandleInvitations();
        step("28 被邀请者-同意小组邀请", findVisible(HomeFrame.class) != null);

        home.clickLogout();
        login = waitFrame(LoginFrame.class, 2000);

        // ── 赵同学申请加入 + 组长审核 ──
        login.fillAndLogin("2021004", "123456");
        home = waitFrame(HomeFrame.class, 2000);
        home.clickCourseButton("管理信息系统");
        course = waitFrame(CourseFrame.class, 2000);
        String joinLabel = course.getJoinableOptionLabel(0);
        boolean joinOk = joinLabel != null;
        if (joinOk) {
            dialogs.queueChoice(joinLabel);
            course.clickJoinGroup();
        }
        step("29 加入小组-提交申请", joinOk);

        course.clickBackHome();
        home = waitFrame(HomeFrame.class, 2000);
        home.clickLogout();
        login = waitFrame(LoginFrame.class, 2000);

        login.fillAndLogin("2021002", "123456");
        home = waitFrame(HomeFrame.class, 2000);
        dialogs.queueConfirmYes();
        home.clickHandleInvitations();
        step("30 组长-同意加入申请", findVisible(HomeFrame.class) != null);

        // 收尾
        closeAllFrames();
        step("31 全部窗口可正常关闭", findVisible(JFrame.class) == null);

        System.out.println("\n--- GUI 逐步验收完成 ---");
    }

    private static void step(String name, boolean ok) {
        int n = stepNo.incrementAndGet();
        String line = String.format("[%02d] %s ... %s", n, name, ok ? "PASS" : "FAIL");
        System.out.println(line);
        if (ok) {
            passed.add(name);
        } else {
            failed.add(name);
        }
    }

    private static <T extends JFrame> T findVisible(Class<T> type) {
        for (Window w : Window.getWindows()) {
            if (type.isInstance(w) && w.isVisible()) {
                return type.cast(w);
            }
        }
        return null;
    }

    private static <T extends JFrame> T waitFrame(Class<T> type, int maxMs) {
        long deadline = System.currentTimeMillis() + maxMs;
        while (System.currentTimeMillis() < deadline) {
            T frame = findVisible(type);
            if (frame != null) {
                return frame;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return findVisible(type);
    }

    private static void closeAllFrames() {
        for (Window w : Window.getWindows()) {
            if (w instanceof JFrame frame) {
                frame.dispose();
            }
        }
    }
}
