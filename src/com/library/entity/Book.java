package com.library.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 图书实体，对应表 book。
 * <p>
 * totalCount 为馆藏总数，availableCount 为当前可借数量；
 * 借出时 availableCount 减 1，归还时加 1。
 * categoryName 为关联查询出的分类名称，不对应数据库字段，仅用于页面展示。
 */
public class Book {
    private Integer id;
    private String isbn;
    private String title;          // 书名
    private String author;         // 作者
    private String publisher;      // 出版社
    private Integer categoryId;    // 分类id
    private String location;       // 馆藏位置
    private Integer totalCount;    // 馆藏总数
    private Integer availableCount;// 当前可借数量
    private BigDecimal price;       // 单价
    private Date createTime;        // 入库时间
    private Integer status;         // 状态:1正常 0下架

    private String categoryName;    // 分类名称(关联查询，非数据库字段)

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public Integer getAvailableCount() { return availableCount; }
    public void setAvailableCount(Integer availableCount) { this.availableCount = availableCount; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
