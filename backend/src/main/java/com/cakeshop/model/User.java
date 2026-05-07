package com.cakeshop.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 用户实体类
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String phone;
    private String password;
    private String username;
    private String role; // user, admin
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {}

    public User(String phone, String password, String username) {
        this.phone = phone;
        this.password = password;
        this.username = username;
        this.role = "user";
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
