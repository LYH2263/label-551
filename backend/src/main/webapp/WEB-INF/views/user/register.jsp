<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>注册 - 网上蛋糕城</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user.css">
</head>
<body>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>

    <div class="container">
        <div class="card register-box">
            <h2>用户注册</h2>
            <form id="registerForm">
                <div class="form-group">
                    <label for="username">用户名</label>
                    <input type="text" id="username" name="username" placeholder="请输入用户名（2-20个字符）" required>
                    <span class="error-msg" id="usernameError"></span>
                </div>
                <div class="form-group">
                    <label for="phone">手机号</label>
                    <input type="text" id="phone" name="phone" placeholder="请输入手机号" required>
                    <span class="error-msg" id="phoneError"></span>
                </div>
                <div class="form-group">
                    <label for="password">密码</label>
                    <input type="password" id="password" name="password" placeholder="请输入密码（至少6位）" required>
                    <span class="error-msg" id="passwordError"></span>
                </div>
                <div class="form-group">
                    <label for="confirmPassword">确认密码</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" placeholder="请再次输入密码" required>
                    <span class="error-msg" id="confirmPasswordError"></span>
                </div>
                <button type="submit" class="btn btn-primary btn-full">注册</button>
            </form>
            <div class="form-footer">
                已有账号？<a href="${pageContext.request.contextPath}/user/login">立即登录</a>
            </div>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/common/footer.jsp" %>

    <script src="${pageContext.request.contextPath}/js/validation.js"></script>
    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        // 实时验证
        const phoneInput = document.getElementById('phone');
        const usernameInput = document.getElementById('username');
        const passwordInput = document.getElementById('password');
        const confirmPasswordInput = document.getElementById('confirmPassword');

        phoneInput.addEventListener('blur', function() {
            if (this.value && !validatePhone(this.value)) {
                showError('phoneError', '请输入正确的手机号格式');
            } else {
                clearError('phoneError');
            }
        });

        usernameInput.addEventListener('blur', function() {
            if (this.value && !validateUsername(this.value)) {
                showError('usernameError', '用户名长度应为2-20个字符');
            } else {
                clearError('usernameError');
            }
        });

        passwordInput.addEventListener('blur', function() {
            if (this.value && !validatePassword(this.value)) {
                showError('passwordError', '密码至少6位');
            } else {
                clearError('passwordError');
            }
        });

        confirmPasswordInput.addEventListener('blur', function() {
            if (this.value && this.value !== passwordInput.value) {
                showError('confirmPasswordError', '两次密码不一致');
            } else {
                clearError('confirmPasswordError');
            }
        });

        // 表单提交
        document.getElementById('registerForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const username = usernameInput.value.trim();
            const phone = phoneInput.value.trim();
            const password = passwordInput.value.trim();
            const confirmPassword = confirmPasswordInput.value.trim();

            // 前端验证
            clearError('usernameError');
            clearError('phoneError');
            clearError('passwordError');
            clearError('confirmPasswordError');

            let hasError = false;

            if (!validateUsername(username)) {
                showError('usernameError', '用户名长度应为2-20个字符');
                hasError = true;
            }

            if (!validatePhone(phone)) {
                showError('phoneError', '请输入正确的手机号格式');
                hasError = true;
            }

            if (!validatePassword(password)) {
                showError('passwordError', '密码至少6位');
                hasError = true;
            }

            if (password !== confirmPassword) {
                showError('confirmPasswordError', '两次密码不一致');
                hasError = true;
            }

            if (hasError) return;

            // 提交注册
            try {
                await register(phone, password, username);
                showToast('注册成功，即将跳转到登录页');
                setTimeout(() => {
                    window.location.href = '${pageContext.request.contextPath}/user/login';
                }, 1500);
            } catch (error) {
                // 错误已在 common.js 中处理
            }
        });
    </script>
</body>
</html>
