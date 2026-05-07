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
 * 用户信息Servlet（个人中心）
 */
@WebServlet("/api/user/*")
public class UserServlet extends BaseServlet {

    private UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if ("/profile".equals(pathInfo) || pathInfo == null || pathInfo.equals("/")) {
                getProfile(request, response);
            } else {
                error(response, 404, "接口不存在");
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
            if ("/profile".equals(pathInfo)) {
                updateProfile(request, response);
            } else if ("/password".equals(pathInfo)) {
                updatePassword(request, response);
            } else {
                error(response, 404, "接口不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }

    /**
     * 获取个人信息
     */
    private void getProfile(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = getCurrentUser(request);
        if (user == null) {
            error(response, 401, "请先登录");
            return;
        }

        // 重新从数据库获取最新信息
        User freshUser = userDao.findById(user.getId());
        if (freshUser == null) {
            error(response, 404, "用户不存在");
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", freshUser.getId());
        userData.put("phone", freshUser.getPhone());
        userData.put("username", freshUser.getUsername());
        userData.put("role", freshUser.getRole());
        userData.put("createdAt", freshUser.getCreatedAt());

        success(response, userData);
    }

    /**
     * 更新个人信息
     */
    private void updateProfile(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = getCurrentUser(request);
        if (user == null) {
            error(response, 401, "请先登录");
            return;
        }

        JsonObject json = readJsonBody(request);

        String username = getJsonString(json, "username");
        String phone = getJsonString(json, "phone");

        // 参数校验
        if (isBlank(username)) {
            error(response, 400, "用户名不能为空");
            return;
        }

        if (phone != null && !phone.equals(user.getPhone())) {
            if (!isValidPhone(phone)) {
                error(response, 400, "手机号格式不正确");
                return;
            }
            // 检查手机号是否已被使用
            User existUser = userDao.findByPhone(phone);
            if (existUser != null && !existUser.getId().equals(user.getId())) {
                error(response, 400, "该手机号已被使用");
                return;
            }
        }

        // 更新用户信息
        user.setUsername(username);
        if (phone != null) {
            user.setPhone(phone);
        }

        if (userDao.update(user)) {
            // 更新Session中的用户信息
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("user", user);
            }
            success(response, "修改成功");
        } else {
            error(response, 500, "修改失败，请重试");
        }
    }

    /**
     * 修改密码
     */
    private void updatePassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = getCurrentUser(request);
        if (user == null) {
            error(response, 401, "请先登录");
            return;
        }

        JsonObject json = readJsonBody(request);

        String oldPassword = getJsonString(json, "oldPassword");
        String newPassword = getJsonString(json, "newPassword");

        // 参数校验
        if (isBlank(oldPassword)) {
            error(response, 400, "原密码不能为空");
            return;
        }
        if (isBlank(newPassword)) {
            error(response, 400, "新密码不能为空");
            return;
        }
        if (newPassword.length() < 6) {
            error(response, 400, "新密码长度不能少于6位");
            return;
        }

        // 验证原密码
        User freshUser = userDao.findById(user.getId());
        if (!PasswordUtil.verify(oldPassword, freshUser.getPassword())) {
            error(response, 400, "原密码错误");
            return;
        }

        // 更新密码
        String encryptedPassword = PasswordUtil.encrypt(newPassword);
        if (userDao.updatePassword(user.getId(), encryptedPassword)) {
            success(response, "密码修改成功");
        } else {
            error(response, 500, "密码修改失败，请重试");
        }
    }
}
