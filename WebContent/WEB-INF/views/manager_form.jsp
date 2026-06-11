<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="active" value="manager" />
<c:set var="pageTitle" value="管理员账号" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="panel">
    <div class="panel-head">
        <h2><c:choose><c:when test="${empty manager.id}">新增普通管理员</c:when><c:otherwise>编辑管理员</c:otherwise></c:choose></h2>
        <a class="btn btn-default" href="${ctx}/admin/manager">返回列表</a>
    </div>
    <div class="panel-body">
        <c:if test="${not empty formError}">
            <div class="alert alert-error">${formError}</div>
        </c:if>
        <form method="post" action="${ctx}/admin/manager">
            <input type="hidden" name="action" value="save">
            <input type="hidden" name="id" value="${manager.id}">
            <div class="form-grid">
                <div class="form-group">
                    <label>登录账号 *</label>
                    <input type="text" name="username" class="form-control" value="${manager.username}"
                           ${not empty manager.id ? 'readonly' : ''} required>
                </div>
                <div class="form-group">
                    <label>真实姓名</label>
                    <input type="text" name="realName" class="form-control" value="${manager.realName}">
                </div>
                <div class="form-group">
                    <label>登录密码 <c:if test="${not empty manager.id}">（留空不修改）</c:if></label>
                    <input type="password" name="password" class="form-control" placeholder="${empty manager.id ? '请输入初始密码' : '不修改请留空'}">
                </div>
                <div class="form-group">
                    <label>联系电话</label>
                    <input type="text" name="phone" class="form-control" value="${manager.phone}">
                </div>
                <div class="form-group">
                    <label>状态</label>
                    <select name="status" class="form-control">
                        <option value="1" ${manager.status==0?'':'selected'}>启用</option>
                        <option value="0" ${manager.status==0?'selected':''}>禁用</option>
                    </select>
                </div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-success">保存</button>
                <a class="btn btn-default" href="${ctx}/admin/manager">取消</a>
            </div>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
