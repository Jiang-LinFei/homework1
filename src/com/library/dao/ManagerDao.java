package com.library.dao;

import com.library.entity.Manager;
import com.library.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 普通管理员数据访问对象。
 * 提供登录查询，以及系统管理员对普通管理员账号的增删改查。
 */
public class ManagerDao {

    /** 按账号查询（用于登录校验、账号唯一性校验） */
    public Manager findByUsername(String username) {
        String sql = "SELECT * FROM manager WHERE username = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } catch (Exception e) {
            throw new RuntimeException("查询管理员失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 按主键查询 */
    public Manager findById(int id) {
        String sql = "SELECT * FROM manager WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } catch (Exception e) {
            throw new RuntimeException("查询管理员失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 查询全部普通管理员（按创建时间倒序） */
    public List<Manager> findAll() {
        String sql = "SELECT * FROM manager ORDER BY create_time DESC";
        List<Manager> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("查询管理员列表失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 新增普通管理员，返回受影响行数 */
    public int insert(Manager m) {
        String sql = "INSERT INTO manager(username, password, real_name, phone, status) VALUES(?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, m.getUsername());
            ps.setString(2, m.getPassword());
            ps.setString(3, m.getRealName());
            ps.setString(4, m.getPhone());
            ps.setInt(5, m.getStatus() == null ? 1 : m.getStatus());
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("新增管理员失败", e);
        } finally {
            DBUtil.close(null, ps, conn);
        }
    }

    /**
     * 更新普通管理员。
     * 当 password 为空时不更新密码字段（保持原密码）。
     */
    public int update(Manager m) {
        StringBuilder sql = new StringBuilder("UPDATE manager SET real_name=?, phone=?, status=?");
        boolean updatePwd = m.getPassword() != null && !m.getPassword().isEmpty();
        if (updatePwd) {
            sql.append(", password=?");
        }
        sql.append(" WHERE id=?");
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql.toString());
            int i = 1;
            ps.setString(i++, m.getRealName());
            ps.setString(i++, m.getPhone());
            ps.setInt(i++, m.getStatus() == null ? 1 : m.getStatus());
            if (updatePwd) {
                ps.setString(i++, m.getPassword());
            }
            ps.setInt(i, m.getId());
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("更新管理员失败", e);
        } finally {
            DBUtil.close(null, ps, conn);
        }
    }

    /** 删除普通管理员 */
    public int delete(int id) {
        String sql = "DELETE FROM manager WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("删除管理员失败", e);
        } finally {
            DBUtil.close(null, ps, conn);
        }
    }

    private Manager map(ResultSet rs) throws Exception {
        Manager m = new Manager();
        m.setId(rs.getInt("id"));
        m.setUsername(rs.getString("username"));
        m.setPassword(rs.getString("password"));
        m.setRealName(rs.getString("real_name"));
        m.setPhone(rs.getString("phone"));
        m.setStatus(rs.getInt("status"));
        m.setCreateTime(rs.getTimestamp("create_time"));
        return m;
    }
}
