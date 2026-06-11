package com.library.entity;

import java.util.Date;

/**
 * 普通管理员实体，对应表 manager。
 * 普通管理员负责图书入库、读者登记、借阅/归还登记、逾期提醒等日常业务。
 */
public class Manager {
    private Integer id;
    private String username;   // 登录账号
    private String password;   // 登录密码(MD5+盐)
    private String realName;   // 真实姓名
    private String phone;      // 联系电话
    private Integer status;    // 状态:1启用 0禁用
    private Date createTime;   // 创建时间

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
