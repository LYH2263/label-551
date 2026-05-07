package com.cakeshop.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 字符编码过滤器
 * 统一处理中文乱码问题
 */
@WebFilter("/*")
public class CharacterEncodingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("CharacterEncodingFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 设置请求的字符编码
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // 只对非静态资源设置 Content-Type
        String uri = req.getRequestURI();
        if (!isStaticResource(uri)) {
            resp.setContentType("text/html;charset=UTF-8");
        }

        chain.doFilter(request, response);
    }

    /**
     * 判断是否为静态资源
     */
    private boolean isStaticResource(String uri) {
        return uri.endsWith(".css") || uri.endsWith(".js") ||
               uri.endsWith(".png") || uri.endsWith(".jpg") ||
               uri.endsWith(".jpeg") || uri.endsWith(".gif") ||
               uri.endsWith(".svg") || uri.endsWith(".ico") ||
               uri.endsWith(".woff") || uri.endsWith(".woff2") ||
               uri.endsWith(".ttf") || uri.endsWith(".eot") ||
               uri.startsWith("/css/") || uri.startsWith("/js/") ||
               uri.startsWith("/images/") || uri.startsWith("/uploads/");
    }

    @Override
    public void destroy() {
        System.out.println("CharacterEncodingFilter destroyed");
    }
}
