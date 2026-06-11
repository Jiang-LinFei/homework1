package com.library.dao;

import com.library.entity.BookCategory;
import com.library.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书分类数据访问对象。
 */
public class BookCategoryDao {

    /** 查询全部分类 */
    public List<BookCategory> findAll() {
        String sql = "SELECT id, name FROM book_category ORDER BY id";
        List<BookCategory> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new BookCategory(rs.getInt("id"), rs.getString("name")));
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("查询图书分类失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 新增分类 */
    public int insert(String name) {
        String sql = "INSERT INTO book_category(name) VALUES(?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("新增图书分类失败", e);
        } finally {
            DBUtil.close(null, ps, conn);
        }
    }

    /** 删除分类 */
    public int delete(int id) {
        String sql = "DELETE FROM book_category WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("删除图书分类失败", e);
        } finally {
            DBUtil.close(null, ps, conn);
        }
    }
}
