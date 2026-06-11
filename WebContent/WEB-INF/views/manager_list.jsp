<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="active" value="manager" />
<c:set var="pageTitle" value="管理员账号" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="panel">
    <div class="panel-head">
        <h2>普通管理员账号</h2>
        <a class="btn btn-success" href="${ctx}/admin/manager?action=add">＋ 新增管理员</a>
    </div>
    <div class="panel-body">
        <c:choose>
            <c:when test="${empty managers}">
                <div class="empty">暂无普通管理员账号。</div>
            </c:when>
            <c:otherwise>
                <table class="data">
                    <thead>
                    <tr>
                        <th>账号</th><th>姓名</th><th>电话</th><th>状态</th><th>创建时间</th><th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="m" items="${managers}">
                        <tr>
                            <td>${m.username}</td>
                            <td>${m.realName}</td>
                            <td>${m.phone}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${m.status==1}"><span class="tag tag-green">启用</span></c:when>
                                    <c:otherwise><span class="tag tag-gray">禁用</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td><fmt:formatDate value="${m.createTime}" pattern="yyyy-MM-dd HH:mm" /></td>
                            <td>
                                <a class="btn btn-sm btn-warning" href="${ctx}/admin/manager?action=edit&id=${m.id}">编辑</a>
                                <a class="btn btn-sm btn-danger" href="${ctx}/admin/manager?action=delete&id=${m.id}"
                                   onclick="return confirmAction('确定删除账号 ${m.username}？')">删除</a>
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
