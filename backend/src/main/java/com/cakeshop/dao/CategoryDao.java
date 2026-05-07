package com.cakeshop.dao;

import com.cakeshop.model.Category;
import com.cakeshop.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 蛋糕分类数据访问层
 */
public class CategoryDao {

    /**
     * 查询所有分类
     */
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories ORDER BY sort_order ASC, id ASC";
        List<Category> categories = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return categories;
    }

    /**
     * 根据ID查询分类
     */
    public Category findById(Integer id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToCategory(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    /**
     * 根据名称查询分类
     */
    public Category findByName(String name) {
        String sql = "SELECT * FROM categories WHERE name = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToCategory(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    /**
     * 新增分类
     */
    public int insert(Category category) {
        String sql = "INSERT INTO categories (name, description, sort_order) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getSortOrder() != null ? category.getSortOrder() : 0);

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
     * 更新分类
     */
    public boolean update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, sort_order = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getSortOrder() != null ? category.getSortOrder() : 0);
            ps.setInt(4, category.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    /**
     * 删除分类
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM categories WHERE id = ?";
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
     * 检查分类下是否有商品
     */
    public boolean hasProducts(Integer categoryId) {
        return countProducts(categoryId) > 0;
    }

    /**
     * 统计分类下商品数量
     */
    public int countProducts(Integer categoryId) {
        String sql = "SELECT COUNT(*) FROM cakes WHERE category_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, categoryId);
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
     * 将分类下的商品移至未分类
     */
    public void moveProductsToUncategorized(Integer categoryId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            // 1. 查找或创建"未分类"分类
            int uncategorizedId = -1;
            ps = conn.prepareStatement("SELECT id FROM categories WHERE name = '未分类'");
            rs = ps.executeQuery();
            if (rs.next()) {
                uncategorizedId = rs.getInt("id");
            } else {
                // 创建"未分类"分类
                ps.close();
                ps = conn.prepareStatement("INSERT INTO categories (name, description, sort_order) VALUES ('未分类', '未分类商品', 999)", Statement.RETURN_GENERATED_KEYS);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    uncategorizedId = rs.getInt(1);
                }
            }
            rs.close();
            ps.close();

            // 2. 移动商品到未分类
            if (uncategorizedId > 0) {
                ps = conn.prepareStatement("UPDATE cakes SET category_id = ? WHERE category_id = ?");
                ps.setInt(1, uncategorizedId);
                ps.setInt(2, categoryId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    /**
     * 删除分类下的所有商品
     */
    public void deleteProductsByCategory(Integer categoryId) {
        String sql = "DELETE FROM cakes WHERE category_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, categoryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
    }

    /**
     * ResultSet映射到Category对象
     */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setSortOrder(rs.getInt("sort_order"));
        category.setCreatedAt(rs.getTimestamp("created_at"));
        category.setUpdatedAt(rs.getTimestamp("updated_at"));
        return category;
    }
}
