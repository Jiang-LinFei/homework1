<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="active" value="${onlyOverdue ? 'overdue' : 'borrow'}" />
<c:set var="pageTitle" value="${onlyOverdue ? '逾期提醒' : '借阅记录'}" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="panel">
    <div class="panel-head">
        <h2>${onlyOverdue ? '逾期未还记录' : '借阅记录查询'}</h2>
        <a class="btn btn-success" href="${ctx}/admin/borrow?action=borrowForm">＋ 借阅登记</a>
    </div>
    <div class="panel-body">
        <%-- 逾期视图固定只看逾期，普通视图提供状态筛选 --%>
        <c:if test="${not onlyOverdue}">
            <form class="toolbar" method="get" action="${ctx}/admin/borrow">
                <input type="text" name="keyword" class="form-control" placeholder="书名 / 读者 / 证号" value="${keyword}">
                <select name="status" class="form-control">
                    <option value="0" ${status==0?'selected':''}>全部状态</option>
                    <option value="1" ${status==1?'selected':''}>借出中</option>
                    <option value="2" ${status==2?'selected':''}>已归还</option>
                </select>
                <button type="submit" class="btn">查询</button>
                <a class="btn btn-default" href="${ctx}/admin/borrow">重置</a>
            </form>
        </c:if>

        <c:choose>
            <c:when test="${empty page.list}">
                <div class="empty">${onlyOverdue ? '暂无逾期记录。' : '没有查询到借阅记录。'}</div>
            </c:when>
            <c:otherwise>
                <table class="data">
                    <thead>
                    <tr>
                        <th>书名</th><th>读者</th><th>借阅证号</th>
                        <th>借出时间</th><th>应还时间</th><th>归还时间</th><th>状态</th><th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="r" items="${page.list}">
                        <tr>
                            <td>${r.bookTitle}</td>
                            <td>${r.readerName}</td>
                            <td>${r.readerCardNo}</td>
                            <td><fmt:formatDate value="${r.borrowTime}" pattern="yyyy-MM-dd" /></td>
                            <td><fmt:formatDate value="${r.dueTime}" pattern="yyyy-MM-dd" /></td>
                            <td><fmt:formatDate value="${r.returnTime}" pattern="yyyy-MM-dd" /></td>
                            <td>
                                <c:choose>
                                    <c:when test="${r.status==2}"><span class="tag tag-gray">已归还</span></c:when>
                                    <c:when test="${r.overdue}"><span class="tag tag-red">已逾期</span></c:when>
                                    <c:otherwise><span class="tag tag-green">借出中</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:if test="${r.status==1}">
                                    <a class="btn btn-sm btn-warning" href="${ctx}/admin/borrow?action=return&id=${r.id}"
                                       onclick="return confirmAction('确认办理《${r.bookTitle}》归还？')">归还</a>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>

                <div class="pagination">
                    <c:set var="overdueParam" value="${onlyOverdue ? '&action=overdue' : ''}" />
                    <c:if test="${page.hasPrev}">
                        <a href="${ctx}/admin/borrow?keyword=${keyword}&status=${status}${overdueParam}&pageNum=${page.pageNum-1}">上一页</a>
                    </c:if>
                    <span class="cur">${page.pageNum} / ${page.totalPages}</span>
                    <c:if test="${page.hasNext}">
                        <a href="${ctx}/admin/borrow?keyword=${keyword}&status=${status}${overdueParam}&pageNum=${page.pageNum+1}">下一页</a>
                    </c:if>
                    <span class="info">共 ${page.total} 条记录</span>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
