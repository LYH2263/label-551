package com.cakeshop.servlet;

import com.cakeshop.dao.*;
import com.cakeshop.model.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单Servlet（用户端）
 */
@WebServlet("/api/order/*")
public class OrderServlet extends BaseServlet {

    private OrderDao orderDao = new OrderDao();
    private CartDao cartDao = new CartDao();
    private CakeDao cakeDao = new CakeDao();
    private AddressDao addressDao = new AddressDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            User user = getCurrentUser(request);
            if (user == null) {
                error(response, 401, "请先登录");
                return;
            }

            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
                // 获取订单列表
                String status = getParam(request, "status");
                List<Order> orders = orderDao.findByUserId(user.getId(), status);
                success(response, orders);
            } else {
                // 获取订单详情
                try {
                    int id = Integer.parseInt(pathInfo.substring(1));
                    Order order = orderDao.findById(id);
                    if (order == null || !order.getUserId().equals(user.getId())) {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                error(response, 401, "请先登录");
                return;
            }

            JsonObject json = readJsonBody(request);
            Integer addressId = getJsonInt(json, "addressId");
            String remark = getJsonString(json, "remark");

            // 获取收货地址
            Address address = null;
            if (addressId != null) {
                address = addressDao.findById(addressId);
                if (address == null || !address.getUserId().equals(user.getId())) {
                    error(response, 400, "收货地址不存在");
                    return;
                }
            } else {
                // 使用默认地址
                address = addressDao.findDefaultByUserId(user.getId());
                if (address == null) {
                    // 尝试获取第一个地址
                    List<Address> addresses = addressDao.findByUserId(user.getId());
                    if (!addresses.isEmpty()) {
                        address = addresses.get(0);
                    }
                }
            }

            if (address == null) {
                error(response, 400, "请先添加收货地址");
                return;
            }

            // 获取购物车商品
            List<CartItem> cartItems = cartDao.findByUserId(user.getId());
            if (cartItems.isEmpty()) {
                error(response, 400, "购物车为空");
                return;
            }

            // 验证商品并计算总价
            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (CartItem cartItem : cartItems) {
                Cake cake = cakeDao.findById(cartItem.getCakeId());
                if (cake == null) {
                    error(response, 400, "商品 [" + cartItem.getCakeName() + "] 已不存在");
                    return;
                }
                if (!"on".equals(cake.getStatus())) {
                    error(response, 400, "商品 [" + cake.getName() + "] 已下架");
                    return;
                }
                if (cake.getStock() < cartItem.getQuantity()) {
                    error(response, 400, "商品 [" + cake.getName() + "] 库存不足");
                    return;
                }

                // 创建订单项
                OrderItem orderItem = new OrderItem(
                    cake.getId(),
                    cake.getName(),
                    cake.getImage(),
                    cake.getPrice(),
                    cartItem.getQuantity()
                );
                orderItems.add(orderItem);
                totalAmount = totalAmount.add(orderItem.getSubtotal());
            }

            // 创建订单
            Order order = new Order();
            order.setOrderNo(OrderDao.generateOrderNo());
            order.setUserId(user.getId());
            order.setAddressId(address.getId());
            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getPhone());
            order.setReceiverAddress(address.getFullAddress());
            order.setTotalAmount(totalAmount);
            order.setStatus("pending");
            order.setRemark(remark);

            int orderId = orderDao.createOrder(order, orderItems);
            if (orderId > 0) {
                // 扣减库存
                for (CartItem cartItem : cartItems) {
                    cakeDao.decreaseStock(cartItem.getCakeId(), cartItem.getQuantity());
                }

                // 清空购物车
                cartDao.clearByUserId(user.getId());

                order.setId(orderId);
                success(response, "下单成功", order);
            } else {
                error(response, 500, "下单失败，请重试");
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
            User user = getCurrentUser(request);
            if (user == null) {
                error(response, 401, "请先登录");
                return;
            }

            // 取消订单
            if (pathInfo != null && pathInfo.matches("/\\d+/cancel")) {
                int id = Integer.parseInt(pathInfo.split("/")[1]);
                Order order = orderDao.findById(id);
                if (order == null || !order.getUserId().equals(user.getId())) {
                    error(response, 404, "订单不存在");
                    return;
                }
                if (!"pending".equals(order.getStatus())) {
                    error(response, 400, "只能取消待付款订单");
                    return;
                }

                if (orderDao.updateStatus(id, "cancelled")) {
                    success(response, "取消成功");
                } else {
                    error(response, 500, "取消失败，请重试");
                }
                return;
            }

            // 模拟支付
            if (pathInfo != null && pathInfo.matches("/\\d+/pay")) {
                int id = Integer.parseInt(pathInfo.split("/")[1]);
                Order order = orderDao.findById(id);
                if (order == null || !order.getUserId().equals(user.getId())) {
                    error(response, 404, "订单不存在");
                    return;
                }
                if (!"pending".equals(order.getStatus())) {
                    error(response, 400, "该订单不是待付款状态");
                    return;
                }

                // 模拟支付成功，更新状态为已支付（待发货）
                if (orderDao.updateStatus(id, "paid")) {
                    success(response, "支付成功");
                } else {
                    error(response, 500, "支付失败，请重试");
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
