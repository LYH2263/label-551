<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>确认订单 - 网上蛋糕城</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <style>
        .checkout-section { background: white; border-radius: 16px; padding: 30px; margin-bottom: 20px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .checkout-section h2 { font-size: 20px; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #eee; }
        .address-list { display: flex; flex-direction: column; gap: 15px; }
        .address-option { display: flex; align-items: flex-start; gap: 15px; padding: 20px; border: 2px solid #eee; border-radius: 12px; cursor: pointer; transition: all 0.3s; }
        .address-option:hover { border-color: var(--primary-light); }
        .address-option.selected { border-color: var(--primary); background: #fff5f5; }
        .address-option input { margin-top: 5px; }
        .address-info { flex: 1; }
        .address-name { font-weight: 600; font-size: 16px; }
        .address-phone { color: var(--text-light); margin-left: 15px; }
        .address-detail { color: var(--text-light); margin-top: 8px; }
        .cart-item { display: flex; gap: 20px; padding: 20px 0; border-bottom: 1px solid #f0f0f0; }
        .cart-item:last-child { border-bottom: none; }
        .cart-item img { width: 80px; height: 80px; border-radius: 12px; object-fit: cover; }
        .cart-item-info { flex: 1; }
        .cart-item-name { font-weight: 600; margin-bottom: 5px; }
        .cart-item-price { color: var(--primary); font-weight: 600; }
        .order-summary { background: #f8f9fa; border-radius: 12px; padding: 24px; }
        .summary-row { display: flex; justify-content: space-between; margin-bottom: 12px; color: var(--text-light); }
        .summary-row.total { font-size: 22px; font-weight: 700; color: var(--primary); border-top: 1px solid #ddd; padding-top: 15px; margin-top: 15px; }
        .checkout-actions { text-align: center; margin-top: 30px; }
        .checkout-actions .btn { padding: 15px 60px; font-size: 18px; }
        .remark-input { width: 100%; padding: 15px; border: 1px solid #eee; border-radius: 12px; resize: none; }
        .no-address { text-align: center; padding: 40px; color: var(--text-light); }
    </style>
</head>
<body>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>

    <div class="container">
        <h1 class="page-title">确认订单</h1>

        <div class="checkout-section">
            <h2>📍 收货地址</h2>
            <div id="addressList" class="address-list">
                <div class="loading" style="margin: 20px auto;"></div>
            </div>
        </div>

        <div class="checkout-section">
            <h2>🛒 商品清单</h2>
            <div id="cartItems"></div>
        </div>

        <div class="checkout-section">
            <h2>📝 订单备注</h2>
            <textarea id="remark" class="remark-input" rows="3" placeholder="请输入订单备注（选填）"></textarea>
        </div>

        <div class="checkout-section">
            <div class="order-summary">
                <div class="summary-row"><span>商品数量</span><span id="totalQuantity">0 件</span></div>
                <div class="summary-row"><span>商品金额</span><span id="subtotal">¥0.00</span></div>
                <div class="summary-row"><span>配送费</span><span>免运费</span></div>
                <div class="summary-row total"><span>应付总额</span><span id="totalAmount">¥0.00</span></div>
            </div>
        </div>

        <div class="checkout-actions">
            <button id="submitBtn" class="btn btn-primary" onclick="submitOrder()">提交订单</button>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/common/footer.jsp" %>

    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        let selectedAddressId = null;
        let cartItems = [];

        async function loadAddresses() {
            try {
                const response = await apiGet(contextPath + '/api/address');
                const addresses = response.data || [];
                renderAddresses(addresses);
            } catch (error) {
                document.getElementById('addressList').innerHTML = '<div class="no-address">加载地址失败</div>';
            }
        }

        function renderAddresses(addresses) {
            if (addresses.length === 0) {
                document.getElementById('addressList').innerHTML = '<div class="no-address"><p>暂无收货地址</p><a href="' + contextPath + '/user/address" class="btn btn-primary" style="margin-top:15px;">添加地址</a></div>';
                return;
            }

            let html = '';
            addresses.forEach((addr, index) => {
                if (addr.isDefault || index === 0) selectedAddressId = addr.id;
                html += '<label class="address-option ' + (addr.isDefault ? 'selected' : '') + '" onclick="selectAddress(' + addr.id + ', this)">' +
                    '<input type="radio" name="address" value="' + addr.id + '" ' + (addr.isDefault ? 'checked' : '') + '>' +
                    '<div class="address-info">' +
                        '<span class="address-name">' + addr.receiverName + '</span>' +
                        '<span class="address-phone">' + addr.phone + '</span>' +
                        '<div class="address-detail">' + addr.province + ' ' + addr.city + ' ' + addr.district + ' ' + addr.detail + '</div>' +
                    '</div>' +
                '</label>';
            });
            document.getElementById('addressList').innerHTML = html;
        }

        function selectAddress(id, el) {
            selectedAddressId = id;
            document.querySelectorAll('.address-option').forEach(opt => opt.classList.remove('selected'));
            el.classList.add('selected');
        }

        async function loadCartItems() {
            try {
                const response = await getCartWithAuth();
                // API返回格式: { data: { items: [...], totalAmount, totalCount } }
                const cartData = response.data || {};
                cartItems = cartData.items || [];
                renderCartItems();
            } catch (error) {
                console.error('加载购物车失败:', error);
            }
        }

        function renderCartItems() {
            if (cartItems.length === 0) {
                document.getElementById('cartItems').innerHTML = '<div class="no-address"><p>购物车是空的</p><a href="' + contextPath + '/cake/list" class="btn btn-primary" style="margin-top:15px;">去选购</a></div>';
                return;
            }

            let html = '';
            let total = 0;
            let quantity = 0;
            cartItems.forEach(item => {
                const price = parseFloat(item.cakePrice || item.price);
                const name = item.cakeName || item.name || '';
                const image = item.cakeImage || item.image || '';
                const subtotal = price * item.quantity;
                total += subtotal;
                quantity += item.quantity;
                html += '<div class="cart-item">' +
                    '<img src="' + resolveCakeImageUrl(image, contextPath) + '" onerror="this.src=\'' + contextPath + '/images/no-image.svg\'">' +
                    '<div class="cart-item-info"><div class="cart-item-name">' + name + '</div><div style="color:var(--text-light);">¥' + price.toFixed(2) + ' × ' + item.quantity + '</div></div>' +
                    '<div class="cart-item-price">¥' + subtotal.toFixed(2) + '</div>' +
                '</div>';
            });
            document.getElementById('cartItems').innerHTML = html;
            document.getElementById('totalQuantity').textContent = quantity + ' 件';
            document.getElementById('subtotal').textContent = '¥' + total.toFixed(2);
            document.getElementById('totalAmount').textContent = '¥' + total.toFixed(2);
        }

        async function submitOrder() {
            if (!selectedAddressId) {
                showToast('请选择收货地址', 'error');
                return;
            }
            if (cartItems.length === 0) {
                showToast('购物车是空的', 'error');
                return;
            }

            const remark = document.getElementById('remark').value.trim();

            try {
                document.getElementById('submitBtn').disabled = true;
                document.getElementById('submitBtn').textContent = '提交中...';

                const response = await apiPost(contextPath + '/api/order', { addressId: selectedAddressId, remark: remark });
                showToast('下单成功', 'success');
                setTimeout(() => {
                    window.location.href = contextPath + '/order/' + response.data.id;
                }, 1000);
            } catch (error) {
                document.getElementById('submitBtn').disabled = false;
                document.getElementById('submitBtn').textContent = '提交订单';
            }
        }

        loadAddresses();
        loadCartItems();
    </script>
</body>
</html>
