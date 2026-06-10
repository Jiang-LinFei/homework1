package com.mis.team.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseUtil {
    private static final String DB_FILE = System.getProperty("team.db.path", "data/team_system.db");
    private static Connection connection;
    private static boolean schemaInitialized = false;

    private DatabaseUtil() {
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            Path dataDir = Path.of("data");
            try {
                Files.createDirectories(dataDir);
            } catch (IOException e) {
                throw new SQLException("无法创建数据目录", e);
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            connection.setAutoCommit(true);
            if (!schemaInitialized) {
                initDatabase();
                schemaInitialized = true;
            }
        }
        return connection;
    }

    /** 仅测试时重置，避免重复执行 init.sql */
    public static void resetForTesting() {
        close();
        schemaInitialized = false;
    }

    private static void initDatabase() throws SQLException {
        if (isAlreadyInitialized()) {
            return;
        }
        Path sqlFile = Path.of("database", "init.sql");
        if (!Files.exists(sqlFile)) {
            sqlFile = Path.of(System.getProperty("user.dir"), "database", "init.sql");
        }
        if (!Files.exists(sqlFile)) {
            throw new SQLException("找不到 database/init.sql");
        }
        try {
            String sql = Files.readString(sqlFile, StandardCharsets.UTF_8);
            executeScript(sql);
        } catch (IOException e) {
            throw new SQLException("读取初始化脚本失败", e);
        }
    }

    /**
     * 判断数据库是否已初始化过。脚本中的示例消息/任务/通知没有唯一约束，
     * 若每次启动都重新执行 init.sql，这些种子数据会被反复插入而不断累积。
     * 因此只在数据库为全新（students 表不存在或为空）时才执行初始化脚本。
     */
    private static boolean isAlreadyInitialized() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private static void executeScript(String sql) throws SQLException {
        StringBuilder current = new StringBuilder();
        try (Statement stmt = getConnection().createStatement()) {
            for (String line : sql.split("\\R")) {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("--") || trimmedLine.isEmpty()) {
                    continue;
                }
                current.append(line).append('\n');
                if (trimmedLine.endsWith(";")) {
                    String statement = current.toString().trim();
                    if (statement.endsWith(";")) {
                        statement = statement.substring(0, statement.length() - 1);
                    }
                    if (!statement.isEmpty()) {
                        stmt.execute(statement);
                    }
                    current.setLength(0);
                }
            }
        }
    }

    public static void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
            connection = null;
        }
    }
}
