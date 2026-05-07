package com.cakeshop.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 购物车项实体类
 */
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer userId;
    private Integer cakeId;
    private Integer quantity;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // 关联的蛋糕信息（非数据库字段）
    private String cakeName;
    private String cakeImage;
    private BigDecimal cakePrice;
    private Integer cakeStock;
    private String cakeStatus;

    public CartItem() {}

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCakeId() {
        return cakeId;
    }

    public void setCakeId(Integer cakeId) {
        this.cakeId = cakeId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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

    public String getCakeName() {
        return cakeName;
    }

    public void setCakeName(String cakeName) {
        this.cakeName = cakeName;
    }

    public String getCakeImage() {
        return cakeImage;
    }

    public void setCakeImage(String cakeImage) {
        this.cakeImage = cakeImage;
    }

    public BigDecimal getCakePrice() {
        return cakePrice;
    }

    public void setCakePrice(BigDecimal cakePrice) {
        this.cakePrice = cakePrice;
    }

    public Integer getCakeStock() {
        return cakeStock;
    }

    public void setCakeStock(Integer cakeStock) {
        this.cakeStock = cakeStock;
    }

    public String getCakeStatus() {
        return cakeStatus;
    }

    public void setCakeStatus(String cakeStatus) {
        this.cakeStatus = cakeStatus;
    }

    /**
     * 计算小计
     */
    public BigDecimal getSubtotal() {
        if (cakePrice != null && quantity != null) {
            return cakePrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}
