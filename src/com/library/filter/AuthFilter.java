package com.library.filter;

import com.library.entity.LoginUser;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 登录与权限过滤器，拦截后台业务路径 /admin/*。
 * <p>
 * 规则：
 * <ul>
 *   <li>未登录 -> 重定向到登录页；</li>
 *   <li>已登录但访问"系统管理员专属"路径(/admin/manager、/admin/category)
 *       而当前是普通管理员 -> 跳转无权限页。</li>
 * </ul>
 * 登录页、静态资源不在 /admin/* 下，因此不受影响。
 */
@WebFilter(filterName = "authFilter", urlPatterns = "/admin/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        HttpSession session = request.getSession(false);
        LoginUser user = session == null ? null : (LoginUser) session.getAttribute(LoginUser.SESSION_KEY);

        String ctx = request.getContextPath();
        if (user == null) {
            // 未登录，回到登录页
            response.sendRedirect(ctx + "/login.jsp");
            return;
        }

        // 系统管理员专属功能：账号管理、分类管理
        String path = request.getRequestURI().substring(ctx.length());
        boolean sysOnly = path.startsWith("/admin/manager") || path.startsWith("/admin/category");
        if (sysOnly && !user.isSysAdmin()) {
            request.setAttribute("msg", "该功能仅系统管理员可用");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }

        chain.doFilter(req, resp);
    }
}
