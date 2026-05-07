package com.cakeshop.servlet.admin;

import com.cakeshop.dao.OrderDao;
import com.cakeshop.model.Order;
import com.cakeshop.servlet.BaseServlet;
import com.cakeshop.util.JsonUtil;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 后台订单管理Servlet
 */
@WebServlet("/api/admin/order/*")
public class AdminOrderServlet extends BaseServlet {

    private OrderDao orderDao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
                // 获取订单列表
                String status = getParam(request, "status");
                String keyword = getParam(request, "keyword");
                String startDate = getParam(request, "startDate");
                String endDate = getParam(request, "endDate");
                int page = getIntParam(request, "page", 1);
                int pageSize = getIntParam(request, "pageSize", 10);

                List<Order> orders = orderDao.findAllByPage(status, keyword, startDate, endDate, page, pageSize);
                int total = orderDao.countAll(status, keyword, startDate, endDate);

                Map<String, Object> result = JsonUtil.createPageResult(orders, page, pageSize, total);
                success(response, result);
            } else {
                // 获取订单详情
                try {
                    int id = Integer.parseInt(pathInfo.substring(1));
                    Order order = orderDao.findById(id);
                    if (order == null) {
                        error(response, 404, "订单不存在");
                        return;
                    }
                    success(response, order);
                } catch (NumberFormatException e) {
                    error(response, 400, "无效的订单ID");
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
                error(response, 400, "请指定订单ID");
                return;
            }

            // 更新订单状态
            if (pathInfo.matches("/\\d+/status")) {
                int id = Integer.parseInt(pathInfo.split("/")[1]);
                Order order = orderDao.findById(id);
                if (order == null) {
                    error(response, 404, "订单不存在");
                    return;
                }

                JsonObject json = readJsonBody(request);
                String status = getJsonString(json, "status");

                // 验证状态值
                if (!"pending".equals(status) && !"paid".equals(status) &&
                    !"shipping".equals(status) && !"completed".equals(status) && !"cancelled".equals(status)) {
                    error(response, 400, "无效的订单状态");
                    return;
                }

                // 状态流转验证
                String currentStatus = order.getStatus();
                boolean validTransition = false;

                switch (currentStatus) {
                    case "pending":
                        validTransition = "paid".equals(status) || "cancelled".equals(status);
                        break;
                    case "paid":
                        validTransition = "shipping".equals(status) || "cancelled".equals(status);
                        break;
                    case "shipping":
                        validTransition = "completed".equals(status);
                        break;
                    default:
                        validTransition = false;
                }

                if (!validTransition) {
                    error(response, 400, "无效的状态变更");
                    return;
                }

                if (orderDao.updateStatus(id, status)) {
                    success(response, "更新成功");
                } else {
                    error(response, 500, "更新失败，请重试");
                }
                return;
            }

            error(response, 404, "接口不存在");
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "操作失败，请重试");
        }
    }
}
