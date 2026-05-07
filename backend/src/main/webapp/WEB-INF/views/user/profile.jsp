<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>个人中心 - 网上蛋糕城</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user.css">
</head>
<body>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>

    <div class="container">
        <h1 class="page-title">个人中心</h1>

        <div class="content-wrapper">
            <div class="sidebar">
                <ul class="sidebar-menu">
                    <li><a href="${pageContext.request.contextPath}/user/profile" class="active">个人信息</a></li>
                    <li><a href="${pageContext.request.contextPath}/user/address">收货地址</a></li>
                    <li><a href="${pageContext.request.contextPath}/order/list">我的订单</a></li>
                </ul>
            </div>

            <div class="main-content">
                <div class="card">
                    <h2 style="margin-bottom: 30px; font-size: 24px;">个人信息</h2>

                    <form id="profileForm">
                        <div class="form-group">
                            <label>用户名</label>
                            <input type="text" id="username" name="username" placeholder="请输入用户名" required>
                        </div>
                        <div class="form-group">
                            <label>手机号</label>
                            <input type="tel" id="phone" name="phone" placeholder="请输入手机号" required>
                        </div>
                        <div class="form-group">
                            <label>注册时间</label>
                            <input type="text" id="createdAt" readonly style="background: #f5f5f5;">
                        </div>
                        <button type="submit" class="btn btn-primary">保存修改</button>
                    </form>
                </div>

                <div class="card" style="margin-top: 30px;">
                    <h2 style="margin-bottom: 30px; font-size: 24px;">修改密码</h2>

                    <form id="passwordForm">
                        <div class="form-group">
                            <label>原密码</label>
                            <input type="password" id="oldPassword" name="oldPassword" placeholder="请输入原密码" required>
                        </div>
                        <div class="form-group">
                            <label>新密码</label>
                            <input type="password" id="newPassword" name="newPassword" placeholder="请输入新密码（至少6位）" required minlength="6">
                        </div>
                        <div class="form-group">
                            <label>确认新密码</label>
                            <input type="password" id="confirmPassword" name="confirmPassword" placeholder="请再次输入新密码" required>
                        </div>
                        <button type="submit" class="btn btn-primary">修改密码</button>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/common/footer.jsp" %>

    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';

        // 加载用户信息
        async function loadProfile() {
            try {
                const response = await apiGet(contextPath + '/api/user/profile');
                const user = response.data;
                document.getElementById('username').value = user.username || '';
                document.getElementById('phone').value = user.phone || '';
                document.getElementById('createdAt').value = user.createdAt || '';
            } catch (error) {
                console.error('加载用户信息失败:', error);
            }
        }

        // 保存个人信息
        document.getElementById('profileForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const username = document.getElementById('username').value.trim();
            const phone = document.getElementById('phone').value.trim();

            if (!username) {
                showToast('用户名不能为空', 'error');
                return;
            }

            // 手机号格式校验
            if (!/^1[3-9]\d{9}$/.test(phone)) {
                showToast('请输入正确的手机号格式', 'error');
                return;
            }

            try {
                await apiPut(contextPath + '/api/user/profile', { username, phone });
                showToast('保存成功', 'success');
            } catch (error) {
                console.error('保存失败:', error);
            }
        });

        // 修改密码
        document.getElementById('passwordForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const oldPassword = document.getElementById('oldPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (newPassword.length < 6) {
                showToast('新密码长度不能少于6位', 'error');
                return;
            }

            if (newPassword !== confirmPassword) {
                showToast('两次输入的密码不一致', 'error');
                return;
            }

            try {
                await apiPut(contextPath + '/api/user/password', { oldPassword, newPassword });
                showToast('密码修改成功', 'success');
                document.getElementById('passwordForm').reset();
            } catch (error) {
                console.error('密码修改失败:', error);
            }
        });

        // 页面加载
        loadProfile();
    </script>
</body>
</html>
