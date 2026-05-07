package com.cakeshop.servlet;

import com.cakeshop.dao.CategoryDao;
import com.cakeshop.model.Category;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 蛋糕分类Servlet（前台）
 */
@WebServlet("/api/category/*")
public class CategoryServlet extends BaseServlet {

    private CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
                // 获取分类列表
                List<Category> categories = categoryDao.findAll();
                success(response, categories);
            } else {
                // 获取单个分类
                try {
                    int id = Integer.parseInt(pathInfo.substring(1));
                    Category category = categoryDao.findById(id);
                    if (category == null) {
                        error(response, 404, "分类不存在");
                        return;
                    }
                    success(response, category);
                } catch (NumberFormatException e) {
                    error(response, 400, "无效的分类ID");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }
}
