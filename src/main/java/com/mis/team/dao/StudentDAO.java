package com.mis.team.dao;

import com.mis.team.model.Student;
import com.mis.team.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDAO {

    public Optional<Student> login(String studentNo, String password) throws SQLException {
        String sql = "SELECT id, student_no, password, name FROM students WHERE student_no = ? AND password = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentNo);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Student> findByStudentNo(String studentNo) throws SQLException {
        String sql = "SELECT id, student_no, password, name FROM students WHERE student_no = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Student> findById(int id) throws SQLException {
        String sql = "SELECT id, student_no, password, name FROM students WHERE id = ?";
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

    public List<Student> findByGroupId(int groupId) throws SQLException {
        String sql = """
                SELECT s.id, s.student_no, s.password, s.name
                FROM students s
                JOIN group_members gm ON s.id = gm.student_id
                WHERE gm.group_id = ?
                ORDER BY gm.role DESC, s.id
                """;
        List<Student> list = new ArrayList<>();
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

    public List<String> findMemberRoles(int groupId) throws SQLException {
        String sql = "SELECT student_id, role FROM group_members WHERE group_id = ?";
        List<String> roles = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getInt("student_id") + ":" + rs.getString("role"));
                }
            }
        }
        return roles;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("student_no"),
                rs.getString("password"),
                rs.getString("name")
        );
    }
}
