package com.mis.team.dao;

import com.mis.team.model.StudyGroup;
import com.mis.team.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupDAO {

    public List<StudyGroup> findByStudentAndCourse(int studentId, int courseId) throws SQLException {
        String sql = """
                SELECT g.id, g.course_id, g.name, g.leader_id, s.name AS leader_name, c.name AS course_name,
                       (SELECT COUNT(*) FROM group_members gm WHERE gm.group_id = g.id) AS member_count
                FROM groups g
                JOIN students s ON g.leader_id = s.id
                JOIN courses c ON g.course_id = c.id
                JOIN group_members gm ON g.id = gm.group_id
                WHERE g.course_id = ? AND gm.student_id = ?
                ORDER BY g.id
                """;
        return queryGroups(sql, courseId, studentId);
    }

    public List<StudyGroup> findByStudentId(int studentId) throws SQLException {
        String sql = """
                SELECT g.id, g.course_id, g.name, g.leader_id, s.name AS leader_name, c.name AS course_name,
                       (SELECT COUNT(*) FROM group_members gm2 WHERE gm2.group_id = g.id) AS member_count
                FROM groups g
                JOIN students s ON g.leader_id = s.id
                JOIN courses c ON g.course_id = c.id
                JOIN group_members gm ON g.id = gm.group_id
                WHERE gm.student_id = ?
                ORDER BY g.id
                """;
        List<StudyGroup> list = new ArrayList<>();
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

    public List<StudyGroup> findByCourseId(int courseId) throws SQLException {
        String sql = """
                SELECT g.id, g.course_id, g.name, g.leader_id, s.name AS leader_name, c.name AS course_name,
                       (SELECT COUNT(*) FROM group_members gm WHERE gm.group_id = g.id) AS member_count
                FROM groups g
                JOIN students s ON g.leader_id = s.id
                JOIN courses c ON g.course_id = c.id
                WHERE g.course_id = ?
                ORDER BY g.id
                """;
        List<StudyGroup> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Optional<StudyGroup> findById(int id) throws SQLException {
        String sql = """
                SELECT g.id, g.course_id, g.name, g.leader_id, s.name AS leader_name, c.name AS course_name,
                       (SELECT COUNT(*) FROM group_members gm WHERE gm.group_id = g.id) AS member_count
                FROM groups g
                JOIN students s ON g.leader_id = s.id
                JOIN courses c ON g.course_id = c.id
                WHERE g.id = ?
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

    public int createGroup(int courseId, String name, int leaderId) throws SQLException {
        String sql = "INSERT INTO groups (course_id, name, leader_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, courseId);
            ps.setString(2, name);
            ps.setInt(3, leaderId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int groupId = rs.getInt(1);
                    addMember(groupId, leaderId, "leader");
                    return groupId;
                }
            }
        }
        throw new SQLException("创建小组失败");
    }

    public void addMember(int groupId, int studentId, String role) throws SQLException {
        String sql = "INSERT OR IGNORE INTO group_members (group_id, student_id, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, studentId);
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }

    public boolean isMember(int groupId, int studentId) throws SQLException {
        String sql = "SELECT 1 FROM group_members WHERE group_id = ? AND student_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countMembers(int groupId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM group_members WHERE group_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<StudyGroup> queryGroups(String sql, int courseId, int studentId) throws SQLException {
        List<StudyGroup> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private StudyGroup mapRow(ResultSet rs) throws SQLException {
        StudyGroup g = new StudyGroup();
        g.setId(rs.getInt("id"));
        g.setCourseId(rs.getInt("course_id"));
        g.setName(rs.getString("name"));
        g.setLeaderId(rs.getInt("leader_id"));
        g.setLeaderName(rs.getString("leader_name"));
        g.setCourseName(rs.getString("course_name"));
        g.setMemberCount(rs.getInt("member_count"));
        return g;
    }
}
