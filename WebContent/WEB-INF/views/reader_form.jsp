<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="active" value="reader" />
<c:set var="pageTitle" value="读者管理" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="panel">
    <div class="panel-head">
        <h2><c:choose><c:when test="${empty reader.id}">读者登记（新增）</c:when><c:otherwise>编辑读者</c:otherwise></c:choose></h2>
        <a class="btn btn-default" href="${ctx}/admin/reader">返回列表</a>
    </div>
    <div class="panel-body">
        <c:if test="${not empty formError}">
            <div class="alert alert-error">${formError}</div>
        </c:if>
        <form method="post" action="${ctx}/admin/reader">
            <input type="hidden" name="action" value="save">
            <input type="hidden" name="id" value="${reader.id}">
            <div class="form-grid">
                <div class="form-group">
                    <label>借阅证号 *</label>
                    <%-- 编辑时证号不可改 --%>
                    <input type="text" name="cardNo" class="form-control" value="${reader.cardNo}"
                           ${not empty reader.id ? 'readonly' : ''} required>
                </div>
                <div class="form-group">
                    <label>姓名 *</label>
                    <input type="text" name="name" class="form-control" value="${reader.name}" required>
                </div>
                <div class="form-group">
                    <label>性别</label>
                    <select name="gender" class="form-control">
                        <option ${reader.gender=='女'?'':'selected'}>男</option>
                        <option ${reader.gender=='女'?'selected':''}>女</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>读者类型</label>
                    <select name="readerType" class="form-control">
                        <option ${reader.readerType=='教师'||reader.readerType=='职工'?'':'selected'}>学生</option>
                        <option ${reader.readerType=='教师'?'selected':''}>教师</option>
                        <option ${reader.readerType=='职工'?'selected':''}>职工</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>联系电话</label>
                    <input type="text" name="phone" class="form-control" value="${reader.phone}">
                </div>
                <div class="form-group">
                    <label>最大可借数量</label>
                    <input type="number" name="maxBorrow" class="form-control" min="1"
                           value="${empty reader ? 5 : reader.maxBorrow}">
                </div>
                <c:if test="${not empty reader.id}">
                    <div class="form-group">
                        <label>状态</label>
                        <select name="status" class="form-control">
                            <option value="1" ${reader.status==1?'selected':''}>正常</option>
                            <option value="0" ${reader.status==0?'selected':''}>注销</option>
                        </select>
                    </div>
                </c:if>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-success">保存</button>
                <a class="btn btn-default" href="${ctx}/admin/reader">取消</a>
            </div>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
