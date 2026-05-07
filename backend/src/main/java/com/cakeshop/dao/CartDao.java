package com.cakeshop.dao;

import com.cakeshop.model.CartItem;
import com.cakeshop.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车数据访问层
 */
public class CartDao {

    /**
     * 查询用户购物车
     */
    public List<CartItem> findByUserId(Integer userId) {
        String sql = "SELECT ci.*, c.name as cake_name, c.image as cake_image, c.price as cake_price, " +
                    "c.stock as cake_stock, c.status as cake_status " +
                    "FROM cart_items ci " +
                    "LEFT JOIN cakes c ON ci.cake_id = c.id " +
                    "WHERE ci.user_id = ? " +
                    "ORDER BY ci.created_at DESC";
        List<CartItem> items = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                items.add(mapResultSetToCartItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return items;
    }

    /**
     * 查询购物车商品
     */
    public CartItem findByUserIdAndCakeId(Integer userId, Integer cakeId) {
        String sql = "SELECT ci.*, c.name as cake_name, c.image as cake_image, c.price as cake_price, " +
                    "c.stock as cake_stock, c.status as cake_status " +
                    "FROM cart_items ci " +
                    "LEFT JOIN cakes c ON ci.cake_id = c.id " +
                    "WHERE ci.user_id = ? AND ci.cake_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, cakeId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToCartItem(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    /**
     * 根据ID查询
     */
    public CartItem findById(Integer id) {
        String sql = "SELECT ci.*, c.name as cake_name, c.image as cake_image, c.price as cake_price, " +
                    "c.stock as cake_stock, c.status as cake_status " +
                    "FROM cart_items ci " +
                    "LEFT JOIN cakes c ON ci.cake_id = c.id " +
                    "WHERE ci.id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToCartItem(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    /**
     * 添加到购物车
     */
    public int insert(CartItem item) {
        String sql = "INSERT INTO cart_items (user_id, cake_id, quantity) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, item.getUserId());
            ps.setInt(2, item.getCakeId());
            ps.setInt(3, item.getQuantity() != null ? item.getQuantity() : 1);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 1; // 更新成功
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return -1;
    }

    /**
     * 更新数量
     */
    public boolean updateQuantity(Integer id, Integer quantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, quantity);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    /**
     * 删除购物车项
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    /**
     * 清空用户购物车
     */
    public boolean clearByUserId(Integer userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    /**
     * 统计购物车数量
     */
    public int countByUserId(Integer userId) {
        String sql = "SELECT SUM(quantity) FROM cart_items WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
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
     * ResultSet映射到CartItem对象
     */
    private CartItem mapResultSetToCartItem(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setId(rs.getInt("id"));
        item.setUserId(rs.getInt("user_id"));
        item.setCakeId(rs.getInt("cake_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        item.setUpdatedAt(rs.getTimestamp("updated_at"));

        // 关联信息
        try {
            item.setCakeName(rs.getString("cake_name"));
            item.setCakeImage(rs.getString("cake_image"));
            item.setCakePrice(rs.getBigDecimal("cake_price"));
            item.setCakeStock(rs.getInt("cake_stock"));
            item.setCakeStatus(rs.getString("cake_status"));
        } catch (SQLException e) {
            // 关联字段可能不存在
        }

        return item;
    }
}
