package com.library.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库工具类。
 * <p>
 * 职责：
 * <ul>
 *   <li>加载 classpath 下的 db.properties，初始化 JDBC 驱动；</li>
 *   <li>对外提供获取/关闭连接的方法；</li>
 *   <li>统一关闭 ResultSet / Statement / Connection，避免资源泄漏。</li>
 * </ul>
 * 本系统数据量较大且记录需长期保存，所有数据库操作都应通过本类获取连接，
 * 以保证连接参数（字符集、时区等）一致。
 */
public final class DBUtil {

    /** JDBC 连接参数，静态块中从配置文件加载一次 */
    private static String driver;
    private static String url;
    private static String username;
    private static String password;

    private DBUtil() {
        // 工具类禁止实例化
    }

    static {
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties props = new Properties();
            props.load(in);
            driver = props.getProperty("jdbc.driver");
            url = props.getProperty("jdbc.url");
            username = props.getProperty("jdbc.username");
            password = props.getProperty("jdbc.password");
            // 注册数据库驱动（只需一次）
            Class.forName(driver);
        } catch (Exception e) {
            // 配置错误属于致命问题，直接抛出便于在启动阶段发现
            throw new ExceptionInInitializerError("数据库配置加载失败：" + e.getMessage());
        }
    }

    /**
     * 获取一个新的数据库连接。
     * @return Connection 连接对象，使用完毕后必须关闭
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 静默关闭数据库资源（任意一个为 null 都会被忽略）。
     * 关闭顺序：ResultSet -> Statement -> Connection。
     */
    public static void close(ResultSet rs, PreparedStatement ps, Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) { }
        }
        if (ps != null) {
            try { ps.close(); } catch (SQLException ignored) { }
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) { }
        }
    }
}
