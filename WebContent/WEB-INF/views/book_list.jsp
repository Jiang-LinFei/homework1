<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="active" value="book" />
<c:set var="pageTitle" value="图书管理" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="panel">
    <div class="panel-head">
        <h2>图书查询</h2>
        <a class="btn btn-success" href="${ctx}/admin/book?action=add">＋ 图书入库</a>
    </div>
    <div class="panel-body">
        <%-- 查询条件：关键字 + 分类 --%>
        <form class="toolbar" method="get" action="${ctx}/admin/book">
            <input type="text" name="keyword" class="form-control" placeholder="书名 / 作者 / ISBN" value="${keyword}">
            <select name="categoryId" class="form-control">
                <option value="0">全部分类</option>
                <c:forEach var="c" items="${categories}">
                    <option value="${c.id}" ${categoryId==c.id?'selected':''}>${c.name}</option>
                </c:forEach>
            </select>
            <button type="submit" class="btn">查询</button>
            <a class="btn btn-default" href="${ctx}/admin/book">重置</a>
        </form>

        <c:choose>
            <c:when test="${empty page.list}">
                <div class="empty">没有查询到图书。</div>
            </c:when>
            <c:otherwise>
                <table class="data">
                    <thead>
                    <tr>
                        <th>ISBN</th><th>书名</th><th>作者</th><th>出版社</th>
                        <th>分类</th><th>位置</th><th>库存(可借/总)</th><th>单价</th><th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="b" items="${page.list}">
                        <tr>
                            <td>${b.isbn}</td>
                            <td>${b.title}</td>
                            <td>${b.author}</td>
                            <td>${b.publisher}</td>
                            <td><span class="tag tag-blue">${b.categoryName}</span></td>
                            <td>${b.location}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${b.availableCount > 0}">
                                        <span class="tag tag-green">${b.availableCount}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="tag tag-red">0</span>
                                    </c:otherwise>
                                </c:choose>
                                / ${b.totalCount}
                            </td>
                            <td><fmt:formatNumber value="${b.price}" type="currency" currencySymbol="￥"/></td>
                            <td>
                                <a class="btn btn-sm btn-warning" href="${ctx}/admin/book?action=edit&id=${b.id}">编辑</a>
                                <a class="btn btn-sm btn-danger" href="${ctx}/admin/book?action=delete&id=${b.id}"
                                   onclick="return confirmAction('确定删除《${b.title}》？')">删除</a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>

                <%-- 分页 --%>
                <div class="pagination">
                    <c:if test="${page.hasPrev}">
                        <a href="${ctx}/admin/book?keyword=${keyword}&categoryId=${categoryId}&pageNum=${page.pageNum-1}">上一页</a>
                    </c:if>
                    <span class="cur">${page.pageNum} / ${page.totalPages}</span>
                    <c:if test="${page.hasNext}">
                        <a href="${ctx}/admin/book?keyword=${keyword}&categoryId=${categoryId}&pageNum=${page.pageNum+1}">下一页</a>
                    </c:if>
                    <span class="info">共 ${page.total} 条记录</span>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
