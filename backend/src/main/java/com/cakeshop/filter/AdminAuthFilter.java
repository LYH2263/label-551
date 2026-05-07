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
 * 管理员权限拦截过滤器
 * 拦截非管理员用户访问后台接口
 */
@WebFilter("/api/admin/*")
public class AdminAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("AdminAuthFilter initialized");
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

        if (!"admin".equals(user.getRole())) {
            resp.setContentType("application/json;charset=UTF-8");
            JsonUtil.writeError(resp, 403, "无权限访问");
            return;
        }

        // 将用户信息放入请求属性
        req.setAttribute("currentUser", user);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("AdminAuthFilter destroyed");
    }
}
