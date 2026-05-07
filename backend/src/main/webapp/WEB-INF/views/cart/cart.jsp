<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>购物车 - 网上蛋糕城</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cart.css">
</head>
<body>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>

    <div class="container">
        <h1 class="page-title">购物车</h1>

        <div id="cartContent">
            <div class="empty-cart">
                <div class="empty-cart-icon">🛒</div>
                <div class="empty-cart-text">购物车是空的</div>
                <a href="${pageContext.request.contextPath}/cake/list" class="btn btn-primary">去选购蛋糕</a>
            </div>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/common/footer.jsp" %>

    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';

        async function loadCart() {
            try {
                // 使用带认证检查的方法，未登录自动跳转登录页
                const response = await getCartWithAuth();
                // API返回格式: { data: { items: [...], totalAmount, totalCount } }
                const cartData = response.data || {};
                const cartItems = cartData.items || [];

                if (cartItems.length === 0) {
                    document.getElementById('cartContent').innerHTML =
                        '<div class="empty-cart">' +
                            '<div class="empty-cart-icon">🛒</div>' +
                            '<div class="empty-cart-text">购物车是空的</div>' +
                            '<a href="' + contextPath + '/cake/list" class="btn btn-primary">去选购蛋糕</a>' +
                        '</div>';
                    return;
                }

                let html = '<table class="cart-table"><thead><tr>';
                html += '<th>商品信息</th><th>单价</th><th>数量</th><th>小计</th><th>操作</th>';
                html += '</tr></thead><tbody>';

                let total = 0;
                let totalQuantity = 0;
                cartItems.forEach(function(item) {
                    // 字段映射: cakePrice -> price, cakeName -> name, cakeImage -> image
                    const price = parseFloat(item.cakePrice || item.price);
                    const subtotal = price * item.quantity;
                    total += subtotal;
                    totalQuantity += item.quantity;
                    const name = item.cakeName || item.name || '';
                    const image = item.cakeImage || item.image || '';

                    html += '<tr>' +
                            '<td>' +
                                '<div class="cart-item-info">' +
                                    '<img src="' + resolveCakeImageUrl(image, contextPath) + '"' +
                                         ' class="cart-item-image"' +
                                         ' onerror="this.src=\'' + contextPath + '/images/no-image.svg\'">' +
                                    '<div>' +
                                        '<div class="cart-item-name">' + name + '</div>' +
                                        '<div class="cart-item-spec">' + (item.spec || '') + '</div>' +
                                    '</div>' +
                                '</div>' +
                            '</td>' +
                            '<td>¥' + price.toFixed(2) + '</td>' +
                            '<td>' +
                                '<div class="quantity-selector">' +
                                    '<button onclick="updateQuantity(' + item.id + ', ' + (item.quantity - 1) + ')" ' + (item.quantity <= 1 ? 'disabled' : '') + '>−</button>' +
                                    '<span class="quantity-value">' + item.quantity + '</span>' +
                                    '<button onclick="updateQuantity(' + item.id + ', ' + (item.quantity + 1) + ')">+</button>' +
                                '</div>' +
                            '</td>' +
                            '<td style="color: #ff6b6b; font-weight: bold;">¥' + subtotal.toFixed(2) + '</td>' +
                            '<td>' +
                                '<button class="btn btn-danger" onclick="removeItem(' + item.id + ')" style="padding: 6px 12px; font-size: 12px;">删除</button>' +
                            '</td>' +
                        '</tr>';
                });

                html += '</tbody></table>';

                html += '<div class="cart-summary">' +
                        '<div class="cart-summary-row">' +
                            '<span>商品数量：</span>' +
                            '<span>' + totalQuantity + ' 件</span>' +
                        '</div>' +
                        '<div class="cart-summary-row total">' +
                            '<span>合计：</span>' +
                            '<span>¥' + total.toFixed(2) + '</span>' +
                        '</div>' +
                        '<div class="cart-actions">' +
                            '<a href="' + contextPath + '/cake/list" class="btn btn-secondary">继续购物</a>' +
                            '<button class="btn btn-primary" onclick="checkout()">去结算</button>' +
                        '</div>' +
                    '</div>';

                document.getElementById('cartContent').innerHTML = html;
            } catch (error) {
                console.error('加载购物车失败:', error);
            }
        }

        async function updateQuantity(itemId, quantity) {
            if (quantity < 1) return;
            try {
                await apiPut(contextPath + '/api/cart/' + itemId, { quantity: quantity });
                showToast('已更新');
                loadCart();
                updateCartCount();
            } catch (error) {
                console.error('更新失败:', error);
            }
        }

        async function removeItem(itemId) {
            const confirmed = await showDeleteConfirm('确定要删除这个商品吗？');
            if (!confirmed) return;
            try {
                await apiDelete(contextPath + '/api/cart/' + itemId);
                showToast('已删除', 'success');
                loadCart();
                updateCartCount();
            } catch (error) {
                console.error('删除失败:', error);
            }
        }

        function checkout() {
            window.location.href = contextPath + '/order/checkout';
        }

        // 页面加载时获取购物车数据
        loadCart();
    </script>
</body>
</html>
