<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.library.dao.BorrowRecordDao" %>
<%--
    公共头部：输出 <html> 头、侧边栏、顶栏，并打开 .content 容器。
    引入此文件前，页面应先用 <c:set> 设置 active(当前菜单) 与 pageTitle(标题)。
    未登录时跳回登录页（双保险，正常已由 AuthFilter 拦截）。
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:if test="${empty sessionScope.loginUser}">
    <c:redirect url="/login.jsp" />
</c:if>
<%-- 侧边栏逾期数量红点：统计当前逾期记录数 --%>
<% request.setAttribute("sidebarOverdue", new BorrowRecordDao().countOverdue()); %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} - 图书馆信息管理系统</title>
    <link rel="stylesheet" href="${ctx}/css/style.css">
    <script src="${ctx}/js/app.js"></script>
</head>
<body>
<div class="layout">
    <aside class="sidebar">
        <div class="brand">📚 图书馆管理系统</div>
        <nav class="menu">
            <a href="${ctx}/admin/dashboard" class="${active=='dashboard'?'active':''}">工作台</a>
            <div class="group-title">日常业务</div>
            <a href="${ctx}/admin/book" class="${active=='book'?'active':''}">图书管理</a>
            <a href="${ctx}/admin/reader" class="${active=='reader'?'active':''}">读者管理</a>
            <a href="${ctx}/admin/borrow?action=borrowForm" class="${active=='borrowForm'?'active':''}">借阅登记</a>
            <a href="${ctx}/admin/borrow" class="${active=='borrow'?'active':''}">借阅记录</a>
            <a href="${ctx}/admin/borrow?action=overdue" class="${active=='overdue'?'active':''}">
                逾期提醒
                <c:if test="${sidebarOverdue > 0}"><span class="badge">${sidebarOverdue}</span></c:if>
            </a>
            <c:if test="${sessionScope.loginUser.sysAdmin}">
                <div class="group-title">系统管理</div>
                <a href="${ctx}/admin/manager" class="${active=='manager'?'active':''}">管理员账号</a>
                <a href="${ctx}/admin/category" class="${active=='category'?'active':''}">图书分类</a>
            </c:if>
        </nav>
    </aside>
    <div class="main">
        <div class="topbar">
            <div class="title">${pageTitle}</div>
            <div class="user-info">
                <b>${sessionScope.loginUser.realName}</b>
                （${sessionScope.loginUser.roleName}）
                <a href="${ctx}/admin/logout" onclick="return confirmAction('确定退出登录？')">退出</a>
            </div>
        </div>
        <div class="content">
