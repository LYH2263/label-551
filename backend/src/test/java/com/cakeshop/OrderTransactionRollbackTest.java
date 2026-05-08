package com.cakeshop;

import com.cakeshop.util.DBUtil;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderTransactionRollbackTest {

    private Integer testUserId;
    private Integer cakeAId;
    private Integer cakeBId;
    private Integer cakeCId;
    private Integer initialOrderCount;
    private Integer initialOrderItemCount;
    private boolean connectionClosed = false;

    @BeforeEach
    void setUp() throws Exception {
        assertTrue(DBUtil.testConnection(), "数据库连接失败，请检查数据库配置");
        prepareTestData();
    }

    @AfterEach
    void tearDown() throws Exception {
        cleanUpTestData();
    }

    private void prepareTestData() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(
                    "INSERT INTO users (phone, password, username, role) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, "19999999999");
            ps.setString(2, "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92");
            ps.setString(3, "集成测试用户");
            ps.setString(4, "user");
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                testUserId = rs.getInt(1);
            }
            rs.close();
            ps.close();

            ps = conn.prepareStatement(
                    "INSERT INTO cakes (name, category_id, price, stock, status) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, "测试蛋糕A");
            ps.setInt(2, 1);
            ps.setBigDecimal(3, new BigDecimal("100.00"));
            ps.setInt(4, 50);
            ps.setString(5, "on");
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                cakeAId = rs.getInt(1);
            }
            rs.close();

            ps.setString(1, "测试蛋糕B");
            ps.setInt(2, 1);
            ps.setBigDecimal(3, new BigDecimal("200.00"));
            ps.setInt(4, 10);
            ps.setString(5, "on");
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                cakeBId = rs.getInt(1);
            }
            rs.close();

            ps.setString(1, "测试蛋糕C");
            ps.setInt(2, 1);
            ps.setBigDecimal(3, new BigDecimal("300.00"));
            ps.setInt(4, 3);
            ps.setString(5, "on");
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                cakeCId = rs.getInt(1);
            }
            rs.close();
            ps.close();

            ps = conn.prepareStatement(
                    "INSERT INTO cart_items (user_id, cake_id, quantity) VALUES (?, ?, ?)"
            );
            ps.setInt(1, testUserId);
            ps.setInt(2, cakeAId);
            ps.setInt(3, 5);
            ps.executeUpdate();

            ps.setInt(1, testUserId);
            ps.setInt(2, cakeBId);
            ps.setInt(3, 10);
            ps.executeUpdate();

            ps.setInt(1, testUserId);
            ps.setInt(2, cakeCId);
            ps.setInt(3, 8);
            ps.executeUpdate();

            conn.commit();

            try (Connection countConn = DBUtil.getConnection()) {
                try (Statement stmt = countConn.createStatement()) {
                    rs = stmt.executeQuery("SELECT COUNT(*) FROM orders");
                    if (rs.next()) {
                        initialOrderCount = rs.getInt(1);
                    }
                    rs.close();

                    rs = stmt.executeQuery("SELECT COUNT(*) FROM order_items");
                    if (rs.next()) {
                        initialOrderItemCount = rs.getInt(1);
                    }
                    rs.close();
                }
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    private void cleanUpTestData() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(true);

            if (testUserId != null) {
                ps = conn.prepareStatement("DELETE FROM cart_items WHERE user_id = ?");
                ps.setInt(1, testUserId);
                ps.executeUpdate();
                ps.close();

                ps = conn.prepareStatement("DELETE FROM users WHERE id = ?");
                ps.setInt(1, testUserId);
                ps.executeUpdate();
                ps.close();
            }

            List<Integer> cakeIds = new ArrayList<>();
            if (cakeAId != null) cakeIds.add(cakeAId);
            if (cakeBId != null) cakeIds.add(cakeBId);
            if (cakeCId != null) cakeIds.add(cakeCId);

            if (!cakeIds.isEmpty()) {
                String placeholders = String.join(",", cakeIds.stream().map(id -> "?").toArray(String[]::new));
                ps = conn.prepareStatement("DELETE FROM cakes WHERE id IN (" + placeholders + ")");
                for (int i = 0; i < cakeIds.size(); i++) {
                    ps.setInt(i + 1, cakeIds.get(i));
                }
                ps.executeUpdate();
            }

        } finally {
            DBUtil.close(conn, ps);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void testTransactionRollbackOnInsufficientStock() throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(
                    "SELECT ci.cake_id, ci.quantity, c.name, c.stock, c.price " +
                            "FROM cart_items ci JOIN cakes c ON ci.cake_id = c.id " +
                            "WHERE ci.user_id = ?"
            );
            ps.setInt(1, testUserId);
            rs = ps.executeQuery();

            List<CartItemInfo> cartItems = new ArrayList<>();
            while (rs.next()) {
                CartItemInfo item = new CartItemInfo();
                item.cakeId = rs.getInt("cake_id");
                item.quantity = rs.getInt("quantity");
                item.cakeName = rs.getString("name");
                item.stock = rs.getInt("stock");
                item.price = rs.getBigDecimal("price");
                cartItems.add(item);
            }
            rs.close();
            ps.close();

            assertEquals(3, cartItems.size(), "购物车应该有3个商品");

            String orderNo = generateOrderNo();
            ps = conn.prepareStatement(
                    "INSERT INTO orders (order_no, user_id, receiver_name, receiver_phone, receiver_address, total_amount, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, orderNo);
            ps.setInt(2, testUserId);
            ps.setString(3, "测试收货人");
            ps.setString(4, "13800000000");
            ps.setString(5, "测试地址");
            ps.setBigDecimal(6, new BigDecimal("3900.00"));
            ps.setString(7, "pending");
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            int orderId = -1;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
            rs.close();
            ps.close();

            assertTrue(orderId > 0, "订单应该创建成功");

            ps = conn.prepareStatement(
                    "INSERT INTO order_items (order_id, cake_id, cake_name, cake_image, price, quantity, subtotal) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            for (CartItemInfo item : cartItems) {
                ps.setInt(1, orderId);
                ps.setInt(2, item.cakeId);
                ps.setString(3, item.cakeName);
                ps.setString(4, "");
                ps.setBigDecimal(5, item.price);
                ps.setInt(6, item.quantity);
                ps.setBigDecimal(7, item.price.multiply(new BigDecimal(item.quantity)));
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();

            for (CartItemInfo item : cartItems) {
                if (item.stock < item.quantity) {
                    throw new IllegalStateException(
                            "商品 [" + item.cakeName + "] 库存不足，当前库存: " + item.stock + ", 需要: " + item.quantity
                    );
                }

                ps = conn.prepareStatement(
                        "UPDATE cakes SET stock = stock - ?, sales = sales + ? WHERE id = ? AND stock >= ?"
                );
                ps.setInt(1, item.quantity);
                ps.setInt(2, item.quantity);
                ps.setInt(3, item.cakeId);
                ps.setInt(4, item.quantity);
                int rows = ps.executeUpdate();
                ps.close();

                if (rows == 0) {
                    throw new IllegalStateException("扣减商品 [" + item.cakeName + "] 库存失败");
                }
            }

            ps = conn.prepareStatement("DELETE FROM cart_items WHERE user_id = ?");
            ps.setInt(1, testUserId);
            ps.executeUpdate();
            ps.close();

            conn.commit();
            fail("应该在库存不足时抛出异常");

        } catch (IllegalStateException e) {
            if (conn != null) {
                conn.rollback();
            }
            assertTrue(e.getMessage().contains("库存不足"), "异常信息应该包含库存不足");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            DBUtil.close(conn, ps, rs);

            connectionClosed = (conn == null) || conn.isClosed();
        }

        verifyDataConsistency();
    }

    private void verifyDataConsistency() throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM orders")) {
            if (rs.next()) {
                int currentOrderCount = rs.getInt(1);
                assertEquals(initialOrderCount, currentOrderCount, "订单表不应该有新增记录");
            }
        }

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM order_items")) {
            if (rs.next()) {
                int currentOrderItemCount = rs.getInt(1);
                assertEquals(initialOrderItemCount, currentOrderItemCount, "订单明细表不应该有新增记录");
            }
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT stock FROM cakes WHERE id = ?")) {
            ps.setInt(1, cakeAId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    assertEquals(50, rs.getInt("stock"), "商品A库存应该保持原值");
                }
            }

            ps.setInt(1, cakeBId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    assertEquals(10, rs.getInt("stock"), "商品B库存应该保持原值");
                }
            }

            ps.setInt(1, cakeCId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    assertEquals(3, rs.getInt("stock"), "商品C库存应该保持原值");
                }
            }
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM cart_items WHERE user_id = ?")) {
            ps.setInt(1, testUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cartCount = rs.getInt(1);
                    assertEquals(3, cartCount, "购物车应该保持3个商品");
                }
            }
        }

        assertTrue(connectionClosed, "数据库连接应该已关闭");
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis();
    }

    private static class CartItemInfo {
        int cakeId;
        int quantity;
        String cakeName;
        int stock;
        BigDecimal price;
    }
}
