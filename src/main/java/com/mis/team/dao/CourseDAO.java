package com.mis.team.dao;

import com.mis.team.model.Course;
import com.mis.team.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourseDAO {

    public List<Course> findByStudentId(int studentId) throws SQLException {
        String sql = """
                SELECT c.id, c.name, c.description, c.teacher
                FROM courses c
                JOIN course_students cs ON c.id = cs.course_id
                WHERE cs.student_id = ?
                ORDER BY c.id
                """;
        List<Course> list = new ArrayList<>();
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

    public Optional<Course> findById(int id) throws SQLException {
        String sql = "SELECT id, name, description, teacher FROM courses WHERE id = ?";
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

    private Course mapRow(ResultSet rs) throws SQLException {
        return new Course(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("teacher")
        );
    }
}
