<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 入口页：已登录跳工作台，未登录跳登录页 --%>
<%
    Object user = session.getAttribute("loginUser");
    String ctx = request.getContextPath();
    if (user != null) {
        response.sendRedirect(ctx + "/admin/dashboard");
    } else {
        response.sendRedirect(ctx + "/login.jsp");
    }
%>
