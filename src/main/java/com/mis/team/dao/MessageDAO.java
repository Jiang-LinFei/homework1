package com.mis.team.dao;

import com.mis.team.model.ChatMessage;
import com.mis.team.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public List<ChatMessage> findByGroupId(int groupId, int currentStudentId) throws SQLException {
        String sql = """
                SELECT m.id, m.group_id, m.sender_id, s.name AS sender_name, m.content, m.created_at
                FROM messages m
                JOIN students s ON m.sender_id = s.id
                WHERE m.group_id = ?
                ORDER BY m.created_at ASC, m.id ASC
                """;
        List<ChatMessage> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatMessage msg = new ChatMessage();
                    msg.setId(rs.getInt("id"));
                    msg.setGroupId(rs.getInt("group_id"));
                    msg.setSenderId(rs.getInt("sender_id"));
                    msg.setSenderName(rs.getString("sender_name"));
                    msg.setContent(rs.getString("content"));
                    msg.setCreatedAt(rs.getString("created_at"));
                    msg.setOwn(rs.getInt("sender_id") == currentStudentId);
                    list.add(msg);
                }
            }
        }
        return list;
    }

    public void sendMessage(int groupId, int senderId, String content) throws SQLException {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO messages (group_id, sender_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, senderId);
            ps.setString(3, content);
            ps.executeUpdate();
        }
    }
}
