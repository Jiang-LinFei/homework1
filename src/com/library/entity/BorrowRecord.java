package com.library.entity;

import java.util.Date;

/**
 * 借阅记录实体，对应表 borrow_record。
 * <p>
 * status：1=借出中，2=已归还。
 * 逾期不单独存字段，而是运行时根据 (status==1 且 dueTime < 当前时间) 判断，
 * overdue 即为该计算结果，仅用于页面展示与"逾期提醒"。
 * bookTitle / readerName / readerCardNo 为关联查询字段，非数据库列。
 */
public class BorrowRecord {
    private Integer id;
    private Integer bookId;
    private Integer readerId;
    private Date borrowTime;     // 借出时间
    private Date dueTime;        // 应还时间
    private Date returnTime;     // 实际归还时间
    private Integer status;      // 1借出中 2已归还
    private Integer operatorId;  // 经办管理员id
    private String remark;       // 备注

    private String bookTitle;    // 书名(关联查询)
    private String readerName;   // 读者姓名(关联查询)
    private String readerCardNo; // 借阅证号(关联查询)
    private boolean overdue;     // 是否逾期(运行时计算)

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public Integer getReaderId() { return readerId; }
    public void setReaderId(Integer readerId) { this.readerId = readerId; }

    public Date getBorrowTime() { return borrowTime; }
    public void setBorrowTime(Date borrowTime) { this.borrowTime = borrowTime; }

    public Date getDueTime() { return dueTime; }
    public void setDueTime(Date dueTime) { this.dueTime = dueTime; }

    public Date getReturnTime() { return returnTime; }
    public void setReturnTime(Date returnTime) { this.returnTime = returnTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getOperatorId() { return operatorId; }
    public void setOperatorId(Integer operatorId) { this.operatorId = operatorId; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getReaderName() { return readerName; }
    public void setReaderName(String readerName) { this.readerName = readerName; }

    public String getReaderCardNo() { return readerCardNo; }
    public void setReaderCardNo(String readerCardNo) { this.readerCardNo = readerCardNo; }

    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }
}
