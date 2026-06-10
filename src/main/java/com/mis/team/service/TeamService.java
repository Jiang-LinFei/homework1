package com.mis.team.service;

import com.mis.team.dao.CourseDAO;
import com.mis.team.dao.FileDAO;
import com.mis.team.dao.GroupDAO;
import com.mis.team.dao.InvitationDAO;
import com.mis.team.dao.MessageDAO;
import com.mis.team.dao.NotificationDAO;
import com.mis.team.dao.StudentDAO;
import com.mis.team.dao.TaskDAO;
import com.mis.team.model.ChatMessage;
import com.mis.team.model.Course;
import com.mis.team.model.GroupFile;
import com.mis.team.model.GroupTask;
import com.mis.team.model.Invitation;
import com.mis.team.model.Notification;
import com.mis.team.model.Student;
import com.mis.team.model.StudyGroup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TeamService {
    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final GroupDAO groupDAO = new GroupDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final FileDAO fileDAO = new FileDAO();
    private final InvitationDAO invitationDAO = new InvitationDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public Optional<Student> login(String studentNo, String password) throws SQLException {
        return studentDAO.login(studentNo, password);
    }

    public List<Course> getStudentCourses(int studentId) throws SQLException {
        return courseDAO.findByStudentId(studentId);
    }

    public Optional<Course> getCourse(int courseId) throws SQLException {
        return courseDAO.findById(courseId);
    }

    public List<StudyGroup> getMyGroups(int studentId) throws SQLException {
        return groupDAO.findByStudentId(studentId);
    }

    public List<StudyGroup> getMyGroupsInCourse(int studentId, int courseId) throws SQLException {
        return groupDAO.findByStudentAndCourse(studentId, courseId);
    }

    public List<StudyGroup> getCourseGroups(int courseId) throws SQLException {
        return groupDAO.findByCourseId(courseId);
    }

    public Optional<StudyGroup> getGroup(int groupId) throws SQLException {
        return groupDAO.findById(groupId);
    }

    public List<Student> getGroupMembers(int groupId) throws SQLException {
        return studentDAO.findByGroupId(groupId);
    }

    public Map<Integer, String> getMemberRoles(int groupId) throws SQLException {
        return studentDAO.findMemberRoles(groupId).stream()
                .map(s -> s.split(":", 2))
                .collect(Collectors.toMap(a -> Integer.parseInt(a[0]), a -> a[1]));
    }

    public List<ChatMessage> getMessages(int groupId, int studentId) throws SQLException {
        requireGroupMember(groupId, studentId);
        return messageDAO.findByGroupId(groupId, studentId);
    }

    public boolean sendMessage(int groupId, int senderId, String content) throws SQLException {
        requireGroupMember(groupId, senderId);
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        messageDAO.sendMessage(groupId, senderId, content.trim());
        return true;
    }

    public List<GroupTask> getTasks(int groupId) throws SQLException {
        return taskDAO.findByGroupId(groupId);
    }

    public void addTask(int groupId, int operatorId, String title, String description, String status, Integer assigneeId) throws SQLException {
        requireGroupMember(groupId, operatorId);
        if (assigneeId != null) {
            requireGroupMember(groupId, assigneeId);
        }
        taskDAO.addTask(groupId, title, description, status, assigneeId);
    }

    public void updateTaskStatus(int taskId, String status) throws SQLException {
        taskDAO.updateStatus(taskId, status);
    }

    public List<GroupFile> getFiles(int groupId) throws SQLException {
        return fileDAO.findByGroupId(groupId);
    }

    public void uploadFile(int groupId, int uploaderId, Path sourceFile) throws SQLException, IOException {
        requireGroupMember(groupId, uploaderId);
        Files.createDirectories(Path.of("uploads", String.valueOf(groupId)));
        String fileName = sourceFile.getFileName().toString();
        Path target = Path.of("uploads", String.valueOf(groupId), System.currentTimeMillis() + "_" + fileName);
        Files.copy(sourceFile, target, StandardCopyOption.REPLACE_EXISTING);
        fileDAO.addFile(groupId, uploaderId, fileName, target.toString());
    }

    public List<Notification> getNotifications(int studentId) throws SQLException {
        return notificationDAO.findByStudentId(studentId);
    }

    public int countUnreadNotifications(int studentId) throws SQLException {
        return notificationDAO.countUnread(studentId);
    }

    public boolean markNotificationRead(int id) throws SQLException {
        return notificationDAO.markRead(id);
    }

    public List<Invitation> getPendingInvitations(int studentId) throws SQLException {
        return invitationDAO.findPendingByInvitee(studentId);
    }

    public List<Invitation> getPendingJoinRequestsForLeader(int leaderId) throws SQLException {
        return invitationDAO.findPendingForLeader(leaderId);
    }

    public int createGroup(int courseId, String groupName, int leaderId, List<String> memberStudentNos) throws SQLException {
        int groupId = groupDAO.createGroup(courseId, groupName, leaderId);
        for (String studentNo : memberStudentNos) {
            Optional<Student> member = studentDAO.findByStudentNo(studentNo.trim());
            if (member.isPresent() && member.get().getId() != leaderId) {
                Invitation inv = new Invitation();
                inv.setGroupId(groupId);
                inv.setCourseId(courseId);
                inv.setInviterId(leaderId);
                inv.setInviteeId(member.get().getId());
                inv.setType("invite");
                inv.setMessage("邀请你加入小组「" + groupName + "」");
                int invId = invitationDAO.create(inv);
                notificationDAO.add(member.get().getId(), "小组邀请",
                        "你被邀请加入「" + groupName + "」", invId);
            }
        }
        return groupId;
    }

    public void requestJoinGroup(int groupId, int studentId, StudyGroup group) throws SQLException {
        if (groupDAO.isMember(groupId, studentId)) {
            throw new IllegalStateException("你已在该小组中");
        }
        if (invitationDAO.hasPendingJoinRequest(groupId, studentId)) {
            throw new IllegalStateException("已提交加入申请，请等待组长审核");
        }
        Invitation inv = new Invitation();
        inv.setGroupId(groupId);
        inv.setCourseId(group.getCourseId());
        inv.setInviterId(studentId);
        inv.setInviteeId(group.getLeaderId());
        inv.setType("join_request");
        inv.setMessage("申请加入小组「" + group.getName() + "」");
        int invId = invitationDAO.create(inv);
        notificationDAO.add(group.getLeaderId(), "加入申请",
                "有同学申请加入「" + group.getName() + "」", invId);
    }

    public void acceptInvitation(int invitationId, int currentStudentId) throws SQLException {
        Invitation inv = invitationDAO.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("邀请不存在"));
        if ("invite".equals(inv.getType()) && inv.getInviteeId() != currentStudentId) {
            throw new IllegalStateException("无权处理该邀请");
        }
        if ("join_request".equals(inv.getType())) {
            StudyGroup group = groupDAO.findById(inv.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("小组不存在"));
            if (group.getLeaderId() != currentStudentId) {
                throw new IllegalStateException("仅组长可审核加入申请");
            }
            if (groupDAO.isMember(inv.getGroupId(), inv.getInviterId())) {
                invitationDAO.updateStatus(invitationId, "accepted");
                return;
            }
            groupDAO.addMember(inv.getGroupId(), inv.getInviterId(), "member");
            notificationDAO.add(inv.getInviterId(), "加入成功",
                    "你已成功加入「" + group.getName() + "」", inv.getGroupId());
        } else {
            if (!groupDAO.isMember(inv.getGroupId(), inv.getInviteeId())) {
                groupDAO.addMember(inv.getGroupId(), inv.getInviteeId(), "member");
            }
            notificationDAO.add(inv.getInviterId(), "邀请已接受",
                    inv.getInviteeName() + " 已加入小组", inv.getGroupId());
        }
        invitationDAO.updateStatus(invitationId, "accepted");
    }

    public void rejectInvitation(int invitationId, int currentStudentId) throws SQLException {
        Invitation inv = invitationDAO.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("邀请不存在"));
        if ("invite".equals(inv.getType()) && inv.getInviteeId() != currentStudentId) {
            throw new IllegalStateException("无权处理该邀请");
        }
        if ("join_request".equals(inv.getType())) {
            StudyGroup group = groupDAO.findById(inv.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("小组不存在"));
            if (group.getLeaderId() != currentStudentId) {
                throw new IllegalStateException("仅组长可审核加入申请");
            }
            notificationDAO.add(inv.getInviterId(), "加入被拒绝",
                    "加入「" + group.getName() + "」的申请未通过", inv.getGroupId());
        }
        invitationDAO.updateStatus(invitationId, "rejected");
    }

    public boolean isGroupMember(int groupId, int studentId) throws SQLException {
        return groupDAO.isMember(groupId, studentId);
    }

    private void requireGroupMember(int groupId, int studentId) throws SQLException {
        if (studentId <= 0) {
            return;
        }
        if (!groupDAO.isMember(groupId, studentId)) {
            throw new IllegalStateException("你不是该小组成员，无法执行此操作");
        }
    }
}
