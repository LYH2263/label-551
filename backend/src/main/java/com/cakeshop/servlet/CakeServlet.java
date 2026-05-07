package com.cakeshop.servlet;

import com.cakeshop.dao.CakeDao;
import com.cakeshop.model.Cake;
import com.cakeshop.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 蛋糕商品Servlet（前台）
 */
@WebServlet("/api/cake/*")
public class CakeServlet extends BaseServlet {

    private CakeDao cakeDao = new CakeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
                // 获取蛋糕列表
                getList(request, response);
            } else if (pathInfo.equals("/hot")) {
                // 获取热门蛋糕
                getHotCakes(request, response);
            } else {
                // 获取蛋糕详情
                try {
                    int id = Integer.parseInt(pathInfo.substring(1));
                    getDetail(request, response, id);
                } catch (NumberFormatException e) {
                    error(response, 400, "无效的蛋糕ID");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }

    /**
     * 获取蛋糕列表（分页）
     */
    private void getList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer categoryId = getIntParam(request, "categoryId");
        String keyword = getParam(request, "keyword");
        int page = getIntParam(request, "page", 1);
        int pageSize = getIntParam(request, "pageSize", 12);

        // 限制pageSize
        if (pageSize > 50) pageSize = 50;
        if (page < 1) page = 1;

        List<Cake> cakes = cakeDao.findByPage(categoryId, keyword, page, pageSize);
        int total = cakeDao.count(categoryId, keyword);

        Map<String, Object> result = JsonUtil.createPageResult(cakes, page, pageSize, total);
        success(response, result);
    }

    /**
     * 获取热门蛋糕
     */
    private void getHotCakes(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        List<Cake> cakes = cakeDao.findHot(8);
        success(response, cakes);
    }

    /**
     * 获取蛋糕详情
     */
    private void getDetail(HttpServletRequest request, HttpServletResponse response, int id)
            throws IOException {
        Cake cake = cakeDao.findById(id);

        if (cake == null) {
            error(response, 404, "蛋糕不存在");
            return;
        }

        // 前台只显示上架商品
        if (!"on".equals(cake.getStatus())) {
            error(response, 404, "商品已下架");
            return;
        }

        success(response, cake);
    }
}
