package com.cakeshop.dao;

import com.cakeshop.model.Address;
import com.cakeshop.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 收货地址数据访问层
 */
public class AddressDao {

    /**
     * 根据用户ID查询所有地址
     */
    public List<Address> findByUserId(Integer userId) {
        String sql = "SELECT * FROM addresses WHERE user_id = ? ORDER BY is_default DESC, created_at DESC";
        List<Address> addresses = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return addresses;
    }

    /**
     * 根据ID查询地址
     */
    public Address findById(Integer id) {
        String sql = "SELECT * FROM addresses WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToAddress(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    /**
     * 查询用户默认地址
     */
    public Address findDefaultByUserId(Integer userId) {
        String sql = "SELECT * FROM addresses WHERE user_id = ? AND is_default = 1 LIMIT 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToAddress(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    /**
     * 新增地址
     */
    public int insert(Address address) {
        String sql = "INSERT INTO addresses (user_id, receiver_name, phone, province, city, district, detail, is_default) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();

            // 如果设为默认，先取消其他默认
            if (address.getIsDefault() != null && address.getIsDefault()) {
                clearDefault(conn, address.getUserId());
            }

            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, address.getUserId());
            ps.setString(2, address.getReceiverName());
            ps.setString(3, address.getPhone());
            ps.setString(4, address.getProvince());
            ps.setString(5, address.getCity());
            ps.setString(6, address.getDistrict());
            ps.setString(7, address.getDetail());
            ps.setBoolean(8, address.getIsDefault() != null && address.getIsDefault());

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
     * 更新地址
     */
    public boolean update(Address address) {
        String sql = "UPDATE addresses SET receiver_name = ?, phone = ?, province = ?, city = ?, district = ?, detail = ?, is_default = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();

            // 如果设为默认，先取消其他默认
            if (address.getIsDefault() != null && address.getIsDefault()) {
                clearDefault(conn, address.getUserId());
            }

            ps = conn.prepareStatement(sql);
            ps.setString(1, address.getReceiverName());
            ps.setString(2, address.getPhone());
            ps.setString(3, address.getProvince());
            ps.setString(4, address.getCity());
            ps.setString(5, address.getDistrict());
            ps.setString(6, address.getDetail());
            ps.setBoolean(7, address.getIsDefault() != null && address.getIsDefault());
            ps.setInt(8, address.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, ps);
        }
        return false;
    }

    /**
     * 删除地址
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM addresses WHERE id = ?";
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
     * 设置默认地址
     */
    public boolean setDefault(Integer addressId, Integer userId) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 取消其他默认
            clearDefault(conn, userId);

            // 设置新默认
            String sql = "UPDATE addresses SET is_default = 1 WHERE id = ? AND user_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, addressId);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();

            conn.commit();
            return rows > 0;
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
        return false;
    }

    /**
     * 取消用户所有默认地址
     */
    private void clearDefault(Connection conn, Integer userId) throws SQLException {
        String sql = "UPDATE addresses SET is_default = 0 WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /**
     * ResultSet映射到Address对象
     */
    private Address mapResultSetToAddress(ResultSet rs) throws SQLException {
        Address address = new Address();
        address.setId(rs.getInt("id"));
        address.setUserId(rs.getInt("user_id"));
        address.setReceiverName(rs.getString("receiver_name"));
        address.setPhone(rs.getString("phone"));
        address.setProvince(rs.getString("province"));
        address.setCity(rs.getString("city"));
        address.setDistrict(rs.getString("district"));
        address.setDetail(rs.getString("detail"));
        address.setIsDefault(rs.getBoolean("is_default"));
        address.setCreatedAt(rs.getTimestamp("created_at"));
        address.setUpdatedAt(rs.getTimestamp("updated_at"));
        return address;
    }
}
