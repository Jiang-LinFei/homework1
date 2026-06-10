package com.mis.team.dao;

import com.mis.team.model.Invitation;
import com.mis.team.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvitationDAO {

    public int create(Invitation invitation) throws SQLException {
        String sql = """
                INSERT INTO invitations (group_id, course_id, inviter_id, invitee_id, type, status, message)
                VALUES (?, ?, ?, ?, ?, 'pending', ?)
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (invitation.getGroupId() != null) {
                ps.setInt(1, invitation.getGroupId());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            if (invitation.getCourseId() != null) {
                ps.setInt(2, invitation.getCourseId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, invitation.getInviterId());
            ps.setInt(4, invitation.getInviteeId());
            ps.setString(5, invitation.getType());
            ps.setString(6, invitation.getMessage());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("创建邀请失败");
    }

    public List<Invitation> findPendingByInvitee(int inviteeId) throws SQLException {
        String sql = """
                SELECT i.*, s1.name AS inviter_name, s2.name AS invitee_name,
                       g.name AS group_name, c.name AS course_name
                FROM invitations i
                JOIN students s1 ON i.inviter_id = s1.id
                JOIN students s2 ON i.invitee_id = s2.id
                LEFT JOIN groups g ON i.group_id = g.id
                LEFT JOIN courses c ON i.course_id = c.id
                WHERE i.invitee_id = ? AND i.status = 'pending'
                ORDER BY i.created_at DESC
                """;
        return queryList(sql, inviteeId);
    }

    public List<Invitation> findPendingForLeader(int leaderId) throws SQLException {
        String sql = """
                SELECT i.*, s1.name AS inviter_name, s2.name AS invitee_name,
                       g.name AS group_name, c.name AS course_name
                FROM invitations i
                JOIN students s1 ON i.inviter_id = s1.id
                JOIN students s2 ON i.invitee_id = s2.id
                LEFT JOIN groups g ON i.group_id = g.id
                LEFT JOIN courses c ON i.course_id = c.id
                JOIN groups grp ON i.group_id = grp.id
                WHERE grp.leader_id = ? AND i.type = 'join_request' AND i.status = 'pending'
                ORDER BY i.created_at DESC
                """;
        return queryList(sql, leaderId);
    }

    public Optional<Invitation> findById(int id) throws SQLException {
        String sql = """
                SELECT i.*, s1.name AS inviter_name, s2.name AS invitee_name,
                       g.name AS group_name, c.name AS course_name
                FROM invitations i
                JOIN students s1 ON i.inviter_id = s1.id
                JOIN students s2 ON i.invitee_id = s2.id
                LEFT JOIN groups g ON i.group_id = g.id
                LEFT JOIN courses c ON i.course_id = c.id
                WHERE i.id = ?
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE invitations SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean hasPendingJoinRequest(int groupId, int studentId) throws SQLException {
        String sql = "SELECT 1 FROM invitations WHERE group_id = ? AND inviter_id = ? AND type = 'join_request' AND status = 'pending'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private List<Invitation> queryList(String sql, int param) throws SQLException {
        List<Invitation> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Invitation mapRow(ResultSet rs) throws SQLException {
        Invitation inv = new Invitation();
        inv.setId(rs.getInt("id"));
        int groupId = rs.getInt("group_id");
        if (!rs.wasNull()) {
            inv.setGroupId(groupId);
        }
        int courseId = rs.getInt("course_id");
        if (!rs.wasNull()) {
            inv.setCourseId(courseId);
        }
        inv.setInviterId(rs.getInt("inviter_id"));
        inv.setInviteeId(rs.getInt("invitee_id"));
        inv.setType(rs.getString("type"));
        inv.setStatus(rs.getString("status"));
        inv.setMessage(rs.getString("message"));
        inv.setCreatedAt(rs.getString("created_at"));
        inv.setInviterName(rs.getString("inviter_name"));
        inv.setInviteeName(rs.getString("invitee_name"));
        inv.setGroupName(rs.getString("group_name"));
        inv.setCourseName(rs.getString("course_name"));
        return inv;
    }
}
