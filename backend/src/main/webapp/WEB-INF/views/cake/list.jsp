<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>蛋糕列表 - 网上蛋糕城</title>
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
            <span>蛋糕列表</span>
        </div>

        <h1 class="page-title">蛋糕列表</h1>

        <!-- 筛选栏 -->
        <div class="cake-list-header">
            <div class="cake-filters">
                <select id="categoryFilter" onchange="filterCakes()">
                    <option value="">全部分类</option>
                    <c:forEach var="category" items="${categories}">
                        <option value="${category.id}" ${categoryId == category.id ? 'selected' : ''}>
                            ${category.name}
                        </option>
                    </c:forEach>
                </select>
                <input type="text" id="searchInput" placeholder="搜索蛋糕名称..."
                       value="${keyword}" onkeypress="handleSearch(event)">
                <button class="btn btn-primary" onclick="filterCakes()">搜索</button>
            </div>
        </div>

        <!-- 蛋糕列表 -->
        <c:choose>
            <c:when test="${not empty cakes}">
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
                        <div class="cake-card" onclick="window.location.href='${pageContext.request.contextPath}/cake/${cake.id}'">
                            <img src="${cakeImageSrc}"
                                 alt="${cake.name}"
                                 class="cake-image"
                                 onerror="this.src='${pageContext.request.contextPath}/images/no-image.svg'">
                            <div class="cake-card-body">
                                <span class="cake-card-category">${cake.categoryName}</span>
                                <h3 class="cake-card-title">${cake.name}</h3>
                                <p style="font-size: 12px; color: #999; margin-bottom: 10px;">
                                    ${cake.description}
                                </p>
                                <div class="cake-card-footer">
                                    <span class="cake-price">¥${cake.price}</span>
                                    <span class="cake-stock ${cake.stock <= 0 ? 'out-of-stock' : ''}">
                                        ${cake.stock > 0 ? '库存: ' : '暂时'}
                                        ${cake.stock > 0 ? cake.stock : '缺货'}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <!-- 分页 -->
                <c:if test="${total > pageSize}">
                    <div class="pagination">
                        <c:if test="${page > 1}">
                            <a href="?page=${page - 1}&categoryId=${categoryId}&keyword=${keyword}">上一页</a>
                        </c:if>

                        <c:forEach begin="1" end="${(total + pageSize - 1) / pageSize}" var="i">
                            <c:if test="${i == page}">
                                <span class="active">${i}</span>
                            </c:if>
                            <c:if test="${i != page}">
                                <a href="?page=${i}&categoryId=${categoryId}&keyword=${keyword}">${i}</a>
                            </c:if>
                        </c:forEach>

                        <c:if test="${page < (total + pageSize - 1) / pageSize}">
                            <a href="?page=${page + 1}&categoryId=${categoryId}&keyword=${keyword}">下一页</a>
                        </c:if>
                    </div>
                </c:if>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <div class="empty-state-icon">🎂</div>
                    <div class="empty-state-text">暂无蛋糕数据</div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <%@ include file="/WEB-INF/views/common/footer.jsp" %>

    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        function filterCakes() {
            const categoryId = document.getElementById('categoryFilter').value;
            const keyword = document.getElementById('searchInput').value;

            let url = '${pageContext.request.contextPath}/cake/list?';
            if (categoryId) {
                url += 'categoryId=' + categoryId + '&';
            }
            if (keyword) {
                url += 'keyword=' + encodeURIComponent(keyword);
            }

            window.location.href = url;
        }

        function handleSearch(event) {
            if (event.key === 'Enter') {
                filterCakes();
            }
        }
    </script>
</body>
</html>
