<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="active" value="borrowForm" />
<c:set var="pageTitle" value="借阅登记" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="panel">
    <div class="panel-head">
        <h2>借阅登记</h2>
        <a class="btn btn-default" href="${ctx}/admin/borrow">借阅记录</a>
    </div>
    <div class="panel-body">
        <c:if test="${not empty formError}">
            <div class="alert alert-error">${formError}</div>
        </c:if>
        <form method="post" action="${ctx}/admin/borrow" onsubmit="return validateBorrow(this)">
            <input type="hidden" name="action" value="doBorrow">
            <div class="form-grid">
                <div class="form-group">
                    <label>选择图书 *</label>
                    <select name="bookId" class="form-control">
                        <option value="">-- 请选择图书 --</option>
                        <c:forEach var="b" items="${books}">
                            <%-- 无可借库存的图书禁用选择 --%>
                            <option value="${b.id}" ${b.availableCount<=0?'disabled':''}>
                                ${b.title}（可借 ${b.availableCount}）
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label>选择读者 *</label>
                    <select name="readerId" class="form-control">
                        <option value="">-- 请选择读者 --</option>
                        <c:forEach var="r" items="${readers}">
                            <option value="${r.id}">${r.name}（${r.cardNo}）</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label>借阅天数</label>
                    <input type="number" name="days" class="form-control" min="1" value="${defaultDays}">
                </div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-success">确认借出</button>
                <a class="btn btn-default" href="${ctx}/admin/borrow">取消</a>
            </div>
        </form>
        <div class="alert alert-info" style="margin-top:16px;">
            提示：借阅会自动校验图书库存与读者可借上限；应还日期 = 借出日期 + 借阅天数。
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
