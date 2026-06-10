package com.mis.team;

import com.mis.team.model.ChatMessage;
import com.mis.team.model.Course;
import com.mis.team.model.GroupFile;
import com.mis.team.model.GroupTask;
import com.mis.team.model.Invitation;
import com.mis.team.model.Notification;
import com.mis.team.model.Student;
import com.mis.team.model.StudyGroup;
import com.mis.team.service.TeamService;
import com.mis.team.util.DatabaseUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 严格全功能验收：覆盖需求文档中每一项可操作功能。
 */
public class StrictAcceptanceTestRunner {
    private final List<String> passed = new ArrayList<>();
    private final List<String> failed = new ArrayList<>();
    private TeamService service;

    public static void main(String[] args) throws Exception {
        Path testDb = Path.of("data", "strict_acceptance_test.db");
        Files.createDirectories(Path.of("data"));
        if (Files.exists(testDb)) {
            Files.delete(testDb);
        }
        System.setProperty("team.db.path", testDb.toString());
        DatabaseUtil.resetForTesting();

        StrictAcceptanceTestRunner runner = new StrictAcceptanceTestRunner();
        runner.runAll();
        DatabaseUtil.close();
        System.exit(runner.failed.isEmpty() ? 0 : 1);
    }

    private void runAll() throws Exception {
        service = new TeamService();
        System.out.println("========== 严格全功能验收测试 ==========\n");

        section("一、登录");
        testLoginValid();
        testLoginInvalidPassword();
        testLoginInvalidStudentNo();
        testLoginEmptyCredentials();

        section("二、课程与主页");
        testDifferentStudentsDifferentCourses();
        testCourseDetails();
        testHomeGroupsList();
        testHomeNotifications();
        testUnreadNotifications();

        section("三、课程小组列表");
        testCourseMyGroups();
        testCourseAllGroups();
        testGroupDetails();

        section("四、小组成员");
        testGroupMembers();
        testMemberRoles();

        section("五、讨论区/发消息");
        testChatHistoryLoad();
        testChatSenderNames();
        testChatOwnVsOthers();
        testChatMessageOrder();
        testSendMessageSuccess();
        testSendEmptyMessageRejected();
        testSendWhitespaceMessageRejected();
        testSendMessageTrimmed();
        testSendMessageVisibleToOtherMember();
        testSendMessagePersistence();
        testMultiUserChatExchange();

        section("六、任务管理");
        testTaskListLoad();
        testTaskFieldsComplete();
        testTaskStatusUpdate();
        testAddTask();
        testSubmitHomeworkTask();

        section("七、文件管理");
        testFileUploadRecord();
        testFileUploadOnDisk();
        testFileUploaderName();
        testMultipleFileUploads();

        section("八、创建小组与邀请");
        testCreateGroupLeaderInGroup();
        testCreateGroupSendsInvite();
        testCreateGroupSendsNotification();
        testCreateGroupSkipsInvalidStudentNo();
        testCreateGroupSkipsSelfInvite();
        testAcceptInvite();
        testRejectInvite();
        testInviteUnauthorizedReject();

        section("九、申请加入小组");
        testJoinRequestToLeader();
        testJoinRequestNotification();
        testDuplicateJoinRequestBlocked();
        testAlreadyMemberJoinBlocked();
        testAcceptJoinRequest();
        testRejectJoinRequest();
        testJoinRequestLeaderOnly();

        section("十、边界与权限");
        testNonMemberCannotSeeGroupMessages(); // 非成员不应在组内 - 通过 isMember  gate UI; service仍可查消息需验证
        testInvitationStatusAfterAccept();
        testMarkNotificationRead();

        System.out.println("\n========== 测试结果 ==========");
        System.out.println("通过: " + passed.size());
        passed.forEach(p -> System.out.println("  ✓ " + p));
        System.out.println("失败: " + failed.size());
        failed.forEach(f -> System.out.println("  ✗ " + f));
        if (!failed.isEmpty()) {
            System.out.println("\n请修复以上失败项后重新运行 StrictAcceptanceTestRunner");
        }
    }

    private void section(String title) {
        System.out.println("\n--- " + title + " ---");
    }

    private void check(String name, boolean ok) {
        if (ok) {
            passed.add(name);
            System.out.println("[PASS] " + name);
        } else {
            failed.add(name);
            System.out.println("[FAIL] " + name);
        }
    }

    private void checkThrows(String name, Runnable action, Class<? extends Exception> expected) {
        try {
            action.run();
            failed.add(name + " (应抛异常但未抛)");
            System.out.println("[FAIL] " + name + " (应抛异常但未抛)");
        } catch (Exception e) {
            boolean ok = expected.isInstance(e);
            if (ok) {
                passed.add(name);
                System.out.println("[PASS] " + name);
            } else {
                failed.add(name + " (异常类型不符: " + e.getMessage() + ")");
                System.out.println("[FAIL] " + name + " (异常: " + e.getMessage() + ")");
            }
        }
    }

    private Student student(String no) throws Exception {
        return service.login(no, "123456").orElseThrow(() -> new RuntimeException("登录失败: " + no));
    }

    private StudyGroup groupOf(Student s, String groupName) throws Exception {
        return service.getMyGroups(s.getId()).stream()
                .filter(g -> groupName.equals(g.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("找不到小组: " + groupName));
    }

    // ── 登录 ──

    private void testLoginValid() throws Exception {
        Optional<Student> s = service.login("2021001", "123456");
        check("登录-正确账号", s.isPresent() && "陈同学".equals(s.get().getName()));
    }

    private void testLoginInvalidPassword() throws Exception {
        check("登录-错误密码", service.login("2021001", "wrong").isEmpty());
    }

    private void testLoginInvalidStudentNo() throws Exception {
        check("登录-不存在学号", service.login("9999999", "123456").isEmpty());
    }

    private void testLoginEmptyCredentials() throws Exception {
        check("登录-空密码", service.login("2021001", "").isEmpty());
        check("登录-空学号", service.login("", "123456").isEmpty());
    }

    // ── 课程与主页 ──

    private void testDifferentStudentsDifferentCourses() throws Exception {
        Student chen = student("2021001");
        Student liu = student("2021005");
        List<Course> c1 = service.getStudentCourses(chen.getId());
        List<Course> c2 = service.getStudentCourses(liu.getId());
        check("课程-陈同学2门课", c1.size() == 2);
        check("课程-刘同学1门课", c2.size() == 1);
        check("课程-两人课程不同",
                c1.stream().anyMatch(c -> "管理信息系统".equals(c.getName()))
                        && c2.stream().anyMatch(c -> "数据库原理".equals(c.getName())));
    }

    private void testCourseDetails() throws Exception {
        Course c = service.getCourse(1).orElseThrow();
        check("课程-名称教师", "管理信息系统".equals(c.getName()) && "张老师".equals(c.getTeacher()));
        check("课程-描述非空", c.getDescription() != null && !c.getDescription().isEmpty());
    }

    private void testHomeGroupsList() throws Exception {
        Student chen = student("2021001");
        List<StudyGroup> groups = service.getMyGroups(chen.getId());
        check("主页-小组列表非空", !groups.isEmpty());
        StudyGroup g2 = groupOf(chen, "第2组");
        check("主页-小组含课程名", "管理信息系统".equals(g2.getCourseName()));
        check("主页-小组人数>0", g2.getMemberCount() >= 4);
    }

    private void testHomeNotifications() throws Exception {
        Student chen = student("2021001");
        List<Notification> notes = service.getNotifications(chen.getId());
        check("主页-通知列表非空", !notes.isEmpty());
        check("主页-含课程公告", notes.stream().anyMatch(n -> "课程公告".equals(n.getTitle())));
    }

    private void testUnreadNotifications() throws Exception {
        Student chen = student("2021001");
        int unread = service.countUnreadNotifications(chen.getId());
        check("主页-未读通知计数", unread > 0);
    }

    // ── 课程小组 ──

    private void testCourseMyGroups() throws Exception {
        Student chen = student("2021001");
        List<StudyGroup> mine = service.getMyGroupsInCourse(chen.getId(), 1);
        check("课程页-我已加入的小组", mine.stream().anyMatch(g -> "第2组".equals(g.getName())));
    }

    private void testCourseAllGroups() throws Exception {
        List<StudyGroup> all = service.getCourseGroups(1);
        check("课程页-全部小组>=2", all.size() >= 2);
        check("课程页-组长信息", all.stream().allMatch(g -> g.getLeaderName() != null));
    }

    private void testGroupDetails() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        StudyGroup detail = service.getGroup(g2.getId()).orElseThrow();
        check("小组-详情组长是陈同学", chen.getId() == detail.getLeaderId());
    }

    // ── 成员 ──

    private void testGroupMembers() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        List<Student> members = service.getGroupMembers(g2.getId());
        check("成员-至少4人", members.size() >= 4);
        check("成员-含陈李王赵", members.size() >= 4
                && members.stream().anyMatch(m -> m.getName().contains("陈"))
                && members.stream().anyMatch(m -> m.getName().contains("李")));
    }

    private void testMemberRoles() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        var roles = service.getMemberRoles(g2.getId());
        check("成员-组长角色", "leader".equals(roles.get(chen.getId())));
    }

    // ── 聊天/消息 ──

    private void testChatHistoryLoad() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        List<ChatMessage> msgs = service.getMessages(g2.getId(), chen.getId());
        check("消息-历史>=5条", msgs.size() >= 5);
    }

    private void testChatSenderNames() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        List<ChatMessage> msgs = service.getMessages(g2.getId(), chen.getId());
        check("消息-发送者姓名非空", msgs.stream().allMatch(m -> m.getSenderName() != null && !m.getSenderName().isEmpty()));
    }

    private void testChatOwnVsOthers() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        List<ChatMessage> msgs = service.getMessages(g2.getId(), chen.getId());
        check("消息-有自己发的", msgs.stream().anyMatch(ChatMessage::isOwn));
        check("消息-有别人发的", msgs.stream().anyMatch(m -> !m.isOwn()));
    }

    private void testChatMessageOrder() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        List<ChatMessage> msgs = service.getMessages(g2.getId(), chen.getId());
        boolean ordered = true;
        for (int i = 1; i < msgs.size(); i++) {
            if (msgs.get(i).getId() < msgs.get(i - 1).getId()) {
                ordered = false;
                break;
            }
        }
        check("消息-按ID升序", ordered);
    }

    private void testSendMessageSuccess() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        String msg = "严格验收消息A_" + System.nanoTime();
        service.sendMessage(g2.getId(), chen.getId(), msg);
        check("消息-发送成功可查", service.getMessages(g2.getId(), chen.getId()).stream()
                .anyMatch(m -> msg.equals(m.getContent())));
    }

    private void testSendEmptyMessageRejected() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        int before = service.getMessages(g2.getId(), chen.getId()).size();
        boolean sent = service.sendMessage(g2.getId(), chen.getId(), "");
        int after = service.getMessages(g2.getId(), chen.getId()).size();
        check("消息-空内容不发送", !sent && before == after);
    }

    private void testSendWhitespaceMessageRejected() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        int before = service.getMessages(g2.getId(), chen.getId()).size();
        boolean sent = service.sendMessage(g2.getId(), chen.getId(), "   \t  ");
        int after = service.getMessages(g2.getId(), chen.getId()).size();
        check("消息-纯空白不发送", !sent && before == after);
    }

    private void testSendMessageTrimmed() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        String core = "trim测试_" + System.nanoTime();
        service.sendMessage(g2.getId(), chen.getId(), "  " + core + "  ");
        check("消息-首尾空格裁剪", service.getMessages(g2.getId(), chen.getId()).stream()
                .anyMatch(m -> core.equals(m.getContent())));
    }

    private void testSendMessageVisibleToOtherMember() throws Exception {
        Student chen = student("2021001");
        Student li = student("2021002");
        StudyGroup g2 = groupOf(chen, "第2组");
        String msg = "给他人可见_" + System.nanoTime();
        service.sendMessage(g2.getId(), chen.getId(), msg);
        check("消息-其他成员可见", service.getMessages(g2.getId(), li.getId()).stream()
                .anyMatch(m -> msg.equals(m.getContent()) && !m.isOwn()));
    }

    private void testSendMessagePersistence() throws Exception {
        Student wang = student("2021003");
        StudyGroup g2 = groupOf(wang, "第2组");
        String msg = "持久化测试_" + System.nanoTime();
        service.sendMessage(g2.getId(), wang.getId(), msg);
        // 模拟重新登录
        Student wang2 = student("2021003");
        check("消息-重登后仍在", service.getMessages(g2.getId(), wang2.getId()).stream()
                .anyMatch(m -> msg.equals(m.getContent())));
    }

    private void testMultiUserChatExchange() throws Exception {
        Student chen = student("2021001");
        Student zhao = student("2021004");
        StudyGroup g2 = groupOf(chen, "第2组");
        String fromChen = "陈发_" + System.nanoTime();
        String fromZhao = "赵回_" + System.nanoTime();
        service.sendMessage(g2.getId(), chen.getId(), fromChen);
        service.sendMessage(g2.getId(), zhao.getId(), fromZhao);
        List<ChatMessage> msgs = service.getMessages(g2.getId(), chen.getId());
        int chenIdx = -1, zhaoIdx = -1;
        for (int i = 0; i < msgs.size(); i++) {
            if (fromChen.equals(msgs.get(i).getContent())) chenIdx = i;
            if (fromZhao.equals(msgs.get(i).getContent())) zhaoIdx = i;
        }
        check("消息-多人互发都入库", chenIdx >= 0 && zhaoIdx >= 0);
        check("消息-后发在后", chenIdx >= 0 && zhaoIdx >= 0 && zhaoIdx > chenIdx);
    }

    // ── 任务 ──

    private void testTaskListLoad() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        check("任务-列表>=4", service.getTasks(g2.getId()).size() >= 4);
    }

    private void testTaskFieldsComplete() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        GroupTask t = service.getTasks(g2.getId()).stream()
                .filter(x -> "数据库设计".equals(x.getTitle())).findFirst().orElseThrow();
        check("任务-标题状态", t.getTitle() != null && "进行中".equals(t.getStatus()));
        check("任务-负责人", t.getAssigneeName() != null && t.getAssigneeName().contains("赵"));
        check("任务-描述", t.getDescription() != null);
    }

    private void testTaskStatusUpdate() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        GroupTask t = service.getTasks(g2.getId()).get(0);
        service.updateTaskStatus(t.getId(), "待开始");
        String status = service.getTasks(g2.getId()).stream()
                .filter(x -> x.getId() == t.getId()).findFirst().map(GroupTask::getStatus).orElse("");
        check("任务-更新状态", "待开始".equals(status));
    }

    private void testAddTask() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        int before = service.getTasks(g2.getId()).size();
        String title = "验收新任务_" + System.nanoTime();
        service.addTask(g2.getId(), chen.getId(), title, "描述", "待开始", chen.getId());
        check("任务-新增任务", service.getTasks(g2.getId()).size() == before + 1);
        check("任务-新增任务可查", service.getTasks(g2.getId()).stream().anyMatch(t -> title.equals(t.getTitle())));
    }

    private void testSubmitHomeworkTask() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        String title = "作业：期末报告_" + System.nanoTime();
        service.addTask(g2.getId(), chen.getId(), title, "说明", "已提交", chen.getId());
        check("作业-提交后任务可查", service.getTasks(g2.getId()).stream()
                .anyMatch(t -> title.equals(t.getTitle()) && "已提交".equals(t.getStatus())));
    }

    // ── 文件 ──

    private void testFileUploadRecord() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        Path tmp = Files.createTempFile("strict", ".docx");
        Files.writeString(tmp, "homework");
        service.uploadFile(g2.getId(), chen.getId(), tmp);
        check("文件-上传记录", service.getFiles(g2.getId()).stream()
                .anyMatch(f -> f.getFileName().endsWith(".docx")));
        Files.deleteIfExists(tmp);
    }

    private void testFileUploadOnDisk() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        Path tmp = Files.createTempFile("disk", ".pdf");
        Files.writeString(tmp, "pdf content");
        service.uploadFile(g2.getId(), chen.getId(), tmp);
        GroupFile rec = service.getFiles(g2.getId()).stream()
                .filter(f -> f.getFileName().endsWith(".pdf")).reduce((a, b) -> b).orElseThrow();
        check("文件-磁盘文件存在", Files.exists(Path.of(rec.getFilePath())));
        Files.deleteIfExists(tmp);
    }

    private void testFileUploaderName() throws Exception {
        Student wang = student("2021003");
        StudyGroup g2 = groupOf(wang, "第2组");
        Path tmp = Files.createTempFile("uploader", ".txt");
        Files.writeString(tmp, "x");
        service.uploadFile(g2.getId(), wang.getId(), tmp);
        check("文件-上传者姓名", service.getFiles(g2.getId()).stream()
                .anyMatch(f -> f.getFileName().endsWith(".txt") && f.getUploaderName().contains("王")));
        Files.deleteIfExists(tmp);
    }

    private void testMultipleFileUploads() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        int before = service.getFiles(g2.getId()).size();
        Path a = Files.createTempFile("multi1", ".txt");
        Path b = Files.createTempFile("multi2", ".txt");
        Files.writeString(a, "a");
        Files.writeString(b, "b");
        service.uploadFile(g2.getId(), chen.getId(), a);
        service.uploadFile(g2.getId(), chen.getId(), b);
        check("文件-多次上传", service.getFiles(g2.getId()).size() >= before + 2);
        Files.deleteIfExists(a);
        Files.deleteIfExists(b);
    }

    // ── 创建小组/邀请 ──

    private void testCreateGroupLeaderInGroup() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组A", chen.getId(), List.of());
        check("建组-组长自动入组", service.isGroupMember(gid, chen.getId()));
    }

    private void testCreateGroupSendsInvite() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组B", chen.getId(), List.of("2021003"));
        Student wang = student("2021003");
        check("建组-邀请pending", service.getPendingInvitations(wang.getId()).stream()
                .anyMatch(i -> "invite".equals(i.getType()) && i.getGroupId() == gid));
    }

    private void testCreateGroupSendsNotification() throws Exception {
        Student chen = student("2021001");
        service.createGroup(1, "严格测试组C", chen.getId(), List.of("2021004"));
        Student zhao = student("2021004");
        check("建组-通知送达", service.getNotifications(zhao.getId()).stream()
                .anyMatch(n -> "小组邀请".equals(n.getTitle()) && n.getContent().contains("严格测试组C")));
    }

    private void testCreateGroupSkipsInvalidStudentNo() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组D", chen.getId(), List.of("9999999", "2021002"));
        // 只有李同学应收到邀请
        Student li = student("2021002");
        long count = service.getPendingInvitations(li.getId()).stream()
                .filter(i -> i.getGroupId() != null && i.getGroupId() == gid).count();
        check("建组-无效学号忽略", count == 1);
    }

    private void testCreateGroupSkipsSelfInvite() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组E", chen.getId(), List.of("2021001", "2021002"));
        Student chen2 = student("2021001");
        long selfInvite = service.getPendingInvitations(chen2.getId()).stream()
                .filter(i -> i.getGroupId() != null && i.getGroupId() == gid && "invite".equals(i.getType())).count();
        check("建组-不邀请自己", selfInvite == 0);
    }

    private void testAcceptInvite() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组F", chen.getId(), List.of("2021003"));
        Student wang = student("2021003");
        Invitation inv = service.getPendingInvitations(wang.getId()).stream()
                .filter(i -> i.getGroupId() == gid).findFirst().orElseThrow();
        service.acceptInvitation(inv.getId(), wang.getId());
        check("邀请-接受后入组", service.isGroupMember(gid, wang.getId()));
    }

    private void testRejectInvite() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组G", chen.getId(), List.of("2021004"));
        Student zhao = student("2021004");
        Invitation inv = service.getPendingInvitations(zhao.getId()).stream()
                .filter(i -> i.getGroupId() == gid).findFirst().orElseThrow();
        service.rejectInvitation(inv.getId(), zhao.getId());
        check("邀请-拒绝后不入组", !service.isGroupMember(gid, zhao.getId()));
    }

    private void testInviteUnauthorizedReject() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组H", chen.getId(), List.of("2021003"));
        Student wang = student("2021003");
        Invitation inv = service.getPendingInvitations(wang.getId()).stream()
                .filter(i -> i.getGroupId() == gid).findFirst().orElseThrow();
        checkThrows("邀请-他人无权处理", () -> {
            try {
                service.acceptInvitation(inv.getId(), student("2021004").getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, RuntimeException.class);
        // unwrap - actually checkThrows needs to handle SQLException wrapped
    }

    // ── 申请加入 ──

    private void testJoinRequestToLeader() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组I", chen.getId(), List.of());
        Student liu = student("2021005");
        StudyGroup g = service.getGroup(gid).orElseThrow();
        if (!service.isGroupMember(gid, liu.getId())) {
            service.requestJoinGroup(gid, liu.getId(), g);
        }
        Invitation req = service.getPendingJoinRequestsForLeader(chen.getId()).stream()
                .filter(i -> i.getGroupId() == gid && i.getInviterId() == liu.getId()).findFirst().orElse(null);
        check("申请-组长收到", req != null);
    }

    private void testJoinRequestNotification() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组J", chen.getId(), List.of());
        Student liu = student("2021005");
        StudyGroup g = service.getGroup(gid).orElseThrow();
        if (!service.getPendingJoinRequestsForLeader(chen.getId()).stream()
                .anyMatch(i -> i.getGroupId() == gid && i.getInviterId() == liu.getId())) {
            service.requestJoinGroup(gid, liu.getId(), g);
        }
        check("申请-组长有通知", service.getNotifications(chen.getId()).stream()
                .anyMatch(n -> "加入申请".equals(n.getTitle())));
    }

    private void testDuplicateJoinRequestBlocked() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组K", chen.getId(), List.of());
        Student liu = student("2021005");
        StudyGroup g = service.getGroup(gid).orElseThrow();
        if (!service.getPendingJoinRequestsForLeader(chen.getId()).stream()
                .anyMatch(i -> i.getGroupId() == gid && i.getInviterId() == liu.getId())) {
            service.requestJoinGroup(gid, liu.getId(), g);
        }
        checkThrows("申请-重复提交拦截", () -> {
            try {
                service.requestJoinGroup(gid, liu.getId(), g);
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, IllegalStateException.class);
    }

    private void testAlreadyMemberJoinBlocked() throws Exception {
        Student chen = student("2021001");
        StudyGroup g2 = groupOf(chen, "第2组");
        checkThrows("申请-已在组内拦截", () -> {
            try {
                service.requestJoinGroup(g2.getId(), chen.getId(), g2);
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, IllegalStateException.class);
    }

    private void testAcceptJoinRequest() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组L", chen.getId(), List.of());
        Student liu = student("2021005");
        StudyGroup g = service.getGroup(gid).orElseThrow();
        if (!service.isGroupMember(gid, liu.getId())) {
            if (!service.getPendingJoinRequestsForLeader(chen.getId()).stream()
                    .anyMatch(i -> i.getGroupId() == gid && i.getInviterId() == liu.getId())) {
                service.requestJoinGroup(gid, liu.getId(), g);
            }
            Invitation req = service.getPendingJoinRequestsForLeader(chen.getId()).stream()
                    .filter(i -> i.getGroupId() == gid && i.getInviterId() == liu.getId()).findFirst().orElseThrow();
            service.acceptInvitation(req.getId(), chen.getId());
        }
        check("申请-同意后入组", service.isGroupMember(gid, liu.getId()));
        check("申请-同意后有通知", service.getNotifications(liu.getId()).stream()
                .anyMatch(n -> "加入成功".equals(n.getTitle())));
    }

    private void testRejectJoinRequest() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组M", chen.getId(), List.of());
        Student liu = student("2021005");
        StudyGroup g = service.getGroup(gid).orElseThrow();
        service.requestJoinGroup(gid, liu.getId(), g);
        Invitation req = service.getPendingJoinRequestsForLeader(chen.getId()).stream()
                .filter(i -> i.getGroupId() == gid && i.getInviterId() == liu.getId()).findFirst().orElseThrow();
        service.rejectInvitation(req.getId(), chen.getId());
        check("申请-拒绝后不入组", !service.isGroupMember(gid, liu.getId()));
        check("申请-拒绝后有通知", service.getNotifications(liu.getId()).stream()
                .anyMatch(n -> "加入被拒绝".equals(n.getTitle())));
    }

    private void testJoinRequestLeaderOnly() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组N", chen.getId(), List.of());
        Student liu = student("2021005");
        StudyGroup g = service.getGroup(gid).orElseThrow();
        service.requestJoinGroup(gid, liu.getId(), g);
        Invitation req = service.getPendingJoinRequestsForLeader(chen.getId()).stream()
                .filter(i -> i.getGroupId() == gid).findFirst().orElseThrow();
        checkThrows("申请-非组长不能审", () -> {
            try {
                service.acceptInvitation(req.getId(), student("2021003").getId());
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, IllegalStateException.class);
        service.rejectInvitation(req.getId(), chen.getId());
    }

    // ── 边界 ──

    private void testNonMemberCannotSeeGroupMessages() throws Exception {
        Student liu = student("2021005");
        StudyGroup g2 = service.getCourseGroups(1).stream()
                .filter(g -> "第2组".equals(g.getName())).findFirst().orElseThrow();
        check("权限-非成员确认", !service.isGroupMember(g2.getId(), liu.getId()));
        checkThrows("权限-非成员不能读消息", () -> {
            try {
                service.getMessages(g2.getId(), liu.getId());
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, IllegalStateException.class);
        checkThrows("权限-非成员不能发消息", () -> {
            try {
                service.sendMessage(g2.getId(), liu.getId(), "非法消息");
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, IllegalStateException.class);
    }

    private void testInvitationStatusAfterAccept() throws Exception {
        Student chen = student("2021001");
        int gid = service.createGroup(1, "严格测试组O", chen.getId(), List.of("2021002"));
        Student li = student("2021002");
        Invitation inv = service.getPendingInvitations(li.getId()).stream()
                .filter(i -> i.getGroupId() == gid).findFirst().orElseThrow();
        service.acceptInvitation(inv.getId(), li.getId());
        check("邀请-接受后不再pending", service.getPendingInvitations(li.getId()).stream()
                .noneMatch(i -> i.getId() == inv.getId()));
    }

    private void testMarkNotificationRead() throws Exception {
        Student chen = student("2021001");
        Notification n = service.getNotifications(chen.getId()).stream()
                .filter(x -> !x.isRead()).findFirst().orElseThrow();
        int before = service.countUnreadNotifications(chen.getId());
        boolean marked = service.markNotificationRead(n.getId());
        int after = service.countUnreadNotifications(chen.getId());
        check("通知-标记已读", marked && after == before - 1);
    }
}
