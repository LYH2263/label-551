package com.cakeshop.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 订单详情实体类
 */
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer orderId;
    private Integer cakeId;
    private String cakeName;
    private String cakeImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private Timestamp createdAt;

    public OrderItem() {}

    public OrderItem(Integer cakeId, String cakeName, String cakeImage, BigDecimal price, Integer quantity) {
        this.cakeId = cakeId;
        this.cakeName = cakeName;
        this.cakeImage = cakeImage;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getCakeId() {
        return cakeId;
    }

    public void setCakeId(Integer cakeId) {
        this.cakeId = cakeId;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
