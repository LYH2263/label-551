package com.cakeshop.servlet;

import com.cakeshop.dao.CakeDao;
import com.cakeshop.dao.CartDao;
import com.cakeshop.model.Cake;
import com.cakeshop.model.CartItem;
import com.cakeshop.model.User;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车Servlet
 */
@WebServlet("/api/cart/*")
public class CartServlet extends BaseServlet {

    private CartDao cartDao = new CartDao();
    private CakeDao cakeDao = new CakeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                error(response, 401, "请先登录");
                return;
            }

            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
                // 获取购物车列表
                List<CartItem> items = cartDao.findByUserId(user.getId());

                // 计算总价
                BigDecimal totalAmount = BigDecimal.ZERO;
                for (CartItem item : items) {
                    if ("on".equals(item.getCakeStatus()) && item.getCakeStock() > 0) {
                        totalAmount = totalAmount.add(item.getSubtotal());
                    }
                }

                Map<String, Object> result = new HashMap<>();
                result.put("items", items);
                result.put("totalAmount", totalAmount);
                result.put("totalCount", cartDao.countByUserId(user.getId()));

                success(response, result);
            } else if (pathInfo.equals("/count")) {
                // 获取购物车数量
                int count = cartDao.countByUserId(user.getId());
                Map<String, Object> result = new HashMap<>();
                result.put("count", count);
                success(response, result);
            } else {
                error(response, 404, "接口不存在");
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
            User user = getCurrentUser(request);
            if (user == null) {
                error(response, 401, "请先登录");
                return;
            }

            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.equals("/clear")) {
                // 清空购物车
                cartDao.clearByUserId(user.getId());
                success(response, "清空成功");
                return;
            }

            // 添加到购物车
            JsonObject json = readJsonBody(request);
            Integer cakeId = getJsonInt(json, "cakeId");
            Integer quantity = getJsonInt(json, "quantity");

            if (cakeId == null) {
                error(response, 400, "请选择商品");
                return;
            }
            if (quantity == null || quantity < 1) {
                quantity = 1;
            }

            // 检查商品是否存在
            Cake cake = cakeDao.findById(cakeId);
            if (cake == null) {
                error(response, 404, "商品不存在");
                return;
            }
            if (!"on".equals(cake.getStatus())) {
                error(response, 400, "商品已下架");
                return;
            }
            if (cake.getStock() < quantity) {
                error(response, 400, "库存不足");
                return;
            }

            // 检查购物车是否已有该商品
            CartItem existItem = cartDao.findByUserIdAndCakeId(user.getId(), cakeId);
            if (existItem != null) {
                // 更新数量
                int newQuantity = existItem.getQuantity() + quantity;
                if (newQuantity > cake.getStock()) {
                    error(response, 400, "超出库存限制");
                    return;
                }
                cartDao.updateQuantity(existItem.getId(), newQuantity);
            } else {
                // 新增
                CartItem item = new CartItem();
                item.setUserId(user.getId());
                item.setCakeId(cakeId);
                item.setQuantity(quantity);
                cartDao.insert(item);
            }

            success(response, "添加成功");
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
            User user = getCurrentUser(request);
            if (user == null) {
                error(response, 401, "请先登录");
                return;
            }

            if (pathInfo == null || pathInfo.equals("/")) {
                error(response, 400, "请指定购物车项ID");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                error(response, 400, "无效的购物车项ID");
                return;
            }

            CartItem item = cartDao.findById(id);
            if (item == null || !item.getUserId().equals(user.getId())) {
                error(response, 404, "购物车项不存在");
                return;
            }

            JsonObject json = readJsonBody(request);
            Integer quantity = getJsonInt(json, "quantity");

            if (quantity == null || quantity < 1) {
                error(response, 400, "数量必须大于0");
                return;
            }

            // 检查库存
            Cake cake = cakeDao.findById(item.getCakeId());
            if (cake == null || !"on".equals(cake.getStatus())) {
                error(response, 400, "商品已下架");
                return;
            }
            if (quantity > cake.getStock()) {
                error(response, 400, "超出库存限制");
                return;
            }

            if (cartDao.updateQuantity(id, quantity)) {
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
            User user = getCurrentUser(request);
            if (user == null) {
                error(response, 401, "请先登录");
                return;
            }

            if (pathInfo == null || pathInfo.equals("/")) {
                error(response, 400, "请指定购物车项ID");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                error(response, 400, "无效的购物车项ID");
                return;
            }

            CartItem item = cartDao.findById(id);
            if (item == null || !item.getUserId().equals(user.getId())) {
                error(response, 404, "购物车项不存在");
                return;
            }

            if (cartDao.delete(id)) {
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
