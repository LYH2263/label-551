package com.cakeshop.servlet;

import com.cakeshop.dao.UserDao;
import com.cakeshop.model.User;
import com.cakeshop.util.PasswordUtil;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证相关Servlet（登录/注册/登出）
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends BaseServlet {

    private UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if ("/register".equals(pathInfo)) {
                register(request, response);
            } else if ("/login".equals(pathInfo)) {
                login(request, response);
            } else if ("/logout".equals(pathInfo)) {
                logout(request, response);
            } else {
                error(response, 404, "接口不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if ("/check".equals(pathInfo)) {
                checkLogin(request, response);
            } else {
                error(response, 404, "接口不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }

    /**
     * 用户注册
     */
    private void register(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        JsonObject json = readJsonBody(request);

        String phone = getJsonString(json, "phone");
        String password = getJsonString(json, "password");
        String username = getJsonString(json, "username");

        // 参数校验
        if (isBlank(phone)) {
            error(response, 400, "手机号不能为空");
            return;
        }
        if (!isValidPhone(phone)) {
            error(response, 400, "手机号格式不正确");
            return;
        }
        if (isBlank(password)) {
            error(response, 400, "密码不能为空");
            return;
        }
        if (password.length() < 6) {
            error(response, 400, "密码长度不能少于6位");
            return;
        }
        if (isBlank(username)) {
            error(response, 400, "用户名不能为空");
            return;
        }

        // 检查手机号是否已注册
        User existUser = userDao.findByPhone(phone);
        if (existUser != null) {
            error(response, 400, "该手机号已注册");
            return;
        }

        // 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setPassword(PasswordUtil.encrypt(password));
        user.setUsername(username);
        user.setRole("user");

        int userId = userDao.insert(user);
        if (userId > 0) {
            success(response, "注册成功");
        } else {
            error(response, 500, "注册失败，请重试");
        }
    }

    /**
     * 用户登录
     */
    private void login(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        JsonObject json = readJsonBody(request);

        String phone = getJsonString(json, "phone");
        String password = getJsonString(json, "password");

        // 参数校验
        if (isBlank(phone)) {
            error(response, 400, "手机号不能为空");
            return;
        }
        if (isBlank(password)) {
            error(response, 400, "密码不能为空");
            return;
        }

        // 查询用户
        User user = userDao.findByPhone(phone);
        if (user == null) {
            error(response, 400, "用户不存在");
            return;
        }

        // 验证密码
        if (!PasswordUtil.verify(password, user.getPassword())) {
            error(response, 400, "密码错误");
            return;
        }

        // 创建Session
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        session.setMaxInactiveInterval(30 * 60); // 30分钟

        // 返回用户信息（不包含密码）
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("phone", user.getPhone());
        userData.put("username", user.getUsername());
        userData.put("role", user.getRole());

        success(response, "登录成功", userData);
    }

    /**
     * 用户登出
     */
    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        success(response, "退出成功");
    }

    /**
     * 检查登录状态
     */
    private void checkLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = getCurrentUser(request);
        if (user != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("phone", user.getPhone());
            userData.put("username", user.getUsername());
            userData.put("role", user.getRole());
            success(response, userData);
        } else {
            error(response, 401, "未登录");
        }
    }
}
