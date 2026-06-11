<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="active" value="dashboard" />
<c:set var="pageTitle" value="工作台" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<%-- 关键指标卡片 --%>
<div class="stat-cards">
    <div class="stat-card blue">
        <div class="num">${bookCount}</div>
        <div class="label">馆藏图书(种)</div>
    </div>
    <div class="stat-card green">
        <div class="num">${readerCount}</div>
        <div class="label">登记读者</div>
    </div>
    <div class="stat-card orange">
        <div class="num">${borrowingCount}</div>
        <div class="label">借出中</div>
    </div>
    <div class="stat-card red">
        <div class="num">${overdueCount}</div>
        <div class="label">逾期未还</div>
    </div>
</div>

<%-- 逾期提醒明细 --%>
<div class="panel">
    <div class="panel-head">
        <h2>逾期提醒</h2>
        <a class="btn btn-sm" href="${ctx}/admin/borrow?action=overdue">查看全部</a>
    </div>
    <div class="panel-body">
        <c:choose>
            <c:when test="${empty overdueList}">
                <div class="empty">暂无逾期记录，借阅情况良好。</div>
            </c:when>
            <c:otherwise>
                <table class="data">
                    <thead>
                    <tr>
                        <th>书名</th><th>读者</th><th>借阅证号</th>
                        <th>借出时间</th><th>应还时间</th><th>逾期</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="r" items="${overdueList}">
                        <tr>
                            <td>${r.bookTitle}</td>
                            <td>${r.readerName}</td>
                            <td>${r.readerCardNo}</td>
                            <td><fmt:formatDate value="${r.borrowTime}" pattern="yyyy-MM-dd" /></td>
                            <td><fmt:formatDate value="${r.dueTime}" pattern="yyyy-MM-dd" /></td>
                            <td><span class="tag tag-red">已逾期</span></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
