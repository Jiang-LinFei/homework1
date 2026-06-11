<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="active" value="category" />
<c:set var="pageTitle" value="图书分类" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="panel">
    <div class="panel-head"><h2>新增分类</h2></div>
    <div class="panel-body">
        <form class="toolbar" method="post" action="${ctx}/admin/category">
            <input type="hidden" name="action" value="add">
            <input type="text" name="name" class="form-control" placeholder="分类名称" required>
            <button type="submit" class="btn btn-success">新增</button>
        </form>
    </div>
</div>

<div class="panel">
    <div class="panel-head"><h2>分类列表</h2></div>
    <div class="panel-body">
        <c:choose>
            <c:when test="${empty categories}">
                <div class="empty">暂无图书分类。</div>
            </c:when>
            <c:otherwise>
                <table class="data">
                    <thead><tr><th>ID</th><th>分类名称</th><th>操作</th></tr></thead>
                    <tbody>
                    <c:forEach var="c" items="${categories}">
                        <tr>
                            <td>${c.id}</td>
                            <td>${c.name}</td>
                            <td>
                                <a class="btn btn-sm btn-danger" href="${ctx}/admin/category?action=delete&id=${c.id}"
                                   onclick="return confirmAction('删除分类「${c.name}」？该分类下的图书将变为未分类。')">删除</a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
