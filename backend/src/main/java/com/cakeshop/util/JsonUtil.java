package com.cakeshop.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON工具类
 */
public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    /**
     * 对象转JSON字符串
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * JSON字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * 写入成功响应
     */
    public static void writeSuccess(HttpServletResponse response, Object data) throws IOException {
        writeResponse(response, 200, true, "操作成功", data);
    }

    /**
     * 写入成功响应（带消息）
     */
    public static void writeSuccess(HttpServletResponse response, String message, Object data) throws IOException {
        writeResponse(response, 200, true, message, data);
    }

    /**
     * 写入成功响应（无数据）
     */
    public static void writeSuccess(HttpServletResponse response, String message) throws IOException {
        writeResponse(response, 200, true, message, null);
    }

    /**
     * 写入错误响应
     */
    public static void writeError(HttpServletResponse response, int statusCode, String message) throws IOException {
        writeResponse(response, statusCode, false, message, null);
    }

    /**
     * 写入响应
     */
    private static void writeResponse(HttpServletResponse response, int statusCode, boolean success, String message, Object data) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("code", statusCode);
        if (data != null) {
            result.put("data", data);
        }

        PrintWriter out = response.getWriter();
        out.print(toJson(result));
        out.flush();
    }

    /**
     * 创建分页响应
     */
    public static Map<String, Object> createPageResult(Object list, int page, int pageSize, int total) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", total);
        result.put("totalPages", (total + pageSize - 1) / pageSize);
        return result;
    }
}
