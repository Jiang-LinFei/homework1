package com.mis.team.dao;

import com.mis.team.model.GroupFile;
import com.mis.team.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {

    public List<GroupFile> findByGroupId(int groupId) throws SQLException {
        String sql = """
                SELECT f.id, f.group_id, f.uploader_id, f.file_name, f.file_path, f.uploaded_at, s.name AS uploader_name
                FROM group_files f
                JOIN students s ON f.uploader_id = s.id
                WHERE f.group_id = ?
                ORDER BY f.uploaded_at DESC
                """;
        List<GroupFile> list = new ArrayList<>();
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

    public void addFile(int groupId, int uploaderId, String fileName, String filePath) throws SQLException {
        String sql = "INSERT INTO group_files (group_id, uploader_id, file_name, file_path) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, uploaderId);
            ps.setString(3, fileName);
            ps.setString(4, filePath);
            ps.executeUpdate();
        }
    }

    private GroupFile mapRow(ResultSet rs) throws SQLException {
        GroupFile file = new GroupFile();
        file.setId(rs.getInt("id"));
        file.setGroupId(rs.getInt("group_id"));
        file.setUploaderId(rs.getInt("uploader_id"));
        file.setFileName(rs.getString("file_name"));
        file.setFilePath(rs.getString("file_path"));
        file.setUploadedAt(rs.getString("uploaded_at"));
        file.setUploaderName(rs.getString("uploader_name"));
        return file;
    }
}
