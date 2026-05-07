package com.cakeshop.servlet.admin;

import com.cakeshop.dao.CategoryDao;
import com.cakeshop.model.Category;
import com.cakeshop.servlet.BaseServlet;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 后台分类管理Servlet
 */
@WebServlet("/api/admin/category/*")
public class AdminCategoryServlet extends BaseServlet {

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
            } else if (pathInfo.matches("/\\d+/check")) {
                // 检查分类下是否有商品
                int id = Integer.parseInt(pathInfo.split("/")[1]);
                int count = categoryDao.countProducts(id);
                java.util.Map<String, Object> result = new java.util.HashMap<>();
                result.put("hasProducts", count > 0);
                result.put("productCount", count);
                success(response, result);
            } else {
                // 获取分类详情
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            JsonObject json = readJsonBody(request);

            Category category = new Category();
            category.setName(getJsonString(json, "name"));
            category.setDescription(getJsonString(json, "description"));
            category.setSortOrder(getJsonInt(json, "sortOrder"));

            // 参数校验
            if (isBlank(category.getName())) {
                error(response, 400, "分类名称不能为空");
                return;
            }

            // 检查分类名是否重复
            Category existCategory = categoryDao.findByName(category.getName());
            if (existCategory != null) {
                error(response, 400, "分类名称已存在");
                return;
            }

            int id = categoryDao.insert(category);
            if (id > 0) {
                category.setId(id);
                success(response, "添加成功", category);
            } else {
                error(response, 500, "添加失败，请重试");
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
                error(response, 400, "请指定分类ID");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                error(response, 400, "无效的分类ID");
                return;
            }

            Category existCategory = categoryDao.findById(id);
            if (existCategory == null) {
                error(response, 404, "分类不存在");
                return;
            }

            JsonObject json = readJsonBody(request);

            String name = getJsonString(json, "name");
            String description = getJsonString(json, "description");
            Integer sortOrder = getJsonInt(json, "sortOrder");

            // 参数校验
            if (isBlank(name)) {
                error(response, 400, "分类名称不能为空");
                return;
            }

            // 检查分类名是否重复
            Category sameNameCategory = categoryDao.findByName(name);
            if (sameNameCategory != null && !sameNameCategory.getId().equals(id)) {
                error(response, 400, "分类名称已存在");
                return;
            }

            existCategory.setName(name);
            existCategory.setDescription(description);
            if (sortOrder != null) {
                existCategory.setSortOrder(sortOrder);
            }

            if (categoryDao.update(existCategory)) {
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
                error(response, 400, "请指定分类ID");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                error(response, 400, "无效的分类ID");
                return;
            }

            Category category = categoryDao.findById(id);
            if (category == null) {
                error(response, 404, "分类不存在");
                return;
            }

            // 检查是否强制删除
            String force = getParam(request, "force");
            String deleteProducts = getParam(request, "deleteProducts");
            if ("true".equals(force)) {
                if ("true".equals(deleteProducts)) {
                    // 删除分类下的所有商品
                    categoryDao.deleteProductsByCategory(id);
                } else {
                    // 将关联商品移至未分类(category_id=null)
                    categoryDao.moveProductsToUncategorized(id);
                }
            } else {
                // 检查分类下是否有商品
                if (categoryDao.hasProducts(id)) {
                    error(response, 400, "该分类下存在商品，无法删除");
                    return;
                }
            }

            if (categoryDao.delete(id)) {
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
