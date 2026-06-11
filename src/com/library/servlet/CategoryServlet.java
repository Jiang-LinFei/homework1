package com.library.servlet;

import com.library.dao.BookCategoryDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 图书分类管理 Servlet（仅系统管理员可访问）。
 * action: list / add / delete。
 */
@WebServlet("/admin/category")
public class CategoryServlet extends HttpServlet {

    private final BookCategoryDao categoryDao = new BookCategoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = param(request, "action", "list");
        if ("delete".equals(action)) {
            categoryDao.delete(intParam(request, "id", 0));
            redirectList(request, response);
            return;
        }
        list(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("add".equals(param(request, "action", ""))) {
            String name = param(request, "name", "");
            if (!name.isEmpty()) {
                categoryDao.insert(name);
            }
            redirectList(request, response);
            return;
        }
        list(request, response);
    }

    private void list(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("categories", categoryDao.findAll());
        request.getRequestDispatcher("/WEB-INF/views/category_list.jsp").forward(request, response);
    }

    private void redirectList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/category");
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
