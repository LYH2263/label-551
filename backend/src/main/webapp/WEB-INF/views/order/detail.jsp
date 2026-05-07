<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>订单详情 - 网上蛋糕城</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <style>
        .order-detail-card { background: white; border-radius: 16px; padding: 30px; margin-bottom: 20px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .order-detail-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 20px; border-bottom: 1px solid #eee; margin-bottom: 20px; }
        .order-status { font-size: 24px; font-weight: 700; }
        .status-pending { color: #f39c12; }
        .status-paid { color: #3498db; }
        .status-shipping { color: #9b59b6; }
        .status-completed { color: #27ae60; }
        .info-row { display: flex; margin-bottom: 15px; }
        .info-label { width: 100px; color: var(--text-light); }
        .info-value { flex: 1; font-weight: 500; }
        .order-items { margin-top: 20px; }
        .order-item { display: flex; gap: 20px; padding: 20px 0; border-bottom: 1px solid #f0f0f0; }
        .order-item:last-child { border-bottom: none; }
        .order-item img { width: 100px; height: 100px; border-radius: 12px; object-fit: cover; }
        .order-item-info { flex: 1; }
        .order-item-name { font-weight: 600; font-size: 18px; margin-bottom: 8px; }
        .order-item-price { color: var(--primary); font-weight: 700; font-size: 18px; }
        .order-summary { background: #f8f9fa; border-radius: 12px; padding: 20px; margin-top: 20px; }
        .summary-row { display: flex; justify-content: space-between; margin-bottom: 10px; }
        .summary-row.total { font-size: 20px; font-weight: 700; color: var(--primary); border-top: 1px solid #ddd; padding-top: 15px; margin-top: 15px; }
        .order-actions { display: flex; gap: 15px; justify-content: center; margin-top: 30px; }
        /* 模拟支付弹窗 */
        .payment-modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); z-index: 10000; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
        .payment-modal.active { display: flex; }
        .payment-content { background: white; border-radius: 24px; padding: 40px; width: 90%; max-width: 420px; text-align: center; animation: slideUp 0.3s ease; }
        @keyframes slideUp { from { transform: translateY(30px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
        .payment-icon { font-size: 64px; margin-bottom: 20px; }
        .payment-title { font-size: 24px; font-weight: 700; margin-bottom: 10px; }
        .payment-amount { font-size: 36px; font-weight: 700; color: var(--primary); margin: 20px 0; }
        .payment-methods { display: flex; gap: 15px; justify-content: center; margin: 30px 0; }
        .payment-method { padding: 15px 25px; border: 2px solid #eee; border-radius: 12px; cursor: pointer; transition: all 0.3s; display: flex; align-items: center; gap: 10px; font-weight: 600; }
        .payment-method:hover { border-color: var(--primary); background: #fff5f5; }
        .payment-method.selected { border-color: var(--primary); background: #fff5f5; }
        .payment-method img { height: 24px; }
        .payment-processing { display: none; flex-direction: column; align-items: center; gap: 20px; }
        .payment-processing.active { display: flex; }
        .payment-success { display: none; flex-direction: column; align-items: center; gap: 15px; }
        .payment-success.active { display: flex; }
        .payment-spinner { width: 50px; height: 50px; border: 4px solid #f0f0f0; border-top-color: var(--primary); border-radius: 50%; animation: spin 1s linear infinite; }
    </style>
</head>
<body>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>

    <div class="container">
        <div class="breadcrumb">
            <a href="${pageContext.request.contextPath}/">首页</a>
            <span>/</span>
            <a href="${pageContext.request.contextPath}/order/list">我的订单</a>
            <span>/</span>
            订单详情
        </div>

        <div id="orderDetail">
            <div class="card" style="text-align: center; padding: 60px;">
                <div class="loading"></div>
                <p style="margin-top: 20px; color: var(--text-light);">加载中...</p>
            </div>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/common/footer.jsp" %>

    <!-- 模拟支付弹窗 -->
    <div id="paymentModal" class="payment-modal">
        <div class="payment-content">
            <!-- 选择支付方式 -->
            <div id="paymentSelect">
                <div class="payment-icon">💳</div>
                <div class="payment-title">确认支付</div>
                <div class="payment-amount" id="paymentAmount">¥0.00</div>
                <div class="payment-methods">
                    <div class="payment-method selected" data-method="wechat">
                        <span style="font-size: 24px;">💚</span>
                        <span>微信支付</span>
                    </div>
                    <div class="payment-method" data-method="alipay">
                        <span style="font-size: 24px;">💙</span>
                        <span>支付宝</span>
                    </div>
                </div>
                <div style="display: flex; gap: 15px; justify-content: center;">
                    <button class="btn btn-secondary" onclick="closePaymentModal()">取消</button>
                    <button class="btn btn-primary" onclick="processPayment()">确认支付</button>
                </div>
            </div>
            <!-- 支付处理中 -->
            <div id="paymentProcessing" class="payment-processing">
                <div class="payment-spinner"></div>
                <div style="font-size: 18px; font-weight: 600;">支付处理中...</div>
                <div style="color: var(--text-light);">请稍候，正在模拟支付流程</div>
            </div>
            <!-- 支付成功 -->
            <div id="paymentSuccess" class="payment-success">
                <div style="font-size: 80px;">✅</div>
                <div style="font-size: 24px; font-weight: 700; color: #27ae60;">支付成功</div>
                <div style="color: var(--text-light);">订单已支付，商家将尽快发货</div>
                <button class="btn btn-primary" onclick="location.reload()" style="margin-top: 15px;">确定</button>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const orderId = '${orderId}';

        const statusText = {
            'pending': '待付款',
            'paid': '待发货',
            'shipping': '配送中',
            'completed': '已完成',
            'cancelled': '已取消'
        };

        async function loadOrderDetail() {
            try {
                const response = await apiGet(contextPath + '/api/order/' + orderId);
                const order = response.data;
                renderOrderDetail(order);
            } catch (error) {
                document.getElementById('orderDetail').innerHTML = '<div class="card" style="text-align:center;padding:60px;"><p>订单不存在或加载失败</p><a href="' + contextPath + '/order/list" class="btn btn-primary" style="margin-top:20px;">返回订单列表</a></div>';
            }
        }

        function renderOrderDetail(order) {
            let html = '<div class="order-detail-card">' +
                '<div class="order-detail-header">' +
                    '<div class="order-status status-' + order.status + '">' + statusText[order.status] + '</div>' +
                    '<div style="color:var(--text-light);">订单号：' + order.orderNo + '</div>' +
                '</div>' +
                '<div class="info-row"><div class="info-label">下单时间</div><div class="info-value">' + order.createdAt + '</div></div>' +
                '<div class="info-row"><div class="info-label">收货人</div><div class="info-value">' + order.receiverName + '</div></div>' +
                '<div class="info-row"><div class="info-label">联系电话</div><div class="info-value">' + order.receiverPhone + '</div></div>' +
                '<div class="info-row"><div class="info-label">收货地址</div><div class="info-value">' + order.receiverAddress + '</div></div>' +
                (order.remark ? '<div class="info-row"><div class="info-label">备注</div><div class="info-value">' + order.remark + '</div></div>' : '') +
            '</div>';

            html += '<div class="order-detail-card"><h3 style="margin-bottom:20px;">商品信息</h3><div class="order-items">';
            let totalQuantity = 0;
            (order.items || []).forEach(item => {
                totalQuantity += item.quantity;
                const itemImage = item.cakeImage || item.image || '';
                html += '<div class="order-item">' +
                    '<img src="' + resolveCakeImageUrl(itemImage, contextPath) + '" onerror="this.src=\'' + contextPath + '/images/no-image.svg\'">' +
                    '<div class="order-item-info"><div class="order-item-name">' + item.cakeName + '</div><div style="color:var(--text-light);">单价：¥' + item.price + ' × ' + item.quantity + '</div></div>' +
                    '<div class="order-item-price">¥' + item.subtotal + '</div>' +
                '</div>';
            });
            html += '</div>';

            html += '<div class="order-summary">' +
                '<div class="summary-row"><span>商品数量</span><span>' + totalQuantity + ' 件</span></div>' +
                '<div class="summary-row total"><span>订单总额</span><span>¥' + order.totalAmount + '</span></div>' +
            '</div></div>';

            // 操作按钮
            html += '<div class="order-actions">' +
                '<a href="' + contextPath + '/order/list" class="btn btn-secondary">返回订单列表</a>';

            // 待付款订单显示支付和取消按钮
            if (order.status === 'pending') {
                html += '<button class="btn btn-danger" onclick="cancelOrder(' + order.id + ')">取消订单</button>';
                html += '<button class="btn btn-primary" onclick="openPaymentModal(' + order.totalAmount + ')">立即支付</button>';
            }

            html += '</div>';

            document.getElementById('orderDetail').innerHTML = html;
        }

        // 当前订单ID
        let currentOrderId = orderId;

        // 打开支付弹窗
        function openPaymentModal(amount) {
            document.getElementById('paymentAmount').textContent = '¥' + parseFloat(amount).toFixed(2);
            document.getElementById('paymentSelect').style.display = 'block';
            document.getElementById('paymentProcessing').classList.remove('active');
            document.getElementById('paymentSuccess').classList.remove('active');
            document.getElementById('paymentModal').classList.add('active');
        }

        // 关闭支付弹窗
        function closePaymentModal() {
            document.getElementById('paymentModal').classList.remove('active');
        }

        // 选择支付方式
        document.querySelectorAll('.payment-method').forEach(method => {
            method.addEventListener('click', function() {
                document.querySelectorAll('.payment-method').forEach(m => m.classList.remove('selected'));
                this.classList.add('selected');
            });
        });

        // 处理支付
        async function processPayment() {
            // 隐藏选择界面，显示处理中
            document.getElementById('paymentSelect').style.display = 'none';
            document.getElementById('paymentProcessing').classList.add('active');

            // 模拟支付延迟
            await new Promise(resolve => setTimeout(resolve, 2000));

            try {
                await apiPut(contextPath + '/api/order/' + currentOrderId + '/pay', {});

                // 显示成功界面
                document.getElementById('paymentProcessing').classList.remove('active');
                document.getElementById('paymentSuccess').classList.add('active');
            } catch (error) {
                closePaymentModal();
                showToast('支付失败，请重试', 'error');
            }
        }

        // 取消订单
        async function cancelOrder(id) {
            const confirmed = await showConfirm({
                title: '取消订单',
                message: '确定要取消这个订单吗？取消后无法恢复。',
                type: 'warning',
                confirmText: '确认取消',
                confirmClass: 'btn-danger'
            });
            if (!confirmed) return;

            try {
                await apiPut(contextPath + '/api/order/' + id + '/cancel', {});
                showToast('订单已取消', 'success');
                setTimeout(() => location.reload(), 1000);
            } catch (error) {
                console.error('取消失败:', error);
            }
        }

        loadOrderDetail();
    </script>
</body>
</html>
