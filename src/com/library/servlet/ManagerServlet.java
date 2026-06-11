package com.library.servlet;

import com.library.dao.ManagerDao;
import com.library.entity.Manager;
import com.library.util.MD5Util;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 普通管理员账号管理 Servlet（仅系统管理员可访问，由 AuthFilter 控制）。
 * action: list / add / edit / save / delete。
 */
@WebServlet("/admin/manager")
public class ManagerServlet extends HttpServlet {

    private final ManagerDao managerDao = new ManagerDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = param(request, "action", "list");
        switch (action) {
            case "add":
                showForm(request, response, null);
                break;
            case "edit":
                showForm(request, response, managerDao.findById(intParam(request, "id", 0)));
                break;
            case "delete":
                managerDao.delete(intParam(request, "id", 0));
                redirectList(request, response);
                break;
            default:
                list(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("save".equals(param(request, "action", ""))) {
            save(request, response);
        } else {
            list(request, response);
        }
    }

    private void list(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("managers", managerDao.findAll());
        request.getRequestDispatcher("/WEB-INF/views/manager_list.jsp").forward(request, response);
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Manager m)
            throws ServletException, IOException {
        request.setAttribute("manager", m);
        request.getRequestDispatcher("/WEB-INF/views/manager_form.jsp").forward(request, response);
    }

    private void save(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String id = param(request, "id", "");
        String username = param(request, "username", "");
        String password = param(request, "password", "");

        Manager m = new Manager();
        m.setUsername(username);
        m.setRealName(param(request, "realName", ""));
        m.setPhone(param(request, "phone", ""));
        m.setStatus(intParam(request, "status", 1));

        if (id == null || id.isEmpty()) {
            // 新增：账号唯一校验 + 密码必填
            if (managerDao.findByUsername(username) != null) {
                backToForm(request, response, m, "账号已存在：" + username);
                return;
            }
            if (password.isEmpty()) {
                backToForm(request, response, m, "请设置初始密码");
                return;
            }
            m.setPassword(MD5Util.encrypt(password));
            managerDao.insert(m);
        } else {
            m.setId(Integer.parseInt(id));
            // 编辑：密码留空表示不修改
            if (!password.isEmpty()) {
                m.setPassword(MD5Util.encrypt(password));
            }
            managerDao.update(m);
        }
        redirectList(request, response);
    }

    private void backToForm(HttpServletRequest request, HttpServletResponse response, Manager m, String error)
            throws ServletException, IOException {
        request.setAttribute("manager", m);
        request.setAttribute("formError", error);
        request.getRequestDispatcher("/WEB-INF/views/manager_form.jsp").forward(request, response);
    }

    private void redirectList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/manager");
    }

    private String param(HttpServletRequest req, String name, String def) {
        String v = req.getParameter(name);
        return v == null ? def : v.trim();
    }

    private int intParam(HttpServletRequest req, String name, int def) {
        try {
            return Integer.parseInt(req.getParameter(name));
        } catch (Exception e) {
            return def;
        }
    }
}
