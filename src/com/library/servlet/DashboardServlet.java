package com.library.servlet;

import com.library.dao.BookDao;
import com.library.dao.BorrowRecordDao;
import com.library.dao.ReaderDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 首页/工作台 Servlet。
 * 汇总图书总数、读者总数、借出中数量、逾期数量等关键指标，
 * 让管理员一进系统就能看到馆藏与借阅概况，并对逾期进行提醒。
 */
@WebServlet("/admin/dashboard")
public class DashboardServlet extends HttpServlet {

    private final BookDao bookDao = new BookDao();
    private final ReaderDao readerDao = new ReaderDao();
    private final BorrowRecordDao borrowDao = new BorrowRecordDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("bookCount", bookDao.count(null, 0));
        request.setAttribute("readerCount", readerDao.count(null));
        request.setAttribute("borrowingCount", borrowDao.count(null, 1, false)); // 借出中
        long overdue = borrowDao.countOverdue();
        request.setAttribute("overdueCount", overdue);
        // 逾期明细(最多展示若干条)用于首页提醒
        request.setAttribute("overdueList", borrowDao.findByPage(null, 0, true, 0, 10));
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }
}
