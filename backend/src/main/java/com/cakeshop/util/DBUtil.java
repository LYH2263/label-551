package com.cakeshop.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库连接工具类
 * 使用JDBC原生实现
 */
public class DBUtil {

    private static String url;
    private static String username;
    private static String password;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 从环境变量读取配置
            String host = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
            String port = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "3306";
            String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "cakeshop";
            username = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
            password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "root123";

            url = String.format(
                "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
                host, port, dbName
            );

            System.out.println("Database URL: " + url);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("MySQL驱动加载失败", e);
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 关闭资源
     */
    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭资源
     */
    public static void close(Connection conn, PreparedStatement ps) {
        close(conn, ps, null);
    }

    /**
     * 测试数据库连接
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            close(conn, null);
        }
    }
}
