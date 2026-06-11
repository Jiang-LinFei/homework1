package com.library.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 统一编码过滤器。
 * 将请求/响应编码设为 UTF-8，避免中文表单提交、页面输出出现乱码。
 * 拦截所有请求(/*)。
 */
@WebFilter(filterName = "characterEncodingFilter", urlPatterns = "/*")
public class CharacterEncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        if (response instanceof HttpServletResponse) {
            response.setCharacterEncoding("UTF-8");
        }
        chain.doFilter(request, response);
    }
}
