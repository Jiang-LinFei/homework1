<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 图书馆信息管理系统</title>
    <link rel="stylesheet" href="${ctx}/css/style.css">
</head>
<body class="login-page">
    <div class="login-box">
        <h1>图书馆信息管理系统</h1>
        <div class="sub">Library Information Management System</div>

        <c:if test="${not empty error}">
            <div class="alert alert-error">${error}</div>
        </c:if>

        <form method="post" action="${ctx}/login">
            <%-- 角色选择：普通管理员 / 系统管理员 --%>
            <div class="role-tabs">
                <label>
                    <input type="radio" name="role" value="MGR" ${role=='SYS'?'':'checked'}>
                    <span>普通管理员</span>
                </label>
                <label>
                    <input type="radio" name="role" value="SYS" ${role=='SYS'?'checked':''}>
                    <span>系统管理员</span>
                </label>
            </div>

            <div class="form-group">
                <label>账号</label>
                <input type="text" name="username" class="form-control" value="${username}" placeholder="请输入账号" autofocus>
            </div>
            <div class="form-group">
                <label>密码</label>
                <input type="password" name="password" class="form-control" placeholder="请输入密码">
            </div>
            <button type="submit" class="btn btn-block">登 录</button>
        </form>

        <div class="sub" style="margin-top:18px;">
            演示账号：系统管理员 admin / admin123　·　普通管理员 librarian / 123456
        </div>
    </div>
</body>
</html>
