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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 模拟用户完整操作流程的验收测试（等价于人工点按钮验收）
 */
public class AcceptanceTestRunner {
    private final List<String> passed = new ArrayList<>();
    private final List<String> failed = new ArrayList<>();
    private TeamService service;

    public static void main(String[] args) throws Exception {
        Path testDb = Path.of("data", "acceptance_test.db");
        Files.createDirectories(Path.of("data"));
        if (Files.exists(testDb)) {
            Files.delete(testDb);
        }
        System.setProperty("team.db.path", testDb.toString());
        DatabaseUtil.resetForTesting();

        AcceptanceTestRunner runner = new AcceptanceTestRunner();
        runner.runAll();
        DatabaseUtil.close();
        System.exit(runner.failed.isEmpty() ? 0 : 1);
    }

    private void runAll() throws Exception {
        service = new TeamService();
        System.out.println("========== 学生课程小组系统 验收测试 ==========\n");

        testLogin();
        testStudentCoursesDiffer();
        testHomeData();
        testGroupChat();
        testSendMessage();
        testTasks();
        testFileUpload();
        testCreateGroupAndInvite();
        testJoinGroupRequest();
        testRejectJoinRequest();

        System.out.println("\n========== 测试结果 ==========");
        System.out.println("通过: " + passed.size());
        passed.forEach(p -> System.out.println("  ✓ " + p));
        System.out.println("失败: " + failed.size());
        failed.forEach(f -> System.out.println("  ✗ " + f));
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

    private void testLogin() throws Exception {
        Optional<Student> ok = service.login("2021001", "123456");
        Optional<Student> bad = service.login("2021001", "wrong");
        check("登录-正确学号密码", ok.isPresent() && "陈同学".equals(ok.get().getName()));
        check("登录-错误密码拒绝", bad.isEmpty());
    }

    private void testStudentCoursesDiffer() throws Exception {
        Student chen = service.login("2021001", "123456").orElseThrow();
        Student liu = service.login("2021005", "123456").orElseThrow();
        List<Course> chenCourses = service.getStudentCourses(chen.getId());
        List<Course> liuCourses = service.getStudentCourses(liu.getId());
        check("不同学生看到不同课程", chenCourses.size() == 2 && liuCourses.size() == 1);
    }

    private void testHomeData() throws Exception {
        Student chen = service.login("2021001", "123456").orElseThrow();
        List<StudyGroup> groups = service.getMyGroups(chen.getId());
        List<Notification> notes = service.getNotifications(chen.getId());
        check("主页-已有课程小组列表", groups.stream().anyMatch(g -> "第2组".equals(g.getName())));
        check("主页-消息通知", !notes.isEmpty());
    }

    private void testGroupChat() throws Exception {
        Student chen = service.login("2021001", "123456").orElseThrow();
        StudyGroup g2 = service.getMyGroups(chen.getId()).stream()
                .filter(g -> "第2组".equals(g.getName())).findFirst().orElseThrow();
        List<ChatMessage> msgs = service.getMessages(g2.getId(), chen.getId());
        check("小组讨论区-历史消息加载", msgs.size() >= 5);
        check("小组讨论区-区分自己/他人消息", msgs.stream().anyMatch(ChatMessage::isOwn)
                && msgs.stream().anyMatch(m -> !m.isOwn()));
    }

    private void testSendMessage() throws Exception {
        Student chen = service.login("2021001", "123456").orElseThrow();
        StudyGroup g2 = service.getMyGroups(chen.getId()).stream()
                .filter(g -> "第2组".equals(g.getName())).findFirst().orElseThrow();
        String testMsg = "验收测试消息_" + System.currentTimeMillis();
        service.sendMessage(g2.getId(), chen.getId(), testMsg);
        boolean found = service.getMessages(g2.getId(), chen.getId()).stream()
                .anyMatch(m -> testMsg.equals(m.getContent()));
        check("讨论区-发送消息", found);
    }

    private void testTasks() throws Exception {
        Student chen = service.login("2021001", "123456").orElseThrow();
        StudyGroup g2 = service.getMyGroups(chen.getId()).stream()
                .filter(g -> "第2组".equals(g.getName())).findFirst().orElseThrow();
        List<GroupTask> tasks = service.getTasks(g2.getId());
        check("任务管理-加载任务列表", tasks.size() >= 4);
        if (!tasks.isEmpty()) {
            int taskId = tasks.get(0).getId();
            service.updateTaskStatus(taskId, "已完成");
            String status = service.getTasks(g2.getId()).stream()
                    .filter(t -> t.getId() == taskId).findFirst().map(GroupTask::getStatus).orElse("");
            check("任务管理-更新任务状态", "已完成".equals(status));
        }
    }

    private void testFileUpload() throws Exception {
        Student chen = service.login("2021001", "123456").orElseThrow();
        StudyGroup g2 = service.getMyGroups(chen.getId()).stream()
                .filter(g -> "第2组".equals(g.getName())).findFirst().orElseThrow();
        Path tmp = Files.createTempFile("acceptance", ".txt");
        Files.writeString(tmp, "test file content");
        service.uploadFile(g2.getId(), chen.getId(), tmp);
        List<GroupFile> files = service.getFiles(g2.getId());
        check("文件上传-记录入库", files.stream().anyMatch(f -> f.getFileName().endsWith(".txt")));
        Files.deleteIfExists(tmp);
    }

    private void testCreateGroupAndInvite() throws Exception {
        Student chen = service.login("2021001", "123456").orElseThrow();
        int groupId = service.createGroup(1, "验收测试组", chen.getId(), List.of("2021002"));
        check("创建小组-成功", groupId > 0);
        Student li = service.login("2021002", "123456").orElseThrow();
        List<Invitation> invites = service.getPendingInvitations(li.getId());
        boolean hasInvite = invites.stream().anyMatch(i -> "invite".equals(i.getType())
                && i.getGroupId() != null && i.getGroupId() == groupId);
        check("创建小组-组员收到邀请", hasInvite);
        // 验证邀请显示名称：应是陈同学邀请，不是李同学
        Invitation inv = invites.stream().filter(i -> i.getGroupId() == groupId).findFirst().orElseThrow();
        check("创建小组-邀请显示邀请人正确", "陈同学".equals(inv.getInviterName()));
        service.acceptInvitation(inv.getId(), li.getId());
        check("创建小组-接受邀请后加入", service.isGroupMember(groupId, li.getId()));
    }

    private void testJoinGroupRequest() throws Exception {
        // 赵同学只在第2组，可申请加入第1组
        Student zhao = service.login("2021004", "123456").orElseThrow();
        StudyGroup g1 = service.getCourseGroups(1).stream()
                .filter(g -> "第1组".equals(g.getName())).findFirst().orElseThrow();
        if (service.isGroupMember(g1.getId(), zhao.getId())) {
            check("加入小组-跳过(已在组内)", true);
            return;
        }
        service.requestJoinGroup(g1.getId(), zhao.getId(), g1);
        Student leader = service.login("2021002", "123456").orElseThrow();
        Invitation req = service.getPendingJoinRequestsForLeader(leader.getId()).stream()
                .filter(i -> "join_request".equals(i.getType()) && g1.getId() == i.getGroupId()
                        && i.getInviterId() == zhao.getId())
                .findFirst().orElse(null);
        check("加入小组-组长收到申请", req != null);
        if (req != null) {
            check("加入小组-申请显示申请人正确", "赵同学".equals(req.getInviterName()));
            service.acceptInvitation(req.getId(), leader.getId());
            check("加入小组-组长同意后加入", service.isGroupMember(g1.getId(), zhao.getId()));
        }
    }

    private void testRejectJoinRequest() throws Exception {
        Student chen = service.login("2021001", "123456").orElseThrow();
        StudyGroup g2 = service.getMyGroups(chen.getId()).stream()
                .filter(g -> "第2组".equals(g.getName())).findFirst().orElseThrow();
        // 刘同学不在第2组，用于测试拒绝
        Student liu = service.login("2021005", "123456").orElseThrow();
        if (service.isGroupMember(g2.getId(), liu.getId())) {
            check("拒绝申请-跳过(已在组内)", true);
            return;
        }
        if (service.getPendingJoinRequestsForLeader(chen.getId()).stream()
                .anyMatch(i -> i.getGroupId() == g2.getId() && i.getInviterId() == liu.getId())) {
            // 清理残留 pending
            Invitation old = service.getPendingJoinRequestsForLeader(chen.getId()).stream()
                    .filter(i -> i.getGroupId() == g2.getId() && i.getInviterId() == liu.getId())
                    .findFirst().orElseThrow();
            service.rejectInvitation(old.getId(), chen.getId());
        }
        service.requestJoinGroup(g2.getId(), liu.getId(), g2);
        Invitation req = service.getPendingJoinRequestsForLeader(chen.getId()).stream()
                .filter(i -> i.getGroupId() == g2.getId() && i.getInviterId() == liu.getId())
                .findFirst().orElseThrow();
        service.rejectInvitation(req.getId(), chen.getId());
        check("拒绝申请-未加入小组", !service.isGroupMember(g2.getId(), liu.getId()));
    }
}
