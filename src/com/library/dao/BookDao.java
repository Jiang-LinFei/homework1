package com.library.dao;

import com.library.entity.Book;
import com.library.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书数据访问对象。
 * 提供图书入库(新增)、修改、删除、按条件分页查询，以及借/还时的库存调整。
 */
public class BookDao {

    /**
     * 按条件分页查询图书。
     * @param keyword    关键字(书名/作者/ISBN 模糊匹配)，可为空
     * @param categoryId 分类id，<=0 表示不限
     * @param offset     起始行
     * @param limit      每页条数
     * @return 当前页图书列表（含分类名称）
     */
    public List<Book> findByPage(String keyword, int categoryId, int offset, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT b.*, c.name AS category_name FROM book b " +
                "LEFT JOIN book_category c ON b.category_id = c.id WHERE 1=1");
        List<Object> params = buildConditionParams(sql, keyword, categoryId);
        sql.append(" ORDER BY b.id DESC LIMIT ?, ?");
        params.add(offset);
        params.add(limit);

        List<Book> list = new ArrayList<>();
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
            throw new RuntimeException("分页查询图书失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 统计符合条件的图书总数（配合分页） */
    public long count(String keyword, int categoryId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM book b WHERE 1=1");
        List<Object> params = buildConditionParams(sql, keyword, categoryId);
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
            throw new RuntimeException("统计图书数量失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 把查询条件拼到 sql 上，并返回对应参数列表（被分页查询和统计复用） */
    private List<Object> buildConditionParams(StringBuilder sql, String keyword, int categoryId) {
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (b.title LIKE ? OR b.author LIKE ? OR b.isbn LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (categoryId > 0) {
            sql.append(" AND b.category_id = ?");
            params.add(categoryId);
        }
        return params;
    }

    /** 按主键查询单本图书 */
    public Book findById(int id) {
        String sql = "SELECT b.*, c.name AS category_name FROM book b " +
                "LEFT JOIN book_category c ON b.category_id = c.id WHERE b.id = ?";
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
            throw new RuntimeException("查询图书失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 图书入库（新增）。新书可借数量默认等于馆藏总数 */
    public int insert(Book b) {
        String sql = "INSERT INTO book(isbn, title, author, publisher, category_id, location, " +
                "total_count, available_count, price) VALUES(?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, b.getIsbn());
            ps.setString(2, b.getTitle());
            ps.setString(3, b.getAuthor());
            ps.setString(4, b.getPublisher());
            setIntOrNull(ps, 5, b.getCategoryId());
            ps.setString(6, b.getLocation());
            ps.setInt(7, b.getTotalCount());
            ps.setInt(8, b.getAvailableCount());
            ps.setBigDecimal(9, b.getPrice());
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("图书入库失败", e);
        } finally {
            DBUtil.close(null, ps, conn);
        }
    }

    /** 更新图书基本信息 */
    public int update(Book b) {
        String sql = "UPDATE book SET isbn=?, title=?, author=?, publisher=?, category_id=?, " +
                "location=?, total_count=?, available_count=?, price=? WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, b.getIsbn());
            ps.setString(2, b.getTitle());
            ps.setString(3, b.getAuthor());
            ps.setString(4, b.getPublisher());
            setIntOrNull(ps, 5, b.getCategoryId());
            ps.setString(6, b.getLocation());
            ps.setInt(7, b.getTotalCount());
            ps.setInt(8, b.getAvailableCount());
            ps.setBigDecimal(9, b.getPrice());
            ps.setInt(10, b.getId());
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("更新图书失败", e);
        } finally {
            DBUtil.close(null, ps, conn);
        }
    }

    /** 删除图书 */
    public int delete(int id) {
        String sql = "DELETE FROM book WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("删除图书失败", e);
        } finally {
            DBUtil.close(null, ps, conn);
        }
    }

    /**
     * 调整可借数量（借出传 -1，归还传 +1）。
     * 在同一事务的连接上执行，由调用方(借阅/归还业务)控制提交，保证库存与借阅记录一致。
     * @param conn  外部传入的连接（事务）
     * @param bookId 图书id
     * @param delta  增量
     */
    public int changeAvailable(Connection conn, int bookId, int delta) throws Exception {
        String sql = "UPDATE book SET available_count = available_count + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, bookId);
            return ps.executeUpdate();
        }
    }

    /** category_id 允许为空 */
    private void setIntOrNull(PreparedStatement ps, int index, Integer value) throws Exception {
        if (value == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private Book map(ResultSet rs) throws Exception {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setPublisher(rs.getString("publisher"));
        int cid = rs.getInt("category_id");
        b.setCategoryId(rs.wasNull() ? null : cid);
        b.setLocation(rs.getString("location"));
        b.setTotalCount(rs.getInt("total_count"));
        b.setAvailableCount(rs.getInt("available_count"));
        b.setPrice(rs.getBigDecimal("price"));
        b.setCreateTime(rs.getTimestamp("create_time"));
        b.setStatus(rs.getInt("status"));
        // category_name 仅在带 JOIN 的查询里存在
        try {
            b.setCategoryName(rs.getString("category_name"));
        } catch (Exception ignored) {
        }
        return b;
    }
}
