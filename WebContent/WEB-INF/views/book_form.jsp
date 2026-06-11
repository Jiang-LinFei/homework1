<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="active" value="book" />
<c:set var="pageTitle" value="图书管理" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="panel">
    <div class="panel-head">
        <h2><c:choose><c:when test="${empty book}">图书入库（新增）</c:when><c:otherwise>编辑图书</c:otherwise></c:choose></h2>
        <a class="btn btn-default" href="${ctx}/admin/book">返回列表</a>
    </div>
    <div class="panel-body">
        <form method="post" action="${ctx}/admin/book">
            <input type="hidden" name="action" value="save">
            <input type="hidden" name="id" value="${book.id}">
            <div class="form-grid">
                <div class="form-group">
                    <label>书名 *</label>
                    <input type="text" name="title" class="form-control" value="${book.title}" required>
                </div>
                <div class="form-group">
                    <label>ISBN</label>
                    <input type="text" name="isbn" class="form-control" value="${book.isbn}">
                </div>
                <div class="form-group">
                    <label>作者</label>
                    <input type="text" name="author" class="form-control" value="${book.author}">
                </div>
                <div class="form-group">
                    <label>出版社</label>
                    <input type="text" name="publisher" class="form-control" value="${book.publisher}">
                </div>
                <div class="form-group">
                    <label>分类</label>
                    <select name="categoryId" class="form-control">
                        <option value="0">未分类</option>
                        <c:forEach var="c" items="${categories}">
                            <option value="${c.id}" ${book.categoryId==c.id?'selected':''}>${c.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label>馆藏位置</label>
                    <input type="text" name="location" class="form-control" value="${book.location}" placeholder="如 A-01-01">
                </div>
                <div class="form-group">
                    <label>馆藏总数 *</label>
                    <input type="number" name="totalCount" class="form-control" min="0"
                           value="${empty book ? 1 : book.totalCount}" required>
                </div>
                <div class="form-group">
                    <label>单价(元)</label>
                    <input type="number" step="0.01" min="0" name="price" class="form-control"
                           value="${empty book ? '0.00' : book.price}">
                </div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-success">保存</button>
                <a class="btn btn-default" href="${ctx}/admin/book">取消</a>
            </div>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
