package com.library.servlet;

import com.library.dao.BookDao;
import com.library.dao.BorrowRecordDao;
import com.library.dao.ReaderDao;
import com.library.entity.BorrowRecord;
import com.library.entity.LoginUser;
import com.library.entity.PageBean;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 借阅业务 Servlet：借阅登记、归还登记、借阅记录查询、逾期查询。
 * <ul>
 *   <li>list      借阅记录列表(可按状态/逾期筛选)</li>
 *   <li>overdue   逾期记录列表(逾期提醒)</li>
 *   <li>borrowForm 打开借阅登记表单</li>
 *   <li>doBorrow  执行借阅登记</li>
 *   <li>return    归还登记</li>
 * </ul>
 */
@WebServlet("/admin/borrow")
public class BorrowServlet extends HttpServlet {

    private final BorrowRecordDao borrowDao = new BorrowRecordDao();
    private final BookDao bookDao = new BookDao();
    private final ReaderDao readerDao = new ReaderDao();

    private static final int PAGE_SIZE = 8;
    /** 默认借阅天数 */
    private static final int DEFAULT_BORROW_DAYS = 30;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = param(request, "action", "list");
        switch (action) {
            case "borrowForm":
                showBorrowForm(request, response);
                break;
            case "return":
                doReturn(request, response);
                break;
            case "overdue":
                list(request, response, true);
                break;
            default:
                list(request, response, false);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("doBorrow".equals(param(request, "action", ""))) {
            doBorrow(request, response);
        } else {
            list(request, response, false);
        }
    }

    /** 借阅记录列表；forceOverdue=true 时强制只看逾期 */
    private void list(HttpServletRequest request, HttpServletResponse response, boolean forceOverdue)
            throws ServletException, IOException {
        String keyword = param(request, "keyword", "");
        int status = intParam(request, "status", 0);
        boolean onlyOverdue = forceOverdue || "1".equals(request.getParameter("onlyOverdue"));
        int pageNum = Math.max(1, intParam(request, "pageNum", 1));

        long total = borrowDao.count(keyword, status, onlyOverdue);
        List<BorrowRecord> records = borrowDao.findByPage(keyword, status, onlyOverdue,
                (pageNum - 1) * PAGE_SIZE, PAGE_SIZE);

        request.setAttribute("page", new PageBean<>(pageNum, PAGE_SIZE, total, records));
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("onlyOverdue", onlyOverdue);
        request.getRequestDispatcher("/WEB-INF/views/borrow_list.jsp").forward(request, response);
    }

    /** 打开借阅登记表单：提供可借图书和读者列表供选择 */
    private void showBorrowForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 只列出仍有库存的图书(取前若干本，避免数据量过大)
        request.setAttribute("books", bookDao.findByPage(null, 0, 0, 500));
        request.setAttribute("readers", readerDao.findAllActive());
        request.setAttribute("defaultDays", DEFAULT_BORROW_DAYS);
        request.getRequestDispatcher("/WEB-INF/views/borrow_form.jsp").forward(request, response);
    }

    /** 执行借阅登记 */
    private void doBorrow(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int bookId = intParam(request, "bookId", 0);
        int readerId = intParam(request, "readerId", 0);
        int days = intParam(request, "days", DEFAULT_BORROW_DAYS);
        LoginUser user = (LoginUser) request.getSession().getAttribute(LoginUser.SESSION_KEY);
        int operatorId = user == null || user.getId() == null ? 0 : user.getId();

        if (bookId <= 0 || readerId <= 0) {
            backToFormWithError(request, response, "请选择图书和读者");
            return;
        }

        String err = borrowDao.borrow(bookId, readerId, operatorId, days);
        if (err != null) {
            backToFormWithError(request, response, err);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/admin/borrow");
    }

    /** 归还登记 */
    private void doReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = intParam(request, "id", 0);
        borrowDao.returnBook(id);
        // 归还后保持在原筛选条件的列表
        String back = request.getParameter("back");
        response.sendRedirect(request.getContextPath() + "/admin/borrow"
                + (back != null && !back.isEmpty() ? back : ""));
    }

    private void backToFormWithError(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("formError", error);
        showBorrowForm(request, response);
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
