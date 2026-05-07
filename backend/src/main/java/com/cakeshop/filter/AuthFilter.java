package com.cakeshop.filter;

import com.cakeshop.model.User;
import com.cakeshop.util.JsonUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 登录拦截过滤器
 * 拦截未登录用户访问需要登录的接口
 */
@WebFilter({"/api/user/*", "/api/cart/*", "/api/order/*", "/api/address/*"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("AuthFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 处理OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = null;
        if (session != null) {
            user = (User) session.getAttribute("user");
        }

        if (user == null) {
            resp.setContentType("application/json;charset=UTF-8");
            JsonUtil.writeError(resp, 401, "请先登录");
            return;
        }

        // 将用户信息放入请求属性，方便后续使用
        req.setAttribute("currentUser", user);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("AuthFilter destroyed");
    }
}
