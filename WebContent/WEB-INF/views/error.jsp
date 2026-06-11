<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="active" value="" />
<c:set var="pageTitle" value="提示" />
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="panel">
    <div class="panel-body">
        <div class="alert alert-error">${empty msg ? '操作出现异常' : msg}</div>
        <a class="btn btn-default" href="${ctx}/admin/dashboard">返回工作台</a>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
