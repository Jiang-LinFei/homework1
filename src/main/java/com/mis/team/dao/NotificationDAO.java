package com.mis.team.dao;

import com.mis.team.model.Notification;
import com.mis.team.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public List<Notification> findByStudentId(int studentId) throws SQLException {
        String sql = """
                SELECT id, student_id, title, content, is_read, related_id, created_at
                FROM notifications
                WHERE student_id = ?
                ORDER BY created_at DESC, id DESC
                """;
        List<Notification> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public void add(int studentId, String title, String content, Integer relatedId) throws SQLException {
        String sql = "INSERT INTO notifications (student_id, title, content, related_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, title);
            ps.setString(3, content);
            if (relatedId == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, relatedId);
            }
            ps.executeUpdate();
        }
    }

    public boolean markRead(int id) throws SQLException {
        String sql = "UPDATE notifications SET is_read = 1 WHERE id = ? AND is_read = 0";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int countUnread(int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE student_id = ? AND is_read = 0";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setStudentId(rs.getInt("student_id"));
        n.setTitle(rs.getString("title"));
        n.setContent(rs.getString("content"));
        n.setRead(rs.getInt("is_read") == 1);
        int relatedId = rs.getInt("related_id");
        if (!rs.wasNull()) {
            n.setRelatedId(relatedId);
        }
        n.setCreatedAt(rs.getString("created_at"));
        return n;
    }
}
