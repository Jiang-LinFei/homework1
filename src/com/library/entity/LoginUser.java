package com.library.entity;

/**
 * 登录用户会话对象，登录成功后存入 HttpSession。
 * role 取值：
 * <ul>
 *   <li>{@link #ROLE_SYS} 系统管理员</li>
 *   <li>{@link #ROLE_MANAGER} 普通管理员</li>
 * </ul>
 */
public class LoginUser {

    public static final String ROLE_SYS = "SYS";       // 系统管理员
    public static final String ROLE_MANAGER = "MGR";   // 普通管理员

    /** 会话中保存登录用户的属性名 */
    public static final String SESSION_KEY = "loginUser";

    private Integer id;
    private String username;
    private String realName;
    private String role;

    public LoginUser() {
    }

    public LoginUser(Integer id, String username, String realName, String role) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.role = role;
    }

    /** 是否系统管理员 */
    public boolean isSysAdmin() {
        return ROLE_SYS.equals(role);
    }

    /** 角色中文名，用于页面展示 */
    public String getRoleName() {
        return isSysAdmin() ? "系统管理员" : "普通管理员";
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
