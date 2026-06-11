package com.library.entity;

/**
 * 图书分类实体，对应表 book_category。
 */
public class BookCategory {
    private Integer id;
    private String name;   // 分类名称

    public BookCategory() {
    }

    public BookCategory(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
