package com.cakeshop.servlet;

import com.cakeshop.model.User;
import com.cakeshop.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 基础Servlet，提供通用方法
 */
public abstract class BaseServlet extends HttpServlet {

    protected static final Gson gson = new Gson();

    /**
     * 读取请求体JSON
     */
    protected JsonObject readJsonBody(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();
        if (json.isEmpty()) {
            return new JsonObject();
        }
        return JsonParser.parseString(json).getAsJsonObject();
    }

    /**
     * 获取当前登录用户
     */
    protected User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute("user");
        }
        return null;
    }

    /**
     * 获取请求参数（字符串）
     */
    protected String getParam(HttpServletRequest request, String name) {
        return request.getParameter(name);
    }

    /**
     * 获取请求参数（整数）
     */
    protected Integer getIntParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取请求参数（整数，带默认值）
     */
    protected int getIntParam(HttpServletRequest request, String name, int defaultValue) {
        Integer value = getIntParam(request, name);
        return value != null ? value : defaultValue;
    }

    /**
     * 从JSON获取字符串
     */
    protected String getJsonString(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return null;
    }

    /**
     * 从JSON获取整数
     */
    protected Integer getJsonInt(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsInt();
        }
        return null;
    }

    /**
     * 从JSON获取布尔值
     */
    protected Boolean getJsonBoolean(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsBoolean();
        }
        return null;
    }

    /**
     * 写入成功响应
     */
    protected void success(HttpServletResponse response, Object data) throws IOException {
        JsonUtil.writeSuccess(response, data);
    }

    /**
     * 写入成功响应（带消息）
     */
    protected void success(HttpServletResponse response, String message, Object data) throws IOException {
        JsonUtil.writeSuccess(response, message, data);
    }

    /**
     * 写入成功响应（无数据）
     */
    protected void success(HttpServletResponse response, String message) throws IOException {
        JsonUtil.writeSuccess(response, message);
    }

    /**
     * 写入错误响应
     */
    protected void error(HttpServletResponse response, int statusCode, String message) throws IOException {
        JsonUtil.writeError(response, statusCode, message);
    }

    /**
     * 检查字符串非空
     */
    protected boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 验证手机号格式
     */
    protected boolean isValidPhone(String phone) {
        if (isBlank(phone)) {
            return false;
        }
        return phone.matches("^1[3-9]\\d{9}$");
    }
}
