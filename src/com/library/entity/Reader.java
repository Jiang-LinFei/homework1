package com.library.entity;

import java.util.Date;

/**
 * 读者实体，对应表 reader。
 * cardNo 为借阅证号(唯一)，maxBorrow 为该读者最大可借数量。
 */
public class Reader {
    private Integer id;
    private String cardNo;        // 借阅证号
    private String name;          // 姓名
    private String gender;        // 性别
    private String phone;         // 联系电话
    private String readerType;    // 读者类型:学生/教师/职工
    private Integer maxBorrow;    // 最大可借数量
    private Date registerTime;    // 登记时间
    private Integer status;       // 状态:1正常 0注销

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCardNo() { return cardNo; }
    public void setCardNo(String cardNo) { this.cardNo = cardNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getReaderType() { return readerType; }
    public void setReaderType(String readerType) { this.readerType = readerType; }

    public Integer getMaxBorrow() { return maxBorrow; }
    public void setMaxBorrow(Integer maxBorrow) { this.maxBorrow = maxBorrow; }

    public Date getRegisterTime() { return registerTime; }
    public void setRegisterTime(Date registerTime) { this.registerTime = registerTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
