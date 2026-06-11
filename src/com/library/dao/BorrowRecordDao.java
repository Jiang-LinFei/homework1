package com.library.dao;

import com.library.entity.BorrowRecord;
import com.library.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 借阅记录数据访问对象。
 * <p>
 * 借出/归还涉及"借阅记录"与"图书库存"两张表的同步修改，
 * 因此采用数据库事务保证一致性：要么都成功，要么都回滚。
 */
public class BorrowRecordDao {

    private final BookDao bookDao = new BookDao();

    /**
     * 借阅登记（事务）。
     * 步骤：校验库存与读者可借额度 -> 插入借阅记录 -> 图书可借数量 -1。
     * @return 成功提示为 null；失败返回错误信息字符串
     */
    public String borrow(int bookId, int readerId, int operatorId, int borrowDays) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // 开启事务

            // 1. 锁定图书行，校验可借数量
            int available = lockBookAvailable(conn, bookId);
            if (available < 0) {
                conn.rollback();
                return "图书不存在";
            }
            if (available <= 0) {
                conn.rollback();
                return "该图书已无可借库存";
            }

            // 2. 校验读者是否超过最大可借数量
            int maxBorrow = getReaderMaxBorrow(conn, readerId);
            if (maxBorrow < 0) {
                conn.rollback();
                return "读者不存在或已注销";
            }
            int current = countActiveByReader(conn, readerId);
            if (current >= maxBorrow) {
                conn.rollback();
                return "该读者借阅数量已达上限(" + maxBorrow + "本)";
            }

            // 3. 插入借阅记录
            Date now = new Date();
            Date due = new Date(now.getTime() + (long) borrowDays * 24 * 3600 * 1000);
            String insert = "INSERT INTO borrow_record(book_id, reader_id, borrow_time, due_time, status, operator_id) " +
                    "VALUES(?,?,?,?,1,?)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, bookId);
                ps.setInt(2, readerId);
                ps.setTimestamp(3, new Timestamp(now.getTime()));
                ps.setTimestamp(4, new Timestamp(due.getTime()));
                ps.setInt(5, operatorId);
                ps.executeUpdate();
            }

            // 4. 图书可借数量 -1
            bookDao.changeAvailable(conn, bookId, -1);

            conn.commit(); // 全部成功，提交
            return null;
        } catch (Exception e) {
            rollbackQuietly(conn);
            throw new RuntimeException("借阅登记失败", e);
        } finally {
            restoreAndClose(conn);
        }
    }

    /**
     * 归还登记（事务）。
     * 步骤：将记录 status 置为已归还、写入归还时间 -> 图书可借数量 +1。
     * @return 成功为 null；失败返回错误信息
     */
    public String returnBook(int recordId) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 查记录，确认还在借出中
            int bookId;
            String query = "SELECT book_id, status FROM borrow_record WHERE id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, recordId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return "借阅记录不存在";
                    }
                    if (rs.getInt("status") == 2) {
                        conn.rollback();
                        return "该记录已归还，请勿重复操作";
                    }
                    bookId = rs.getInt("book_id");
                }
            }

            // 更新归还信息
            String update = "UPDATE borrow_record SET status = 2, return_time = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                ps.setInt(2, recordId);
                ps.executeUpdate();
            }

            // 图书可借数量 +1
            bookDao.changeAvailable(conn, bookId, 1);

            conn.commit();
            return null;
        } catch (Exception e) {
            rollbackQuietly(conn);
            throw new RuntimeException("归还登记失败", e);
        } finally {
            restoreAndClose(conn);
        }
    }

    /**
     * 分页查询借阅记录。
     * @param keyword     关键字(书名/读者姓名/借阅证号)
     * @param status      0=全部 1=借出中 2=已归还
     * @param onlyOverdue 仅看逾期(借出中且已过应还日期)
     */
    public List<BorrowRecord> findByPage(String keyword, int status, boolean onlyOverdue, int offset, int limit) {
        StringBuilder sql = new StringBuilder(baseSelect());
        List<Object> params = new ArrayList<>();
        appendConditions(sql, params, keyword, status, onlyOverdue);
        sql.append(" ORDER BY r.id DESC LIMIT ?, ?");
        params.add(offset);
        params.add(limit);

        List<BorrowRecord> list = new ArrayList<>();
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
            throw new RuntimeException("查询借阅记录失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public long count(String keyword, int status, boolean onlyOverdue) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM borrow_record r " +
                "LEFT JOIN book b ON r.book_id=b.id LEFT JOIN reader rd ON r.reader_id=rd.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendConditions(sql, params, keyword, status, onlyOverdue);
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
            throw new RuntimeException("统计借阅记录失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 统计当前逾期数量（用于首页/逾期提醒红点） */
    public long countOverdue() {
        return count(null, 0, true);
    }

    private String baseSelect() {
        return "SELECT r.*, b.title AS book_title, rd.name AS reader_name, rd.card_no AS reader_card_no " +
                "FROM borrow_record r " +
                "LEFT JOIN book b ON r.book_id = b.id " +
                "LEFT JOIN reader rd ON r.reader_id = rd.id WHERE 1=1";
    }

    private void appendConditions(StringBuilder sql, List<Object> params,
                                  String keyword, int status, boolean onlyOverdue) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (b.title LIKE ? OR rd.name LIKE ? OR rd.card_no LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (status == 1 || status == 2) {
            sql.append(" AND r.status = ?");
            params.add(status);
        }
        if (onlyOverdue) {
            // 逾期：借出中(status=1) 且 应还时间 < 当前时间
            sql.append(" AND r.status = 1 AND r.due_time < NOW()");
        }
    }

    // ---------------- 事务内部辅助方法 ----------------

    /** 锁定图书行并返回可借数量；不存在返回 -1 */
    private int lockBookAvailable(Connection conn, int bookId) throws Exception {
        String sql = "SELECT available_count FROM book WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    /** 取读者最大可借数量；不存在或注销返回 -1 */
    private int getReaderMaxBorrow(Connection conn, int readerId) throws Exception {
        String sql = "SELECT max_borrow FROM reader WHERE id = ? AND status = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    /** 统计读者当前借出中(未归还)的数量 */
    private int countActiveByReader(Connection conn, int readerId) throws Exception {
        String sql = "SELECT COUNT(*) FROM borrow_record WHERE reader_id = ? AND status = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (Exception ignored) { }
        }
    }

    private void restoreAndClose(Connection conn) {
        if (conn != null) {
            try { conn.setAutoCommit(true); } catch (Exception ignored) { }
            try { conn.close(); } catch (Exception ignored) { }
        }
    }

    private BorrowRecord map(ResultSet rs) throws Exception {
        BorrowRecord r = new BorrowRecord();
        r.setId(rs.getInt("id"));
        r.setBookId(rs.getInt("book_id"));
        r.setReaderId(rs.getInt("reader_id"));
        r.setBorrowTime(rs.getTimestamp("borrow_time"));
        r.setDueTime(rs.getTimestamp("due_time"));
        r.setReturnTime(rs.getTimestamp("return_time"));
        r.setStatus(rs.getInt("status"));
        int op = rs.getInt("operator_id");
        r.setOperatorId(rs.wasNull() ? null : op);
        r.setRemark(rs.getString("remark"));
        r.setBookTitle(rs.getString("book_title"));
        r.setReaderName(rs.getString("reader_name"));
        r.setReaderCardNo(rs.getString("reader_card_no"));
        // 运行时计算是否逾期：借出中且应还时间已过
        boolean overdue = r.getStatus() != null && r.getStatus() == 1
                && r.getDueTime() != null && r.getDueTime().before(new Date());
        r.setOverdue(overdue);
        return r;
    }
}
