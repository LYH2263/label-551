<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 网上蛋糕城</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user.css">
</head>
<body>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>

    <div class="container">
        <div class="card login-box">
            <h2>用户登录</h2>
            <form id="loginForm">
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
                <button type="submit" class="btn btn-primary btn-full">登录</button>
            </form>
            <div class="form-footer">
                没有账号？<a href="${pageContext.request.contextPath}/user/register">立即注册</a>
            </div>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/common/footer.jsp" %>

    <script src="${pageContext.request.contextPath}/js/validation.js"></script>
    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        // 手机号实时验证
        const phoneInput = document.getElementById('phone');
        phoneInput.addEventListener('blur', function() {
            if (this.value && !validatePhone(this.value)) {
                showError('phoneError', '请输入正确的手机号格式');
            } else {
                clearError('phoneError');
            }
        });

        // 表单提交
        document.getElementById('loginForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const phone = phoneInput.value.trim();
            const password = document.getElementById('password').value.trim();

            // 前端验证
            clearError('phoneError');
            clearError('passwordError');

            let hasError = false;

            if (!validatePhone(phone)) {
                showError('phoneError', '请输入正确的手机号格式');
                hasError = true;
            }

            if (!validatePassword(password)) {
                showError('passwordError', '密码至少6位');
                hasError = true;
            }

            if (hasError) return;

            // 提交登录
            try {
                await login(phone, password);
                showToast('登录成功');
                const currentUser = await checkLogin();
                setTimeout(() => {
                    if (currentUser && currentUser.role === 'admin') {
                        window.location.href = '${pageContext.request.contextPath}/admin';
                    } else {
                        window.location.href = '${pageContext.request.contextPath}/';
                    }
                }, 1000);
            } catch (error) {
                // 错误已在 common.js 中处理
            }
        });
    </script>
</body>
</html>
