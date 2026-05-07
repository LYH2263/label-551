package com.cakeshop.servlet.admin;

import com.cakeshop.dao.UserDao;
import com.cakeshop.model.User;
import com.cakeshop.servlet.BaseServlet;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台用户管理Servlet
 */
@WebServlet("/api/admin/user/*")
public class AdminUserServlet extends BaseServlet {

    private UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
                // 获取用户列表
                List<User> users = userDao.findAll();

                // 转换为不含密码的Map列表
                List<Map<String, Object>> userList = new ArrayList<>();
                for (User user : users) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", user.getId());
                    userData.put("phone", user.getPhone());
                    userData.put("username", user.getUsername());
                    userData.put("role", user.getRole());
                    userData.put("createdAt", user.getCreatedAt());
                    userList.add(userData);
                }

                Map<String, Object> result = new HashMap<>();
                result.put("list", userList);
                result.put("total", userDao.count());

                success(response, result);
            } else {
                // 获取用户详情
                try {
                    int id = Integer.parseInt(pathInfo.substring(1));
                    User user = userDao.findById(id);
                    if (user == null) {
                        error(response, 404, "用户不存在");
                        return;
                    }

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", user.getId());
                    userData.put("phone", user.getPhone());
                    userData.put("username", user.getUsername());
                    userData.put("role", user.getRole());
                    userData.put("createdAt", user.getCreatedAt());

                    success(response, userData);
                } catch (NumberFormatException e) {
                    error(response, 400, "无效的用户ID");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                error(response, 400, "请指定用户ID");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                error(response, 400, "无效的用户ID");
                return;
            }

            User user = userDao.findById(id);
            if (user == null) {
                error(response, 404, "用户不存在");
                return;
            }

            JsonObject json = readJsonBody(request);
            String username = getJsonString(json, "username");
            String role = getJsonString(json, "role");

            if (username != null && !username.trim().isEmpty()) {
                user.setUsername(username.trim());
            }
            if (role != null && ("user".equals(role) || "admin".equals(role))) {
                user.setRole(role);
            }

            if (userDao.update(user)) {
                success(response, "修改成功");
            } else {
                error(response, 500, "修改失败，请重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                error(response, 400, "请指定用户ID");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                error(response, 400, "无效的用户ID");
                return;
            }

            User user = userDao.findById(id);
            if (user == null) {
                error(response, 404, "用户不存在");
                return;
            }

            // 不允许删除管理员
            if ("admin".equals(user.getRole())) {
                error(response, 400, "不能删除管理员账号");
                return;
            }

            if (userDao.delete(id)) {
                success(response, "删除成功");
            } else {
                error(response, 500, "删除失败，请重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }
}
