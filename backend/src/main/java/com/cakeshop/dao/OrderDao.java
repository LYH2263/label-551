package com.cakeshop.dao;

import com.cakeshop.model.Order;
import com.cakeshop.model.OrderItem;
import com.cakeshop.util.DBUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单数据访问层
 */
public class OrderDao {

    /**
     * 生成订单号
     */
    public static String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        int random = (int) (Math.random() * 10000);
        return "ORD" + timestamp + String.format("%04d", random);
    }

    /**
     * 创建订单（包含订单项）
     */
    public int createOrder(Order order, List<OrderItem> items) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 插入订单
            String orderSql = "INSERT INTO orders (order_no, user_id, address_id, receiver_name, receiver_phone, receiver_address, total_amount, status, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, order.getOrderNo());
            ps.setInt(2, order.getUserId());
            if (order.getAddressId() != null) {
                ps.setInt(3, order.getAddressId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, order.getReceiverName());
            ps.setString(5, order.getReceiverPhone());
            ps.setString(6, order.getReceiverAddress());
            ps.setBigDecimal(7, order.getTotalAmount());
            ps.setString(8, order.getStatus() != null ? order.getStatus() : "pending");
            ps.setString(9, order.getRemark());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                conn.rollback();
                return -1;
            }

            ResultSet rs = ps.getGeneratedKeys();
            int orderId = -1;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
            rs.close();
            ps.close();

            // 插入订单项
            String itemSql = "INSERT INTO order_items (order_id, cake_id, cake_name, cake_image, price, quantity, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(itemSql);
            for (OrderItem item : items) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.getCakeId());
                ps.setString(3, item.getCakeName());
                ps.setString(4, item.getCakeImage());
                ps.setBigDecimal(5, item.getPrice());
                ps.setInt(6, item.getQuantity());
                ps.setBigDecimal(7, item.getSubtotal());
                ps.addBatch();
            }
            ps.executeBatch();

            conn.commit();
            return orderId;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.close(conn, ps);
        }
        return -1;
    }

    /**
     * 根据ID查询订单
     */
    public Order findById(Integer id) {
        String sql = "SELECT o.*, u.username FROM orders o " +
                    "LEFT JOIN users u ON o.user_id = u.id " +
                    "WHERE o.id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(findItemsByOrderId(id));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    /**
     * 根据订单号查询
     */
    public Order findByOrderNo(String orderNo) {
        String sql = "SELECT o.*, u.username FROM orders o " +
                    "LEFT JOIN users u ON o.user_id = u.id " +
                    "WHERE o.order_no = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, orderNo);
            rs = ps.executeQuery();

            if (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(findItemsByOrderId(order.getId()));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    /**
     * 查询用户订单
     */
    public List<Order> findByUserId(Integer userId, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT o.*, u.username FROM orders o " +
            "LEFT JOIN users u ON o.user_id = u.id " +
            "WHERE o.user_id = ?"
        );
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (status != null && !status.isEmpty()) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY o.created_at DESC");

        List<Order> orders = new ArrayList<>();
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
                Order order = mapResultSetToOrder(rs);
                order.setItems(findItemsByOrderId(order.getId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return orders;
    }

    /**
     * 管理员分页查询订单
     */
    public List<Order> findAllByPage(String status, String keyword, String startDate, String endDate, int page, int pageSize) {
        StringBuilder sql = new StringBuilder(
            "SELECT o.*, u.username FROM orders o " +
            "LEFT JOIN users u ON o.user_id = u.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isEmpty()) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (o.order_no LIKE ? OR o.receiver_name LIKE ? OR u.username LIKE ?)");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND DATE(o.created_at) >= ?");
            params.add(startDate.trim());
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND DATE(o.created_at) <= ?");
            params.add(endDate.trim());
        }

        sql.append(" ORDER BY o.created_at DESC");
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        List<Order> orders = new ArrayList<>();
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
                Order order = mapResultSetToOrder(rs);
                order.setItems(findItemsByOrderId(order.getId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return orders;
    }

    /**
     * 管理员统计订单数量
     */
    public int countAll(String status, String keyword, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM orders o " +
            "LEFT JOIN users u ON o.user_id = u.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isEmpty()) {
            sql.append(" AND o.status = ?");
            params.add(status);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (o.order_no LIKE ? OR o.receiver_name LIKE ? OR u.username LIKE ?)");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND DATE(o.created_at) >= ?");
            params.add(startDate.trim());
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND DATE(o.created_at) <= ?");
            params.add(endDate.trim());
        }

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
     * 更新订单状态
     */
    public boolean updateStatus(Integer id, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
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
     * 查询订单项
     */
    public List<OrderItem> findItemsByOrderId(Integer orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setCakeId(rs.getInt("cake_id"));
                item.setCakeName(rs.getString("cake_name"));
                item.setCakeImage(rs.getString("cake_image"));
                item.setPrice(rs.getBigDecimal("price"));
                item.setQuantity(rs.getInt("quantity"));
                item.setSubtotal(rs.getBigDecimal("subtotal"));
                item.setCreatedAt(rs.getTimestamp("created_at"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return items;
    }

    /**
     * ResultSet映射到Order对象
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setOrderNo(rs.getString("order_no"));
        order.setUserId(rs.getInt("user_id"));
        order.setAddressId(rs.getInt("address_id"));
        order.setReceiverName(rs.getString("receiver_name"));
        order.setReceiverPhone(rs.getString("receiver_phone"));
        order.setReceiverAddress(rs.getString("receiver_address"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setRemark(rs.getString("remark"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        order.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            order.setUsername(rs.getString("username"));
        } catch (SQLException e) {
            // username可能不存在
        }

        return order;
    }

    /**
     * 获取销售总额（已完成订单）
     */
    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status IN ('paid', 'shipping', 'completed')";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return BigDecimal.ZERO;
    }
}
