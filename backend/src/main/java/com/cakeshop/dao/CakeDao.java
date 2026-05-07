package com.cakeshop.dao;

import com.cakeshop.model.Cake;
import com.cakeshop.util.DBUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 蛋糕商品数据访问层
 */
public class CakeDao {

    /**
     * 分页查询蛋糕列表（前台，只显示上架商品）
     */
    public List<Cake> findByPage(Integer categoryId, String keyword, int page, int pageSize) {
        StringBuilder sql = new StringBuilder(
            "SELECT c.*, cat.name as category_name FROM cakes c " +
            "LEFT JOIN categories cat ON c.category_id = cat.id " +
            "WHERE c.status = 'on'"
        );
        List<Object> params = new ArrayList<>();

        if (categoryId != null) {
            sql.append(" AND c.category_id = ?");
            params.add(categoryId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (c.name LIKE ? OR c.description LIKE ?)");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }

        sql.append(" ORDER BY c.sales DESC, c.created_at DESC");
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        List<Cake> cakes = new ArrayList<>();
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
                cakes.add(mapResultSetToCake(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return cakes;
    }

    /**
     * 获取热门蛋糕
     */
    public List<Cake> findHot(int limit) {
        String sql = "SELECT c.*, cat.name as category_name FROM cakes c " +
                    "LEFT JOIN categories cat ON c.category_id = cat.id " +
                    "WHERE c.status = 'on' ORDER BY c.sales DESC LIMIT ?";
        List<Cake> cakes = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);
            rs = ps.executeQuery();

            while (rs.next()) {
                cakes.add(mapResultSetToCake(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return cakes;
    }

    /**
     * 统计蛋糕数量（前台）
     */
    public int count(Integer categoryId, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM cakes WHERE status = 'on'");
        List<Object> params = new ArrayList<>();

        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR description LIKE ?)");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
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
     * 根据ID查询蛋糕
     */
    public Cake findById(Integer id) {
        String sql = "SELECT c.*, cat.name as category_name FROM cakes c " +
                    "LEFT JOIN categories cat ON c.category_id = cat.id " +
                    "WHERE c.id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToCake(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    /**
     * 管理员分页查询（包含下架商品）
     */
    public List<Cake> findAllByPage(Integer categoryId, String keyword, String status, int page, int pageSize) {
        StringBuilder sql = new StringBuilder(
            "SELECT c.*, cat.name as category_name FROM cakes c " +
            "LEFT JOIN categories cat ON c.category_id = cat.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (categoryId != null) {
            sql.append(" AND c.category_id = ?");
            params.add(categoryId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (c.name LIKE ? OR c.description LIKE ?)");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND c.status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY c.created_at DESC");
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        List<Cake> cakes = new ArrayList<>();
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
                cakes.add(mapResultSetToCake(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return cakes;
    }

    /**
     * 管理员统计数量
     */
    public int countAll(Integer categoryId, String keyword, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM cakes WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR description LIKE ?)");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
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
     * 新增蛋糕
     */
    public int insert(Cake cake) {
        String sql = "INSERT INTO cakes (name, category_id, price, original_price, image, images, description, size, flavor, stock, sales, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, cake.getName());
            ps.setInt(2, cake.getCategoryId());
            ps.setBigDecimal(3, cake.getPrice());
            ps.setBigDecimal(4, cake.getOriginalPrice());
            ps.setString(5, cake.getImage());
            ps.setString(6, cake.getImages());
            ps.setString(7, cake.getDescription());
            ps.setString(8, cake.getSize());
            ps.setString(9, cake.getFlavor());
            ps.setInt(10, cake.getStock() != null ? cake.getStock() : 0);
            ps.setInt(11, cake.getSales() != null ? cake.getSales() : 0);
            ps.setString(12, cake.getStatus() != null ? cake.getStatus() : "on");

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
     * 更新蛋糕
     */
    public boolean update(Cake cake) {
        String sql = "UPDATE cakes SET name = ?, category_id = ?, price = ?, original_price = ?, " +
                    "image = ?, images = ?, description = ?, size = ?, flavor = ?, stock = ?, status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, cake.getName());
            ps.setInt(2, cake.getCategoryId());
            ps.setBigDecimal(3, cake.getPrice());
            ps.setBigDecimal(4, cake.getOriginalPrice());
            ps.setString(5, cake.getImage());
            ps.setString(6, cake.getImages());
            ps.setString(7, cake.getDescription());
            ps.setString(8, cake.getSize());
            ps.setString(9, cake.getFlavor());
            ps.setInt(10, cake.getStock() != null ? cake.getStock() : 0);
            ps.setString(11, cake.getStatus());
            ps.setInt(12, cake.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    /**
     * 更新上下架状态
     */
    public boolean updateStatus(Integer id, String status) {
        String sql = "UPDATE cakes SET status = ? WHERE id = ?";
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
     * 减少库存
     */
    public boolean decreaseStock(Integer id, int quantity) {
        String sql = "UPDATE cakes SET stock = stock - ?, sales = sales + ? WHERE id = ? AND stock >= ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, quantity);
            ps.setInt(2, quantity);
            ps.setInt(3, id);
            ps.setInt(4, quantity);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    /**
     * 删除蛋糕
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM cakes WHERE id = ?";
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
     * ResultSet映射到Cake对象
     */
    private Cake mapResultSetToCake(ResultSet rs) throws SQLException {
        Cake cake = new Cake();
        cake.setId(rs.getInt("id"));
        cake.setName(rs.getString("name"));
        cake.setCategoryId(rs.getInt("category_id"));
        try {
            cake.setCategoryName(rs.getString("category_name"));
        } catch (SQLException e) {
            // category_name可能不存在
        }
        cake.setPrice(rs.getBigDecimal("price"));
        cake.setOriginalPrice(rs.getBigDecimal("original_price"));
        cake.setImage(rs.getString("image"));
        cake.setImages(rs.getString("images"));
        cake.setDescription(rs.getString("description"));
        cake.setSize(rs.getString("size"));
        cake.setFlavor(rs.getString("flavor"));
        cake.setStock(rs.getInt("stock"));
        cake.setSales(rs.getInt("sales"));
        cake.setStatus(rs.getString("status"));
        cake.setCreatedAt(rs.getTimestamp("created_at"));
        cake.setUpdatedAt(rs.getTimestamp("updated_at"));
        return cake;
    }
}
