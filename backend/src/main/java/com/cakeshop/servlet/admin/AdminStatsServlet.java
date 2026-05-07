package com.cakeshop.servlet.admin;

import com.cakeshop.dao.CakeDao;
import com.cakeshop.dao.OrderDao;
import com.cakeshop.dao.UserDao;
import com.cakeshop.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台统计Servlet
 */
@WebServlet("/api/admin/stats")
public class AdminStatsServlet extends BaseServlet {

    private UserDao userDao = new UserDao();
    private CakeDao cakeDao = new CakeDao();
    private OrderDao orderDao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 用户数量
            stats.put("userCount", userDao.count());

            // 商品数量
            stats.put("cakeCount", cakeDao.countAll(null, null, null));

            // 订单数量
            stats.put("orderCount", orderDao.countAll(null, null, null, null));

            // 销售总额 (已完成订单)
            BigDecimal revenue = orderDao.getTotalRevenue();
            stats.put("revenue", revenue != null ? revenue : BigDecimal.ZERO);

            success(response, stats);
        } catch (Exception e) {
            e.printStackTrace();
            error(response, 500, "获取统计数据失败");
        }
    }
}
