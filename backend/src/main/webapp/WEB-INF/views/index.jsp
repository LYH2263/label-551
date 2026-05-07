<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
        <!DOCTYPE html>
        <html lang="zh-CN">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>首页 - 网上蛋糕城</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cake.css">
        </head>

        <body>
            <%@ include file="/WEB-INF/views/common/header.jsp" %>

                <div class="container">
                    <!-- 欢迎横幅 -->
                    <div class="hero-banner">
                        <h1>🎂 欢迎来到网上蛋糕城</h1>
                        <p>新鲜制作 · 精美配送 · 品质保证</p>
                        <div class="hero-banner-btn-wrapper">
                            <a href="${pageContext.request.contextPath}/cake/list" class="btn btn-hero-action">浏览全部蛋糕</a>
                        </div>
                    </div>

                    <!-- 分类导航 -->
                    <div class="category-tags">
                        <a href="${pageContext.request.contextPath}/cake/list"
                            class="category-tag ${empty categoryId ? 'active' : ''}">全部</a>
                        <c:forEach var="category" items="${categories}">
                            <a href="${pageContext.request.contextPath}/cake/list?categoryId=${category.id}"
                                class="category-tag ${categoryId == category.id ? 'active' : ''}">
                                ${category.name}
                            </a>
                        </c:forEach>
                    </div>

                    <!-- 热门蛋糕 -->
                    <h2 class="page-title">热门推荐</h2>
                    <div class="cake-grid">
                        <c:forEach var="cake" items="${cakes}">
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
                            <div class="cake-card"
                                onclick="window.location.href='${pageContext.request.contextPath}/cake/${cake.id}'">
                                <div class="cake-image-wrapper">
                                    <img src="${cakeImageSrc}"
                                        alt="${cake.name}" class="cake-image"
                                        onerror="this.src='${pageContext.request.contextPath}/images/no-image.svg'">
                                </div>
                                <div class="cake-card-body">
                                    <span class="cake-card-category">${cake.categoryName}</span>
                                    <h3 class="cake-card-title">${cake.name}</h3>
                                    <div class="cake-card-footer">
                                        <span class="cake-price">¥${cake.price}</span>
                                        <span class="cake-stock ${cake.stock <= 0 ? 'out-of-stock' : ''}">
                                            ${cake.stock > 0 ? '有货' : '缺货'}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <!-- 查看更多 -->
                    <div class="section-more-cakes">
                        <a href="${pageContext.request.contextPath}/cake/list"
                            class="btn btn-primary btn-lg btn-hero-action">
                            查看更多蛋糕
                        </a>
                    </div>
                </div>

                <%@ include file="/WEB-INF/views/common/footer.jsp" %>

                    <script src="${pageContext.request.contextPath}/js/common.js"></script>
        </body>

        </html>
