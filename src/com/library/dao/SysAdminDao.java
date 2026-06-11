package com.library.dao;

import com.library.entity.SysAdmin;
import com.library.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 系统管理员数据访问对象。
 */
public class SysAdminDao {

    /**
     * 按账号查询系统管理员（用于登录校验）。
     * @param username 账号
     * @return 命中返回实体，否则返回 null
     */
    public SysAdmin findByUsername(String username) {
        String sql = "SELECT id, username, password, real_name, create_time FROM sys_admin WHERE username = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("查询系统管理员失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 将结果集当前行映射为实体 */
    private SysAdmin map(ResultSet rs) throws Exception {
        SysAdmin admin = new SysAdmin();
        admin.setId(rs.getInt("id"));
        admin.setUsername(rs.getString("username"));
        admin.setPassword(rs.getString("password"));
        admin.setRealName(rs.getString("real_name"));
        admin.setCreateTime(rs.getTimestamp("create_time"));
        return admin;
    }
}
