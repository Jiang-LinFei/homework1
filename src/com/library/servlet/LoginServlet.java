package com.library.servlet;

import com.library.dao.ManagerDao;
import com.library.dao.SysAdminDao;
import com.library.entity.LoginUser;
import com.library.entity.Manager;
import com.library.entity.SysAdmin;
import com.library.util.MD5Util;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录处理 Servlet。
 * <p>
 * 根据前端选择的角色(role=SYS / MGR)分别查询系统管理员表或普通管理员表，
 * 用 MD5+盐 校验密码，成功后写入会话并跳转到首页，失败回到登录页并提示。
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final SysAdminDao sysAdminDao = new SysAdminDao();
    private final ManagerDao managerDao = new ManagerDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = trim(request.getParameter("username"));
        String password = request.getParameter("password");
        String role = request.getParameter("role"); // SYS / MGR

        String error = null;
        LoginUser loginUser = null;

        if (username.isEmpty() || password == null || password.isEmpty()) {
            error = "请输入账号和密码";
        } else {
            String encrypted = MD5Util.encrypt(password);
            if (LoginUser.ROLE_SYS.equals(role)) {
                // 系统管理员登录
                SysAdmin admin = sysAdminDao.findByUsername(username);
                if (admin != null && admin.getPassword().equalsIgnoreCase(encrypted)) {
                    loginUser = new LoginUser(admin.getId(), admin.getUsername(),
                            admin.getRealName(), LoginUser.ROLE_SYS);
                }
            } else {
                // 普通管理员登录
                Manager m = managerDao.findByUsername(username);
                if (m != null && m.getPassword().equalsIgnoreCase(encrypted)) {
                    if (m.getStatus() != null && m.getStatus() == 0) {
                        error = "该账号已被禁用，请联系系统管理员";
                    } else {
                        loginUser = new LoginUser(m.getId(), m.getUsername(),
                                m.getRealName(), LoginUser.ROLE_MANAGER);
                    }
                }
            }
            if (loginUser == null && error == null) {
                error = "账号或密码错误";
            }
        }

        if (loginUser != null) {
            request.getSession().setAttribute(LoginUser.SESSION_KEY, loginUser);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else {
            request.setAttribute("error", error);
            request.setAttribute("username", username);
            request.setAttribute("role", role);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 直接访问 /login 时回到登录页
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
