<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>我的订单 - 网上蛋糕城</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <style>
        .main-content { flex: 1; min-width: 0; }
        .order-page-head {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 20px;
            margin-bottom: 20px;
            padding: 24px 26px;
            border-radius: 18px;
            border: 1px solid rgba(212, 93, 121, 0.12);
            background:
                radial-gradient(circle at 100% 0%, rgba(212, 93, 121, 0.12), rgba(212, 93, 121, 0) 45%),
                linear-gradient(135deg, #fff7f9, #fffefb);
        }
        .order-page-head h2 {
            margin: 0 0 6px;
            font-size: 24px;
            font-weight: 700;
            color: var(--text-main);
        }
        .order-page-head p {
            margin: 0;
            color: var(--text-light);
            font-size: 14px;
        }
        .order-tabs {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-bottom: 28px;
            background: #fff;
            padding: 10px;
            border-radius: 14px;
            border: 1px solid var(--border);
            box-shadow: var(--shadow-sm);
        }
        .order-tab {
            padding: 10px 18px;
            border-radius: 999px;
            cursor: pointer;
            font-weight: 600;
            transition: var(--transition);
            border: 1px solid transparent;
            background: transparent;
            color: var(--text-light);
        }
        .order-tab:hover {
            color: var(--primary);
            border-color: rgba(212, 93, 121, 0.25);
            background: rgba(212, 93, 121, 0.05);
        }
        .order-tab.active {
            background: #fff;
            color: var(--primary);
            border-color: rgba(212, 93, 121, 0.35);
            box-shadow: var(--shadow-sm);
        }
        .order-card { background: white; border-radius: 16px; margin-bottom: 20px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .order-header { background: #f8f9fa; padding: 15px 24px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; gap: 12px; flex-wrap: wrap; }
        .order-no { font-weight: 600; color: var(--text-main); }
        .order-time { color: var(--text-light); font-size: 14px; }
        .order-body { padding: 24px; }
        .order-item { display: flex; gap: 20px; padding: 15px 0; border-bottom: 1px solid #f0f0f0; }
        .order-item:last-child { border-bottom: none; }
        .order-item img { width: 80px; height: 80px; border-radius: 12px; object-fit: cover; }
        .order-item-info { flex: 1; }
        .order-item-name { font-weight: 600; margin-bottom: 5px; }
        .order-item-spec { color: var(--text-light); font-size: 14px; }
        .order-item-price { text-align: right; }
        .order-item-unit { color: var(--text-light); font-size: 14px; }
        .order-item-subtotal { color: var(--primary); font-weight: 600; font-size: 18px; }
        .order-footer { padding: 20px 24px; border-top: 1px solid #f0f0f0; display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
        .order-total { font-size: 14px; color: var(--text-light); }
        .order-total span { font-size: 24px; color: var(--primary); font-weight: 700; }
        .order-actions { display: flex; gap: 10px; }
        .status-pending { color: #f39c12; }
        .status-paid { color: #3498db; }
        .status-shipping { color: #9b59b6; }
        .status-completed { color: #27ae60; }
        .status-cancelled { color: #95a5a6; }
        .empty-orders { text-align: center; padding: 80px 20px; }
        .empty-orders-icon { font-size: 64px; margin-bottom: 20px; }
        @media (max-width: 768px) {
            .order-page-head {
                padding: 18px;
                flex-direction: column;
                align-items: flex-start;
            }
            .order-page-head .btn {
                width: 100%;
            }
            .order-item {
                gap: 12px;
            }
            .order-item img {
                width: 64px;
                height: 64px;
            }
        }
    </style>
</head>
<body>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>

    <div class="container">
        <h1 class="page-title">我的订单</h1>

        <div class="content-wrapper">
            <div class="sidebar">
                <ul class="sidebar-menu">
                    <li><a href="${pageContext.request.contextPath}/user/profile">个人信息</a></li>
                    <li><a href="${pageContext.request.contextPath}/user/address">收货地址</a></li>
                    <li><a href="${pageContext.request.contextPath}/order/list" class="active">我的订单</a></li>
                </ul>
            </div>

            <div class="main-content">
                <div class="order-page-head">
                    <div>
                        <h2>订单管理中心</h2>
                        <p>按状态快速筛选，查看配送进度与历史明细。</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/cake/list" class="btn btn-secondary">继续选购</a>
                </div>
                <div class="order-tabs">
                    <button class="order-tab active" data-status="">全部</button>
                    <button class="order-tab" data-status="pending">待付款</button>
                    <button class="order-tab" data-status="paid">待发货</button>
                    <button class="order-tab" data-status="shipping">配送中</button>
                    <button class="order-tab" data-status="completed">已完成</button>
                </div>

                <div id="orderList">
                    <div class="card" style="text-align: center; padding: 60px;">
                        <div class="loading"></div>
                        <p style="margin-top: 20px; color: var(--text-light);">加载中...</p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/common/footer.jsp" %>

    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        let currentStatus = '';

        const statusText = {
            'pending': '待付款',
            'paid': '待发货',
            'shipping': '配送中',
            'completed': '已完成',
            'cancelled': '已取消'
        };

        // 切换标签
        document.querySelectorAll('.order-tab').forEach(tab => {
            tab.addEventListener('click', function() {
                document.querySelectorAll('.order-tab').forEach(t => t.classList.remove('active'));
                this.classList.add('active');
                currentStatus = this.dataset.status;
                loadOrders();
            });
        });

        async function loadOrders() {
            try {
                const params = currentStatus ? { status: currentStatus } : {};
                const response = await apiGet(contextPath + '/api/order/list', params);
                const orders = response.data || [];
                renderOrders(orders);
            } catch (error) {
                document.getElementById('orderList').innerHTML = '<div class="empty-orders"><div class="empty-orders-icon">😢</div><p>加载失败，请刷新重试</p></div>';
            }
        }

        function renderOrders(orders) {
            if (orders.length === 0) {
                document.getElementById('orderList').innerHTML = '<div class="empty-orders"><div class="empty-orders-icon">📦</div><p style="color:var(--text-light);">暂无订单</p><a href="' + contextPath + '/cake/list" class="btn btn-primary" style="margin-top:20px;">去选购</a></div>';
                return;
            }

            let html = '';
            orders.forEach(order => {
                html += '<div class="order-card">' +
                    '<div class="order-header">' +
                        '<div><span class="order-no">订单号：' + order.orderNo + '</span></div>' +
                        '<div><span class="order-time">' + order.createdAt + '</span> <span class="badge badge-' + getStatusClass(order.status) + '" style="margin-left:10px;">' + statusText[order.status] + '</span></div>' +
                    '</div>' +
                    '<div class="order-body">';

                (order.items || []).forEach(item => {
                    const itemImage = item.cakeImage || item.image || '';
                    html += '<div class="order-item">' +
                        '<img src="' + resolveCakeImageUrl(itemImage, contextPath) + '" onerror="this.src=\'' + contextPath + '/images/no-image.svg\'">' +
                        '<div class="order-item-info"><div class="order-item-name">' + item.cakeName + '</div><div class="order-item-spec">x' + item.quantity + '</div></div>' +
                        '<div class="order-item-price"><div class="order-item-unit">¥' + item.price + '</div><div class="order-item-subtotal">¥' + item.subtotal + '</div></div>' +
                    '</div>';
                });

                html += '</div>' +
                    '<div class="order-footer">' +
                        '<div class="order-total">共 ' + (order.items ? order.items.length : 0) + ' 件商品，合计：<span>¥' + order.totalAmount + '</span></div>' +
                        '<div class="order-actions">' +
                            '<a href="' + contextPath + '/order/' + order.id + '" class="btn btn-secondary">查看详情</a>' +
                        '</div>' +
                    '</div>' +
                '</div>';
            });
            document.getElementById('orderList').innerHTML = html;
        }

        function getStatusClass(status) {
            const map = { 'pending': 'warning', 'paid': 'info', 'shipping': 'primary', 'completed': 'success', 'cancelled': 'secondary' };
            return map[status] || 'secondary';
        }

        loadOrders();
    </script>
</body>
</html>
