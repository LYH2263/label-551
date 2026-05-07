package com.cakeshop.dao;

import com.cakeshop.model.User;
import com.cakeshop.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问层
 * 使用JDBC原生实现
 */
public class UserDao {

    /**
     * 根据手机号查询用户
     */
    public User findByPhone(String phone) {
        String sql = "SELECT * FROM users WHERE phone = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, phone);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    /**
     * 根据ID查询用户
     */
    public User findById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    /**
     * 新增用户
     */
    public int insert(User user) {
        String sql = "INSERT INTO users (phone, password, username, role) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getPhone());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getRole() != null ? user.getRole() : "user");

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return -1;
    }

    /**
     * 更新用户信息
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, phone = ?, role = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPhone());
            ps.setString(3, user.getRole());
            ps.setInt(4, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    /**
     * 删除用户（先清理关联数据）
     */
    public boolean delete(Integer id) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 删除用户的购物车
            ps = conn.prepareStatement("DELETE FROM cart_items WHERE user_id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            // 2. 删除用户的收货地址
            ps = conn.prepareStatement("DELETE FROM addresses WHERE user_id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            // 3. 删除用户的订单项（通过订单关联）
            ps = conn.prepareStatement("DELETE oi FROM order_items oi INNER JOIN orders o ON oi.order_id = o.id WHERE o.user_id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            // 4. 删除用户的订单
            ps = conn.prepareStatement("DELETE FROM orders WHERE user_id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            // 5. 删除用户
            ps = conn.prepareStatement("DELETE FROM users WHERE id = ?");
            ps.setInt(1, id);
            int result = ps.executeUpdate();

            conn.commit();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.close(conn, ps);
        }
        return false;
    }

    /**
     * 更新密码
     */
    public boolean updatePassword(Integer userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    /**
     * 查询所有用户（管理员用）
     */
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return users;
    }

    /**
     * 统计用户数量
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM users";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    /**
     * ResultSet映射到User对象
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setPhone(rs.getString("phone"));
        user.setPassword(rs.getString("password"));
        user.setUsername(rs.getString("username"));
        user.setRole(rs.getString("role"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}
