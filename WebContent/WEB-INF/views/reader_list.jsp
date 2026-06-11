<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="active" value="reader" />
<c:set var="pageTitle" value="读者管理" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="panel">
    <div class="panel-head">
        <h2>读者查询</h2>
        <a class="btn btn-success" href="${ctx}/admin/reader?action=add">＋ 读者登记</a>
    </div>
    <div class="panel-body">
        <form class="toolbar" method="get" action="${ctx}/admin/reader">
            <input type="text" name="keyword" class="form-control" placeholder="证号 / 姓名 / 电话" value="${keyword}">
            <button type="submit" class="btn">查询</button>
            <a class="btn btn-default" href="${ctx}/admin/reader">重置</a>
        </form>

        <c:choose>
            <c:when test="${empty page.list}">
                <div class="empty">没有查询到读者。</div>
            </c:when>
            <c:otherwise>
                <table class="data">
                    <thead>
                    <tr>
                        <th>借阅证号</th><th>姓名</th><th>性别</th><th>类型</th>
                        <th>电话</th><th>可借上限</th><th>登记时间</th><th>状态</th><th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="r" items="${page.list}">
                        <tr>
                            <td>${r.cardNo}</td>
                            <td>${r.name}</td>
                            <td>${r.gender}</td>
                            <td>${r.readerType}</td>
                            <td>${r.phone}</td>
                            <td>${r.maxBorrow}</td>
                            <td><fmt:formatDate value="${r.registerTime}" pattern="yyyy-MM-dd" /></td>
                            <td>
                                <c:choose>
                                    <c:when test="${r.status==1}"><span class="tag tag-green">正常</span></c:when>
                                    <c:otherwise><span class="tag tag-gray">注销</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <a class="btn btn-sm btn-warning" href="${ctx}/admin/reader?action=edit&id=${r.id}">编辑</a>
                                <a class="btn btn-sm btn-danger" href="${ctx}/admin/reader?action=delete&id=${r.id}"
                                   onclick="return confirmAction('确定删除读者 ${r.name}？')">删除</a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>

                <div class="pagination">
                    <c:if test="${page.hasPrev}">
                        <a href="${ctx}/admin/reader?keyword=${keyword}&pageNum=${page.pageNum-1}">上一页</a>
                    </c:if>
                    <span class="cur">${page.pageNum} / ${page.totalPages}</span>
                    <c:if test="${page.hasNext}">
                        <a href="${ctx}/admin/reader?keyword=${keyword}&pageNum=${page.pageNum+1}">下一页</a>
                    </c:if>
                    <span class="info">共 ${page.total} 条记录</span>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
