package com.library.dao;

import com.library.entity.Reader;
import com.library.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 读者数据访问对象。
 * 提供读者登记(新增)、修改、删除（注销）、按条件分页查询。
 */
public class ReaderDao {

    /** 按条件分页查询读者（关键字匹配证号/姓名/电话） */
    public List<Reader> findByPage(String keyword, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM reader WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendKeyword(sql, params, keyword);
        sql.append(" ORDER BY id DESC LIMIT ?, ?");
        params.add(offset);
        params.add(limit);

        List<Reader> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("分页查询读者失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 统计读者总数 */
    public long count(String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM reader WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendKeyword(sql, params, keyword);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (Exception e) {
            throw new RuntimeException("统计读者数量失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    private void appendKeyword(StringBuilder sql, List<Object> params, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (card_no LIKE ? OR name LIKE ? OR phone LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
    }

    /** 查询全部正常状态读者（用于借阅登记下拉选择） */
    public List<Reader> findAllActive() {
        String sql = "SELECT * FROM reader WHERE status = 1 ORDER BY id";
        List<Reader> list = new ArrayList<>();
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
            throw new RuntimeException("查询读者失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public Reader findById(int id) {
        String sql = "SELECT * FROM reader WHERE id = ?";
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
            throw new RuntimeException("查询读者失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public Reader findByCardNo(String cardNo) {
        String sql = "SELECT * FROM reader WHERE card_no = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, cardNo);
            rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } catch (Exception e) {
            throw new RuntimeException("查询读者失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 读者登记（新增） */
    public int insert(Reader r) {
        String sql = "INSERT INTO reader(card_no, name, gender, phone, reader_type, max_borrow, status) " +
                "VALUES(?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, r.getCardNo());
            ps.setString(2, r.getName());
            ps.setString(3, r.getGender());
            ps.setString(4, r.getPhone());
            ps.setString(5, r.getReaderType());
            ps.setInt(6, r.getMaxBorrow() == null ? 5 : r.getMaxBorrow());
            ps.setInt(7, r.getStatus() == null ? 1 : r.getStatus());
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("读者登记失败", e);
        } finally {
            DBUtil.close(null, ps, conn);
        }
    }

    public int update(Reader r) {
        String sql = "UPDATE reader SET name=?, gender=?, phone=?, reader_type=?, max_borrow=?, status=? WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, r.getName());
            ps.setString(2, r.getGender());
            ps.setString(3, r.getPhone());
            ps.setString(4, r.getReaderType());
            ps.setInt(5, r.getMaxBorrow() == null ? 5 : r.getMaxBorrow());
            ps.setInt(6, r.getStatus() == null ? 1 : r.getStatus());
            ps.setInt(7, r.getId());
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("更新读者失败", e);
        } finally {
            DBUtil.close(null, ps, conn);
        }
    }

    public int delete(int id) {
        String sql = "DELETE FROM reader WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("删除读者失败", e);
        } finally {
            DBUtil.close(null, ps, conn);
        }
    }

    private Reader map(ResultSet rs) throws Exception {
        Reader r = new Reader();
        r.setId(rs.getInt("id"));
        r.setCardNo(rs.getString("card_no"));
        r.setName(rs.getString("name"));
        r.setGender(rs.getString("gender"));
        r.setPhone(rs.getString("phone"));
        r.setReaderType(rs.getString("reader_type"));
        r.setMaxBorrow(rs.getInt("max_borrow"));
        r.setRegisterTime(rs.getTimestamp("register_time"));
        r.setStatus(rs.getInt("status"));
        return r;
    }
}
