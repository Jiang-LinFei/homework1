package com.library.entity;

import java.util.List;

/**
 * 通用分页对象。
 * <p>
 * 图书、借阅记录等数据量较大，列表查询统一分页返回，避免一次性加载过多数据。
 *
 * @param <T> 列表元素类型
 */
public class PageBean<T> {
    private int pageNum;       // 当前页码(从1开始)
    private int pageSize;      // 每页条数
    private long total;        // 总记录数
    private int totalPages;    // 总页数
    private List<T> list;      // 当前页数据

    public PageBean(int pageNum, int pageSize, long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
        // 计算总页数，向上取整
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
    }

    public int getPageNum() { return pageNum; }
    public void setPageNum(int pageNum) { this.pageNum = pageNum; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }

    /** 是否有上一页 */
    public boolean isHasPrev() { return pageNum > 1; }

    /** 是否有下一页 */
    public boolean isHasNext() { return pageNum < totalPages; }
}
