package com.cakeshop.servlet;

import com.cakeshop.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查Servlet
 */
@WebServlet("/api/health")
public class HealthServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());

        // 检查数据库连接
        boolean dbHealthy = DBUtil.testConnection();
        health.put("database", dbHealthy ? "UP" : "DOWN");

        if (dbHealthy) {
            success(response, health);
        } else {
            error(response, 503, "Database connection failed");
        }
    }
}
