<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<header class="header">
    <div class="header-glass-bg"></div>
    <div class="container">
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&display=swap" rel="stylesheet">

        <a href="${pageContext.request.contextPath}/" class="logo">
            <span class="logo-icon">🎂</span>
            <span class="logo-text">网上蛋糕城</span>
        </a>

        <nav>
            <ul class="nav-menu">
                <li>
                    <a href="${pageContext.request.contextPath}/" class="${pageContext.request.requestURI.endsWith('/index.jsp') || pageContext.request.requestURI.endsWith('/') ? 'active' : ''}">首页</a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/cake/list" class="${pageContext.request.requestURI.contains('/cake/list') ? 'active' : ''}">蛋糕列表</a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/cart" class="${pageContext.request.requestURI.contains('/cart') ? 'active' : ''}">购物车</a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/order/list" class="${pageContext.request.requestURI.contains('/order/list') ? 'active' : ''}">我的订单</a>
                </li>
            </ul>
        </nav>

        <div class="user-actions">
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <div class="user-profile-menu">
                        <a href="${pageContext.request.contextPath}/user/profile" class="profile-card" title="个人中心">
                            <span class="profile-avatar">${fn:length(sessionScope.user.username) > 0 ? fn:substring(sessionScope.user.username, 0, 1) : 'U'}</span>
                            <span class="user-greeting">
                                <span class="greeting-text">您好, </span>
                                <span class="username">${sessionScope.user.username}</span>
                            </span>
                        </a>
                    </div>

                    <div class="cart-btn-wrapper">
                        <a href="${pageContext.request.contextPath}/cart" class="cart-icon-btn" title="购物车">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="9" cy="21" r="1"></circle>
                                <circle cx="20" cy="21" r="1"></circle>
                                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                            </svg>
                            <span class="cart-text">购物车</span>
                        </a>
                        <span class="header-badge cart-count">0</span>
                    </div>

                    <c:if test="${sessionScope.user.role == 'admin'}">
                        <a href="${pageContext.request.contextPath}/admin" class="admin-link">后台管理</a>
                    </c:if>
                    <a href="javascript:void(0)" onclick="doLogout()" class="logout-link" title="退出登录">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                            <polyline points="16 17 21 12 16 7"></polyline>
                            <line x1="21" y1="12" x2="9" y2="12"></line>
                        </svg>
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/user/login" class="login-link">登录</a>
                    <a href="${pageContext.request.contextPath}/user/register" class="btn btn-primary btn-register">注册</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>

<script>
    async function doLogout() {
        const confirmed = await showConfirm({
            title: '退出登录',
            message: '确定要退出登录吗？',
            type: 'warning',
            confirmText: '退出',
            cancelText: '取消'
        });

        if (confirmed) {
            try {
                await logout();
                showToast('退出成功', 'success');
                setTimeout(() => {
                    window.location.href = '${pageContext.request.contextPath}/';
                }, 1000);
            } catch (error) {
                console.error('退出失败:', error);
            }
        }
    }
</script>
