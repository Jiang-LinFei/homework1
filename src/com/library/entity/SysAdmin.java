package com.library.entity;

import java.util.Date;

/**
 * 系统管理员实体，对应表 sys_admin。
 * 系统管理员负责维护普通管理员账号、图书分类等。
 */
public class SysAdmin {
    private Integer id;
    private String username;   // 登录账号
    private String password;   // 登录密码(MD5+盐)
    private String realName;   // 真实姓名
    private Date createTime;   // 创建时间

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
