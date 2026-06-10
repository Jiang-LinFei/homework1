package com.mis.team.dao;

import com.mis.team.model.GroupTask;
import com.mis.team.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public List<GroupTask> findByGroupId(int groupId) throws SQLException {
        String sql = """
                SELECT t.id, t.group_id, t.title, t.description, t.status, t.assignee_id, s.name AS assignee_name
                FROM tasks t
                LEFT JOIN students s ON t.assignee_id = s.id
                WHERE t.group_id = ?
                ORDER BY t.id
                """;
        List<GroupTask> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public void addTask(int groupId, String title, String description, String status, Integer assigneeId) throws SQLException {
        String sql = "INSERT INTO tasks (group_id, title, description, status, assignee_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setString(4, status);
            if (assigneeId == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, assigneeId);
            }
            ps.executeUpdate();
        }
    }

    public void updateStatus(int taskId, String status) throws SQLException {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, taskId);
            ps.executeUpdate();
        }
    }

    private GroupTask mapRow(ResultSet rs) throws SQLException {
        GroupTask task = new GroupTask();
        task.setId(rs.getInt("id"));
        task.setGroupId(rs.getInt("group_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStatus(rs.getString("status"));
        int assigneeId = rs.getInt("assignee_id");
        if (!rs.wasNull()) {
            task.setAssigneeId(assigneeId);
            task.setAssigneeName(rs.getString("assignee_name"));
        }
        return task;
    }
}
