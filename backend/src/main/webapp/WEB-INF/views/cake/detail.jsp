<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${cake.name} - 蛋糕详情</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cake.css">
</head>
<body>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>

    <div class="container">
        <!-- 面包屑 -->
        <div class="breadcrumb">
            <a href="${pageContext.request.contextPath}/">首页</a>
            <span>&gt;</span>
            <a href="${pageContext.request.contextPath}/cake/list">蛋糕列表</a>
            <span>&gt;</span>
            <span>${cake.name}</span>
        </div>

        <!-- 蛋糕详情 -->
        <c:set var="cakeImageSrc" value="${pageContext.request.contextPath}/images/no-image.svg" />
        <c:if test="${not empty cake.image}">
            <c:choose>
                <c:when test="${fn:startsWith(cake.image, 'http://') or fn:startsWith(cake.image, 'https://')}">
                    <c:set var="cakeImageSrc" value="${cake.image}" />
                </c:when>
                <c:when test="${fn:startsWith(cake.image, '/uploads/')}">
                    <c:set var="cakeImageSrc" value="${pageContext.request.contextPath}${cake.image}" />
                </c:when>
                <c:otherwise>
                    <c:set var="cakeImageSrc" value="${pageContext.request.contextPath}/uploads/cakes/${cake.image}" />
                </c:otherwise>
            </c:choose>
        </c:if>
        <div class="cake-detail">
            <div>
                <img src="${cakeImageSrc}"
                     alt="${cake.name}"
                     class="cake-detail-image"
                     onerror="this.src='${pageContext.request.contextPath}/images/no-image.svg'">
            </div>
            <div class="cake-detail-info">
                <h1>${cake.name}</h1>

                <div class="cake-detail-meta">
                    <p><strong>分类：</strong>${cake.categoryName}</p>
                    <c:if test="${not empty cake.size}">
                        <p><strong>尺寸：</strong>${cake.size}</p>
                    </c:if>
                    <c:if test="${not empty cake.flavor}">
                        <p><strong>口味：</strong>${cake.flavor}</p>
                    </c:if>
                    <p><strong>库存：</strong>
                        <c:choose>
                            <c:when test="${cake.stock > 0}">
                                <span>${cake.stock} 份</span>
                            </c:when>
                            <c:otherwise>
                                <span class="out-of-stock">暂时缺货</span>
                            </c:otherwise>
                        </c:choose>
                    </p>
                    <p><strong>已售：</strong>${cake.sales} 份</p>
                </div>

                <div class="cake-detail-price">
                    ¥${cake.price}
                </div>

                <div class="cake-detail-description">
                    <strong>商品描述：</strong><br>
                    ${cake.description}
                </div>

                <div class="cake-detail-actions">
                    <div class="quantity-selector large">
                        <button onclick="changeQuantity(-1)" id="minusBtn">−</button>
                        <span class="quantity-value" id="quantity">1</span>
                        <button onclick="changeQuantity(1)" id="plusBtn">+</button>
                    </div>
                    <c:choose>
                        <c:when test="${cake.stock > 0}">
                            <button class="btn btn-primary" onclick="addToCart()">加入购物车</button>
                        </c:when>
                        <c:otherwise>
                            <button class="btn btn-primary" disabled>暂时缺货</button>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/common/footer.jsp" %>

    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        const maxStock = ${cake.stock};
        const cakeId = ${cake.id};
        let currentQuantity = 1;

        function updateQuantityDisplay() {
            document.getElementById('quantity').textContent = currentQuantity;
            document.getElementById('minusBtn').disabled = currentQuantity <= 1;
            document.getElementById('plusBtn').disabled = currentQuantity >= maxStock;
        }

        function changeQuantity(delta) {
            let value = currentQuantity + delta;
            if (value < 1) value = 1;
            if (value > maxStock) value = maxStock;
            currentQuantity = value;
            updateQuantityDisplay();
        }

        async function addToCart() {
            try {
                await apiPost('${pageContext.request.contextPath}/api/cart', {
                    cakeId: cakeId,
                    quantity: currentQuantity
                });
                showToast('已加入购物车');
                updateCartCount();
            } catch (error) {
                if (error.message.includes('未登录')) {
                    showToast('请先登录', 'error');
                    setTimeout(() => {
                        window.location.href = '${pageContext.request.contextPath}/user/login';
                    }, 1500);
                }
            }
        }

        // 初始化按钮状态
        updateQuantityDisplay();
    </script>
</body>
</html>
