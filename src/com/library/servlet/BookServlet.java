package com.library.servlet;

import com.library.dao.BookCategoryDao;
import com.library.dao.BookDao;
import com.library.entity.Book;
import com.library.entity.PageBean;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * 图书管理 Servlet，统一通过 action 参数分发：
 * <ul>
 *   <li>list   图书查询/列表(分页+关键字+分类筛选)</li>
 *   <li>add    打开新增(入库)表单</li>
 *   <li>edit   打开编辑表单</li>
 *   <li>save   保存(新增或更新)</li>
 *   <li>delete 删除</li>
 * </ul>
 */
@WebServlet("/admin/book")
public class BookServlet extends HttpServlet {

    private final BookDao bookDao = new BookDao();
    private final BookCategoryDao categoryDao = new BookCategoryDao();

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
                showForm(request, response, bookDao.findById(intParam(request, "id", 0)));
                break;
            case "delete":
                bookDao.delete(intParam(request, "id", 0));
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

    /** 图书查询/分页列表 */
    private void list(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = param(request, "keyword", "");
        int categoryId = intParam(request, "categoryId", 0);
        int pageNum = Math.max(1, intParam(request, "pageNum", 1));

        long total = bookDao.count(keyword, categoryId);
        List<Book> books = bookDao.findByPage(keyword, categoryId, (pageNum - 1) * PAGE_SIZE, PAGE_SIZE);

        request.setAttribute("page", new PageBean<>(pageNum, PAGE_SIZE, total, books));
        request.setAttribute("keyword", keyword);
        request.setAttribute("categoryId", categoryId);
        request.setAttribute("categories", categoryDao.findAll());
        request.getRequestDispatcher("/WEB-INF/views/book_list.jsp").forward(request, response);
    }

    /** 打开新增/编辑表单 */
    private void showForm(HttpServletRequest request, HttpServletResponse response, Book book)
            throws ServletException, IOException {
        request.setAttribute("book", book);
        request.setAttribute("categories", categoryDao.findAll());
        request.getRequestDispatcher("/WEB-INF/views/book_form.jsp").forward(request, response);
    }

    /** 保存图书（id 为空则入库新增，否则更新） */
    private void save(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Book b = new Book();
        String id = param(request, "id", "");
        b.setIsbn(param(request, "isbn", ""));
        b.setTitle(param(request, "title", ""));
        b.setAuthor(param(request, "author", ""));
        b.setPublisher(param(request, "publisher", ""));
        Integer categoryId = intParam(request, "categoryId", 0);
        b.setCategoryId(categoryId > 0 ? categoryId : null);
        b.setLocation(param(request, "location", ""));
        int total = intParam(request, "totalCount", 0);
        b.setTotalCount(total);
        b.setPrice(new BigDecimal(param(request, "price", "0")));

        if (id == null || id.isEmpty()) {
            // 新书入库：可借数量默认等于馆藏总数
            b.setAvailableCount(total);
            bookDao.insert(b);
        } else {
            b.setId(Integer.parseInt(id));
            // 编辑时：根据已借出数量(原总数-原可借)推算新的可借数量，避免覆盖在借库存
            Book old = bookDao.findById(b.getId());
            int borrowed = old.getTotalCount() - old.getAvailableCount();
            int available = total - borrowed;
            b.setAvailableCount(Math.max(available, 0));
            bookDao.update(b);
        }
        redirectList(request, response);
    }

    private void redirectList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/book");
    }

    // ---------- 参数辅助 ----------
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
