package com.cakeshop;

import com.cakeshop.util.DBUtil;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTransactionRollbackTest {

    private static final String TEST_PHONE = "99900001111";
    private static final String TEST_PASSWORD = "test_hash_password";
    private static final String TEST_USERNAME = "事务回滚测试用户";
    private static final String TEST_CATEGORY_NAME = "事务测试分类_ROLLBACK";
    private static final String TEST_RECEIVER_NAME = "测试收件人";
    private static final String TEST_RECEIVER_PHONE = "99900002222";
    private static final String TEST_RECEIVER_ADDRESS = "广东省深圳市南山区测试路1号";

    private int testUserId;
    private int testCategoryId;
    private int cakeIdA;
    private int cakeIdB;
    private int cakeIdC;
    private int testAddressId;

    private int initialOrderCount;
    private int initialOrderItemCount;
    private int initialCartItemCount;

    @BeforeEach
    void setUp() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            assertFalse(conn.isClosed(), "数据库连接应正常建立");

            ps = conn.prepareStatement(
                "INSERT INTO users (phone, password, username, role) VALUES (?, ?, ?, 'user')",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, TEST_PHONE);
            ps.setString(2, TEST_PASSWORD);
            ps.setString(3, TEST_USERNAME);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            assertTrue(rs.next(), "应成功生成用户ID");
            testUserId = rs.getInt(1);
            rs.close();
            ps.close();

            ps = conn.prepareStatement(
                "INSERT INTO categories (name, description, sort_order) VALUES (?, ?, 99)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, TEST_CATEGORY_NAME);
            ps.setString(2, "事务回滚测试专用分类");
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            assertTrue(rs.next(), "应成功生成分类ID");
            testCategoryId = rs.getInt(1);
            rs.close();
            ps.close();

            cakeIdA = insertCake(conn, "事务测试商品A_库存充足", testCategoryId,
                new BigDecimal("100.00"), new BigDecimal("120.00"), 50);
            cakeIdB = insertCake(conn, "事务测试商品B_库存临界", testCategoryId,
                new BigDecimal("200.00"), new BigDecimal("240.00"), 10);
            cakeIdC = insertCake(conn, "事务测试商品C_库存不足", testCategoryId,
                new BigDecimal("300.00"), new BigDecimal("360.00"), 3);

            assertTrue(cakeIdA > 0, "商品A应成功创建");
            assertTrue(cakeIdB > 0, "商品B应成功创建");
            assertTrue(cakeIdC > 0, "商品C应成功创建");

            ps = conn.prepareStatement(
                "INSERT INTO addresses (user_id, receiver_name, phone, province, city, district, detail, is_default) " +
                "VALUES (?, ?, ?, '广东省', '深圳市', '南山区', '测试路1号', 1)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, testUserId);
            ps.setString(2, TEST_RECEIVER_NAME);
            ps.setString(3, TEST_RECEIVER_PHONE);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            assertTrue(rs.next(), "应成功生成地址ID");
            testAddressId = rs.getInt(1);
            rs.close();
            ps.close();

            insertCartItem(conn, testUserId, cakeIdA, 5);
            insertCartItem(conn, testUserId, cakeIdB, 10);
            insertCartItem(conn, testUserId, cakeIdC, 8);

            initialOrderCount = countTable(conn, "orders", "user_id = " + testUserId);
            initialOrderItemCount = 0;
            initialCartItemCount = countTable(conn, "cart_items", "user_id = " + testUserId);

            assertEquals(0, initialOrderCount, "初始状态下不应有测试用户的订单");
            assertEquals(3, initialCartItemCount, "初始状态下购物车应有3个商品");
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            ps = conn.prepareStatement("DELETE FROM cart_items WHERE user_id = ?");
            ps.setInt(1, testUserId);
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement(
                "DELETE oi FROM order_items oi " +
                "INNER JOIN orders o ON oi.order_id = o.id WHERE o.user_id = ?"
            );
            ps.setInt(1, testUserId);
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement("DELETE FROM orders WHERE user_id = ?");
            ps.setInt(1, testUserId);
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement("DELETE FROM addresses WHERE user_id = ?");
            ps.setInt(1, testUserId);
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement("DELETE FROM cakes WHERE id IN (?, ?, ?)");
            ps.setInt(1, cakeIdA);
            ps.setInt(2, cakeIdB);
            ps.setInt(3, cakeIdC);
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement("DELETE FROM categories WHERE id = ?");
            ps.setInt(1, testCategoryId);
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement("DELETE FROM users WHERE id = ?");
            ps.setInt(1, testUserId);
            ps.executeUpdate();
            ps.close();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
            DBUtil.close(conn, ps);
        }
    }

    @Test
    @DisplayName("商品C库存不足时，整个下单事务应回滚，所有数据保持一致")
    void testOrderTransactionRollbackOnInsufficientStock() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean transactionRolledBack = false;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            List<int[]> cartData = new ArrayList<>();
            ps = conn.prepareStatement(
                "SELECT ci.cake_id, ci.quantity, c.name, c.price, c.image " +
                "FROM cart_items ci LEFT JOIN cakes c ON ci.cake_id = c.id " +
                "WHERE ci.user_id = ? ORDER BY ci.cake_id"
            );
            ps.setInt(1, testUserId);
            rs = ps.executeQuery();
            while (rs.next()) {
                cartData.add(new int[]{
                    rs.getInt("cake_id"),
                    rs.getInt("quantity")
                });
            }
            rs.close();
            ps.close();
            assertEquals(3, cartData.size(), "应读取到3个购物车商品");

            BigDecimal totalAmount = BigDecimal.ZERO;
            List<Object[]> orderItemData = new ArrayList<>();
            for (int[] item : cartData) {
                int cakeId = item[0];
                int quantity = item[1];

                ps = conn.prepareStatement("SELECT price FROM cakes WHERE id = ?");
                ps.setInt(1, cakeId);
                rs = ps.executeQuery();
                assertTrue(rs.next(), "应查询到蛋糕价格");
                BigDecimal price = rs.getBigDecimal("price");
                rs.close();
                ps.close();

                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
                totalAmount = totalAmount.add(subtotal);
                orderItemData.add(new Object[]{cakeId, quantity, price, subtotal});
            }

            String orderNo = generateOrderNo();
            ps = conn.prepareStatement(
                "INSERT INTO orders (order_no, user_id, address_id, receiver_name, receiver_phone, " +
                "receiver_address, total_amount, status, remark) VALUES (?, ?, ?, ?, ?, ?, ?, 'pending', ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, orderNo);
            ps.setInt(2, testUserId);
            ps.setInt(3, testAddressId);
            ps.setString(4, TEST_RECEIVER_NAME);
            ps.setString(5, TEST_RECEIVER_PHONE);
            ps.setString(6, TEST_RECEIVER_ADDRESS);
            ps.setBigDecimal(7, totalAmount);
            ps.setString(8, "事务回滚测试");
            int orderRows = ps.executeUpdate();
            assertEquals(1, orderRows, "应成功插入1条订单记录");

            rs = ps.getGeneratedKeys();
            int orderId = -1;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
            rs.close();
            ps.close();
            assertTrue(orderId > 0, "应获取到有效的订单ID");

            for (Object[] itemData : orderItemData) {
                int cakeId = (int) itemData[0];
                int quantity = (int) itemData[1];
                BigDecimal price = (BigDecimal) itemData[2];
                BigDecimal subtotal = (BigDecimal) itemData[3];

                ps = conn.prepareStatement(
                    "INSERT INTO order_items (order_id, cake_id, cake_name, cake_image, price, quantity, subtotal) " +
                    "VALUES (?, ?, '测试商品', '', ?, ?, ?)"
                );
                ps.setInt(1, orderId);
                ps.setInt(2, cakeId);
                ps.setBigDecimal(3, price);
                ps.setInt(4, quantity);
                ps.setBigDecimal(5, subtotal);
                ps.executeUpdate();
                ps.close();
            }

            boolean stockInsufficient = false;
            for (int[] item : cartData) {
                int cakeId = item[0];
                int quantity = item[1];

                ps = conn.prepareStatement(
                    "UPDATE cakes SET stock = stock - ?, sales = sales + ? WHERE id = ? AND stock >= ?"
                );
                ps.setInt(1, quantity);
                ps.setInt(2, quantity);
                ps.setInt(3, cakeId);
                ps.setInt(4, quantity);
                int affectedRows = ps.executeUpdate();
                ps.close();

                if (affectedRows == 0) {
                    stockInsufficient = true;
                    break;
                }
            }

            if (stockInsufficient) {
                conn.rollback();
                transactionRolledBack = true;
            } else {
                ps = conn.prepareStatement("DELETE FROM cart_items WHERE user_id = ?");
                ps.setInt(1, testUserId);
                ps.executeUpdate();
                ps.close();

                conn.commit();
            }
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            transactionRolledBack = true;
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
            DBUtil.close(conn, ps, rs);
        }

        assertTrue(transactionRolledBack, "由于商品C库存不足，事务应被回滚");

        Connection verifyConn = null;
        PreparedStatement verifyPs = null;
        ResultSet verifyRs = null;

        try {
            verifyConn = DBUtil.getConnection();
            assertFalse(verifyConn.isClosed(), "验证连接应正常建立");

            verifyPs = verifyConn.prepareStatement("SELECT COUNT(*) FROM orders WHERE user_id = ?");
            verifyPs.setInt(1, testUserId);
            verifyRs = verifyPs.executeQuery();
            assertTrue(verifyRs.next());
            int orderCount = verifyRs.getInt(1);
            verifyRs.close();
            verifyPs.close();
            assertEquals(initialOrderCount, orderCount,
                "事务回滚后，订单表中不应有新订单记录");

            verifyPs = verifyConn.prepareStatement("SELECT COUNT(*) FROM order_items WHERE order_id IN " +
                "(SELECT id FROM orders WHERE user_id = ?)");
            verifyPs.setInt(1, testUserId);
            verifyRs = verifyPs.executeQuery();
            assertTrue(verifyRs.next());
            int orderItemCount = verifyRs.getInt(1);
            verifyRs.close();
            verifyPs.close();
            assertEquals(initialOrderItemCount, orderItemCount,
                "事务回滚后，订单明细表中不应有新记录");

            verifyPs = verifyConn.prepareStatement("SELECT stock FROM cakes WHERE id = ?");
            verifyPs.setInt(1, cakeIdA);
            verifyRs = verifyPs.executeQuery();
            assertTrue(verifyRs.next());
            int stockA = verifyRs.getInt("stock");
            verifyRs.close();
            verifyPs.close();
            assertEquals(50, stockA,
                "事务回滚后，商品A库存应保持原值50");

            verifyPs = verifyConn.prepareStatement("SELECT stock FROM cakes WHERE id = ?");
            verifyPs.setInt(1, cakeIdB);
            verifyRs = verifyPs.executeQuery();
            assertTrue(verifyRs.next());
            int stockB = verifyRs.getInt("stock");
            verifyRs.close();
            verifyPs.close();
            assertEquals(10, stockB,
                "事务回滚后，商品B库存应保持原值10");

            verifyPs = verifyConn.prepareStatement("SELECT stock FROM cakes WHERE id = ?");
            verifyPs.setInt(1, cakeIdC);
            verifyRs = verifyPs.executeQuery();
            assertTrue(verifyRs.next());
            int stockC = verifyRs.getInt("stock");
            verifyRs.close();
            verifyPs.close();
            assertEquals(3, stockC,
                "事务回滚后，商品C库存应保持原值3");

            verifyPs = verifyConn.prepareStatement("SELECT COUNT(*) FROM cart_items WHERE user_id = ?");
            verifyPs.setInt(1, testUserId);
            verifyRs = verifyPs.executeQuery();
            assertTrue(verifyRs.next());
            int cartCount = verifyRs.getInt(1);
            verifyRs.close();
            verifyPs.close();
            assertEquals(initialCartItemCount, cartCount,
                "事务回滚后，购物车中的商品不应被清空，应保持原有3个商品");

        } finally {
            DBUtil.close(verifyConn, verifyPs, verifyRs);
        }
    }

    private int insertCake(Connection conn, String name, int categoryId,
                           BigDecimal price, BigDecimal originalPrice, int stock) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(
                "INSERT INTO cakes (name, category_id, price, original_price, image, description, " +
                "size, flavor, stock, sales, status) VALUES (?, ?, ?, ?, '', '', '8寸', '原味', ?, 0, 'on')",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            ps.setInt(2, categoryId);
            ps.setBigDecimal(3, price);
            ps.setBigDecimal(4, originalPrice);
            ps.setInt(5, stock);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void insertCartItem(Connection conn, int userId, int cakeId, int quantity) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                "INSERT INTO cart_items (user_id, cake_id, quantity) VALUES (?, ?, ?)"
            );
            ps.setInt(1, userId);
            ps.setInt(2, cakeId);
            ps.setInt(3, quantity);
            int rows = ps.executeUpdate();
            assertEquals(1, rows, "应成功插入购物车项");
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private int countTable(Connection conn, String table, String whereClause) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT COUNT(*) FROM " + table + " WHERE " + whereClause);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        int random = (int) (Math.random() * 10000);
        return "ORD" + timestamp + String.format("%04d", random);
    }
}
