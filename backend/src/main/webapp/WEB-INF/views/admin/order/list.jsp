<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>订单管理 - 网上蛋糕城</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <style>
        .admin-layout { display: flex; min-height: 100vh; }
        .admin-sidebar { width: 250px; background: linear-gradient(180deg, #2c3e50, #1a252f); padding: 20px 0; position: fixed; height: 100vh; }
        .admin-logo { color: white; font-size: 24px; font-weight: 700; padding: 20px 30px; border-bottom: 1px solid rgba(255,255,255,0.1); margin-bottom: 20px; }
        .admin-menu { list-style: none; }
        .admin-menu a { display: flex; align-items: center; gap: 12px; padding: 15px 30px; color: rgba(255,255,255,0.7); text-decoration: none; transition: all 0.3s; }
        .admin-menu a:hover { background: rgba(255,255,255,0.1); color: white; }
        .admin-menu a.active { background: var(--primary); color: white; }
        .admin-content { flex: 1; margin-left: 250px; padding: 30px; background: #f5f6fa; }
        .admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
        .admin-title { font-size: 28px; font-weight: 700; }
        .filter-bar { display: flex; gap: 15px; margin-bottom: 20px; }
        .filter-bar select, .filter-bar input { padding: 10px 15px; border: 1px solid #ddd; border-radius: 8px; }
        .data-table { background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .action-btns { display: flex; gap: 8px; }
        .action-btns button { padding: 6px 12px; font-size: 12px; }
        .modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center; }
        .modal.active { display: flex; }
        .modal-content { background: white; border-radius: 20px; padding: 40px; width: 90%; max-width: 700px; max-height: 90vh; overflow-y: auto; }
        .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
        .modal-close { background: none; border: none; font-size: 24px; cursor: pointer; }
        .order-item { display: flex; gap: 15px; padding: 15px 0; border-bottom: 1px solid #eee; }
        .order-item img { width: 60px; height: 60px; border-radius: 8px; object-fit: cover; }
    </style>
</head>
<body>
    <div class="admin-layout">
        <aside class="admin-sidebar">
            <div class="admin-logo">🎂 蛋糕城后台</div>
            <ul class="admin-menu">
                <li><a href="${pageContext.request.contextPath}/admin">📊 控制台</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/cake/list">🍰 蛋糕管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/category/list">📁 分类管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/order/list" class="active">📦 订单管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/user/list">👥 用户管理</a></li>
                <li><a href="${pageContext.request.contextPath}/">🏠 返回前台</a></li>
            </ul>
        </aside>

        <main class="admin-content">
            <div class="admin-header">
                <h1 class="admin-title">订单管理</h1>
            </div>

            <div class="filter-bar">
                <select id="filterStatus" onchange="loadOrders()">
                    <option value="">全部状态</option>
                    <option value="pending">待付款</option>
                    <option value="paid">待发货</option>
                    <option value="shipping">配送中</option>
                    <option value="completed">已完成</option>
                    <option value="cancelled">已取消</option>
                </select>
                <input type="date" id="filterStartDate" onchange="loadOrders()" title="开始日期">
                <input type="date" id="filterEndDate" onchange="loadOrders()" title="结束日期">
                <input type="text" id="filterKeyword" placeholder="搜索订单号/用户名" onkeyup="loadOrders()">
            </div>

            <div class="data-table">
                <table>
                    <thead><tr><th>订单号</th><th>用户</th><th>金额</th><th>状态</th><th>时间</th><th>操作</th></tr></thead>
                    <tbody id="orderList"><tr><td colspan="6" style="text-align:center;padding:30px;">加载中...</td></tr></tbody>
                </table>
            </div>
        </main>
    </div>

    <div id="orderModal" class="modal">
        <div class="modal-content">
            <div class="modal-header"><h3>订单详情</h3><button class="modal-close" onclick="closeModal()">&times;</button></div>
            <div id="orderDetail"></div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const statusText = {'pending':'待付款','paid':'待发货','shipping':'配送中','completed':'已完成','cancelled':'已取消'};

        async function loadOrders() {
            try {
                const params = {};
                const status = document.getElementById('filterStatus').value;
                const keyword = document.getElementById('filterKeyword').value;
                const startDate = document.getElementById('filterStartDate').value;
                const endDate = document.getElementById('filterEndDate').value;
                if (status) params.status = status;
                if (keyword) params.keyword = keyword;
                if (startDate) params.startDate = startDate;
                if (endDate) params.endDate = endDate;

                const response = await apiGet(contextPath + '/api/admin/order', params);
                // API返回分页格式: { data: { list: [...], total, page, pageSize } }
                const result = response.data || {};
                const orders = result.list || [];
                renderOrders(orders);
            } catch (error) {
                document.getElementById('orderList').innerHTML = '<tr><td colspan="6" style="text-align:center;">加载失败</td></tr>';
            }
        }

        function renderOrders(orders) {
            if (orders.length === 0) {
                document.getElementById('orderList').innerHTML = '<tr><td colspan="6" style="text-align:center;padding:30px;">暂无数据</td></tr>';
                return;
            }
            let html = '';
            orders.forEach(order => {
                html += '<tr>' +
                    '<td>' + order.orderNo + '</td>' +
                    '<td>' + order.username + '</td>' +
                    '<td>¥' + order.totalAmount + '</td>' +
                    '<td><span class="badge badge-' + getStatusClass(order.status) + '">' + statusText[order.status] + '</span></td>' +
                    '<td>' + order.createdAt + '</td>' +
                    '<td class="action-btns">' +
                        '<button class="btn btn-secondary" onclick="viewOrder(' + order.id + ')">详情</button>' +
                        (order.status === 'paid' ? '<button class="btn btn-primary" onclick="shipOrder(' + order.id + ')">发货</button>' : '') +
                        (order.status === 'shipping' ? '<button class="btn btn-success" onclick="completeOrder(' + order.id + ')">完成</button>' : '') +
                        (order.status === 'pending' ? '<button class="btn btn-danger" onclick="cancelOrder(' + order.id + ')">取消</button>' : '') +
                    '</td>' +
                '</tr>';
            });
            document.getElementById('orderList').innerHTML = html;
        }

        function getStatusClass(status) {
            const map = {'pending':'warning','paid':'info','shipping':'primary','completed':'success','cancelled':'secondary'};
            return map[status] || 'secondary';
        }

        async function viewOrder(id) {
            try {
                const response = await apiGet(contextPath + '/api/admin/order/' + id);
                const order = response.data;
                let html = '<div style="margin-bottom:20px;">' +
                    '<p><strong>订单号：</strong>' + order.orderNo + '</p>' +
                    '<p><strong>状态：</strong>' + statusText[order.status] + '</p>' +
                    '<p><strong>收货人：</strong>' + order.receiverName + ' ' + order.receiverPhone + '</p>' +
                    '<p><strong>地址：</strong>' + order.receiverAddress + '</p>' +
                    '<p><strong>备注：</strong>' + (order.remark || '无') + '</p>' +
                '</div><h4>商品清单</h4>';

                (order.items || []).forEach(item => {
                    const itemImage = item.cakeImage || item.image || '';
                    html += '<div class="order-item">' +
                        '<img src="' + resolveCakeImageUrl(itemImage, contextPath) + '" onerror="this.src=\'' + contextPath + '/images/no-image.svg\'">' +
                        '<div style="flex:1;"><div style="font-weight:600;">' + item.cakeName + '</div><div style="color:#888;">¥' + item.price + ' × ' + item.quantity + '</div></div>' +
                        '<div style="font-weight:600;color:var(--primary);">¥' + item.subtotal + '</div>' +
                    '</div>';
                });

                html += '<div style="text-align:right;margin-top:20px;font-size:20px;font-weight:700;color:var(--primary);">总计：¥' + order.totalAmount + '</div>';
                document.getElementById('orderDetail').innerHTML = html;
                document.getElementById('orderModal').classList.add('active');
            } catch (error) { console.error('获取订单失败:', error); }
        }

        function closeModal() { document.getElementById('orderModal').classList.remove('active'); }

        async function completeOrder(id) {
            const confirmed = await showConfirm({
                title: '确认操作',
                message: '确定将此订单标记为已完成？',
                type: 'success',
                confirmText: '确定',
                confirmClass: 'btn-success'
            });
            if (!confirmed) return;
            try {
                await apiPut(contextPath + '/api/admin/order/' + id + '/status', { status: 'completed' });
                showToast('操作成功', 'success');
                loadOrders();
            } catch (error) { console.error('操作失败:', error); }
        }

        async function shipOrder(id) {
            const confirmed = await showConfirm({
                title: '确认发货',
                message: '确定将此订单标记为配送中？',
                type: 'info',
                confirmText: '确认发货',
                confirmClass: 'btn-primary'
            });
            if (!confirmed) return;
            try {
                await apiPut(contextPath + '/api/admin/order/' + id + '/status', { status: 'shipping' });
                showToast('发货成功', 'success');
                loadOrders();
            } catch (error) { console.error('操作失败:', error); }
        }

        async function cancelOrder(id) {
            const confirmed = await showConfirm({
                title: '取消订单',
                message: '确定要取消此订单吗？',
                type: 'danger',
                confirmText: '确认取消',
                confirmClass: 'btn-danger'
            });
            if (!confirmed) return;
            try {
                await apiPut(contextPath + '/api/admin/order/' + id + '/status', { status: 'cancelled' });
                showToast('订单已取消', 'success');
                loadOrders();
            } catch (error) { console.error('操作失败:', error); }
        }

        loadOrders();
    </script>
</body>
</html>
