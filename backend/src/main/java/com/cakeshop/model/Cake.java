package com.cakeshop.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 蛋糕商品实体类
 */
public class Cake implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private Integer categoryId;
    private String categoryName; // 冗余字段，方便前端显示
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String image;
    private String images; // JSON数组
    private String description;
    private String size;
    private String flavor;
    private Integer stock;
    private Integer sales;
    private String status; // on, off
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Cake() {}

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    /**
     * 是否有库存
     */
    public boolean hasStock() {
        return stock != null && stock > 0;
    }

    /**
     * 是否上架
     */
    public boolean isOnSale() {
        return "on".equals(status);
    }

    /**
     * 获取规格（尺寸 + 口味）
     */
    public String getSpec() {
        StringBuilder spec = new StringBuilder();
        if (size != null && !size.isEmpty()) {
            spec.append(size);
        }
        if (flavor != null && !flavor.isEmpty()) {
            if (spec.length() > 0) {
                spec.append(" / ");
            }
            spec.append(flavor);
        }
        return spec.toString();
    }
}
