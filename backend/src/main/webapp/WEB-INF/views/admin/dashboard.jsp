<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>后台管理 - 网上蛋糕城</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <style>
        .admin-layout { display: flex; min-height: 100vh; }
        .admin-sidebar { width: 250px; background: linear-gradient(180deg, #2c3e50, #1a252f); padding: 20px 0; position: fixed; height: 100vh; overflow-y: auto; }
        .admin-logo { color: white; font-size: 24px; font-weight: 700; padding: 20px 30px; border-bottom: 1px solid rgba(255,255,255,0.1); margin-bottom: 20px; }
        .admin-menu { list-style: none; }
        .admin-menu a { display: flex; align-items: center; gap: 12px; padding: 15px 30px; color: rgba(255,255,255,0.7); text-decoration: none; transition: all 0.3s; }
        .admin-menu a:hover { background: rgba(255,255,255,0.1); color: white; }
        .admin-menu a.active { background: var(--primary); color: white; }
        .admin-menu-icon { font-size: 18px; }
        .admin-content { flex: 1; margin-left: 250px; padding: 30px; background: #f5f6fa; min-height: 100vh; }
        .admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
        .admin-title { font-size: 28px; font-weight: 700; }
        .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 30px; }
        .stat-card { background: white; border-radius: 16px; padding: 25px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .stat-icon { width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; margin-bottom: 15px; }
        .stat-icon.users { background: rgba(52, 152, 219, 0.1); color: #3498db; }
        .stat-icon.cakes { background: rgba(255, 107, 107, 0.1); color: var(--primary); }
        .stat-icon.orders { background: rgba(46, 204, 113, 0.1); color: #2ecc71; }
        .stat-icon.revenue { background: rgba(155, 89, 182, 0.1); color: #9b59b6; }
        .stat-value { font-size: 32px; font-weight: 700; margin-bottom: 5px; }
        .stat-label { color: var(--text-light); font-size: 14px; }
        .recent-section { background: white; border-radius: 16px; padding: 25px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        .section-title { font-size: 18px; font-weight: 600; }
    </style>
</head>
<body>
    <div class="admin-layout">
        <aside class="admin-sidebar">
            <div class="admin-logo">🎂 蛋糕城后台</div>
            <ul class="admin-menu">
                <li><a href="${pageContext.request.contextPath}/admin" class="active"><span class="admin-menu-icon">📊</span> 控制台</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/cake/list"><span class="admin-menu-icon">🍰</span> 蛋糕管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/category/list"><span class="admin-menu-icon">📁</span> 分类管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/order/list"><span class="admin-menu-icon">📦</span> 订单管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/user/list"><span class="admin-menu-icon">👥</span> 用户管理</a></li>
                <li><a href="${pageContext.request.contextPath}/"><span class="admin-menu-icon">🏠</span> 返回前台</a></li>
            </ul>
        </aside>

        <main class="admin-content">
            <div class="admin-header">
                <h1 class="admin-title">控制台</h1>
                <div>欢迎，${sessionScope.user.username}</div>
            </div>

            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-icon users">👥</div>
                    <div class="stat-value" id="userCount">-</div>
                    <div class="stat-label">注册用户</div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon cakes">🍰</div>
                    <div class="stat-value" id="cakeCount">-</div>
                    <div class="stat-label">商品数量</div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon orders">📦</div>
                    <div class="stat-value" id="orderCount">-</div>
                    <div class="stat-label">订单总数</div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon revenue">💰</div>
                    <div class="stat-value" id="revenue">-</div>
                    <div class="stat-label">销售总额</div>
                </div>
            </div>

            <div class="recent-section">
                <div class="section-header">
                    <h3 class="section-title">最近订单</h3>
                    <a href="${pageContext.request.contextPath}/admin/order/list" class="btn btn-secondary" style="padding: 8px 16px; font-size: 13px;">查看全部</a>
                </div>
                <table>
                    <thead>
                        <tr><th>订单号</th><th>用户</th><th>金额</th><th>状态</th><th>时间</th></tr>
                    </thead>
                    <tbody id="recentOrders">
                        <tr><td colspan="5" style="text-align:center;padding:30px;">加载中...</td></tr>
                    </tbody>
                </table>
            </div>
        </main>
    </div>

    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const statusText = {'pending':'待付款','paid':'待发货','shipping':'配送中','completed':'已完成','cancelled':'已取消'};

        async function loadStats() {
            try {
                const response = await apiGet(contextPath + '/api/admin/stats');
                const stats = response.data;
                document.getElementById('userCount').textContent = stats.userCount || 0;
                document.getElementById('cakeCount').textContent = stats.cakeCount || 0;
                document.getElementById('orderCount').textContent = stats.orderCount || 0;
                document.getElementById('revenue').textContent = '¥' + (stats.revenue || 0).toFixed(0);
            } catch (error) {
                console.error('加载统计失败:', error);
            }
        }

        async function loadRecentOrders() {
            try {
                const response = await apiGet(contextPath + '/api/admin/order', { page: 1, pageSize: 5 });
                // API返回分页格式: { data: { list: [...], total } }
                const result = response.data || {};
                const orders = result.list || [];
                if (orders.length === 0) {
                    document.getElementById('recentOrders').innerHTML = '<tr><td colspan="5" style="text-align:center;padding:30px;">暂无订单</td></tr>';
                    return;
                }
                let html = '';
                orders.forEach(order => {
                    html += '<tr><td>' + order.orderNo + '</td><td>' + (order.username || '-') + '</td><td>¥' + order.totalAmount + '</td><td><span class="badge badge-' + getStatusClass(order.status) + '">' + statusText[order.status] + '</span></td><td>' + order.createdAt + '</td></tr>';
                });
                document.getElementById('recentOrders').innerHTML = html;
            } catch (error) {
                document.getElementById('recentOrders').innerHTML = '<tr><td colspan="5" style="text-align:center;">加载失败</td></tr>';
            }
        }

        function getStatusClass(status) {
            const map = {'pending':'warning','paid':'info','shipping':'primary','completed':'success','cancelled':'secondary'};
            return map[status] || 'secondary';
        }

        loadStats();
        loadRecentOrders();
    </script>
</body>
</html>
