package com.ecommerce.order.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 响应字符编码过滤器
 * 确保所有响应都使用UTF-8编码
 */
@WebFilter(filterName = "ResponseEncodingFilter", urlPatterns = "/*")
public class ResponseEncodingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        // 设置请求编码
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        // 设置响应编码
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        // 如果是HttpServletResponse，设置Content-Type
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.setHeader("Content-Type", "application/json;charset=UTF-8");
            httpResponse.setHeader("Content-Encoding", "UTF-8");
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}