package com.library.servlet;

import com.library.dao.ReaderDao;
import com.library.entity.PageBean;
import com.library.entity.Reader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 读者管理 Servlet（读者登记/查询/编辑/注销删除）。
 * action: list / add / edit / save / delete。
 */
@WebServlet("/admin/reader")
public class ReaderServlet extends HttpServlet {

    private final ReaderDao readerDao = new ReaderDao();
    private static final int PAGE_SIZE = 8;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = param(request, "action", "list");
        switch (action) {
            case "add":
                showForm(request, response, null);
                break;
            case "edit":
                showForm(request, response, readerDao.findById(intParam(request, "id", 0)));
                break;
            case "delete":
                readerDao.delete(intParam(request, "id", 0));
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
        String keyword = param(request, "keyword", "");
        int pageNum = Math.max(1, intParam(request, "pageNum", 1));
        long total = readerDao.count(keyword);
        List<Reader> readers = readerDao.findByPage(keyword, (pageNum - 1) * PAGE_SIZE, PAGE_SIZE);
        request.setAttribute("page", new PageBean<>(pageNum, PAGE_SIZE, total, readers));
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/WEB-INF/views/reader_list.jsp").forward(request, response);
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Reader reader)
            throws ServletException, IOException {
        request.setAttribute("reader", reader);
        request.getRequestDispatcher("/WEB-INF/views/reader_form.jsp").forward(request, response);
    }

    private void save(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String id = param(request, "id", "");
        String cardNo = param(request, "cardNo", "");

        Reader r = new Reader();
        r.setCardNo(cardNo);
        r.setName(param(request, "name", ""));
        r.setGender(param(request, "gender", "男"));
        r.setPhone(param(request, "phone", ""));
        r.setReaderType(param(request, "readerType", "学生"));
        r.setMaxBorrow(intParam(request, "maxBorrow", 5));
        r.setStatus(intParam(request, "status", 1));

        if (id == null || id.isEmpty()) {
            // 登记新读者前先校验借阅证号唯一
            if (readerDao.findByCardNo(cardNo) != null) {
                request.setAttribute("reader", r);
                request.setAttribute("formError", "借阅证号已存在：" + cardNo);
                request.getRequestDispatcher("/WEB-INF/views/reader_form.jsp").forward(request, response);
                return;
            }
            readerDao.insert(r);
        } else {
            r.setId(Integer.parseInt(id));
            readerDao.update(r);
        }
        redirectList(request, response);
    }

    private void redirectList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/reader");
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
