<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理员登录 - 网上蛋糕城</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user.css">
</head>
<body style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center;">
    <div class="container" style="max-width: 500px;">
        <div class="card login-box" style="margin: 0;">
            <h2 style="color: #667eea;">管理员登录</h2>
            <form id="loginForm">
                <div class="form-group">
                    <label for="phone">手机号</label>
                    <input type="text" id="phone" name="phone" placeholder="请输入管理员手机号" required>
                    <span class="error-msg" id="phoneError"></span>
                </div>
                <div class="form-group">
                    <label for="password">密码</label>
                    <input type="password" id="password" name="password" placeholder="请输入密码" required>
                    <span class="error-msg" id="passwordError"></span>
                </div>
                <button type="submit" class="btn btn-primary btn-full" style="background: linear-gradient(135deg, #667eea, #764ba2);">登录</button>
            </form>
            <div class="form-footer">
                <a href="${pageContext.request.contextPath}/">返回前台</a>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/validation.js"></script>
    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        const phoneInput = document.getElementById('phone');
        phoneInput.addEventListener('blur', function() {
            if (this.value && !validatePhone(this.value)) {
                showError('phoneError', '请输入正确的手机号格式');
            } else {
                clearError('phoneError');
            }
        });

        document.getElementById('loginForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const phone = phoneInput.value.trim();
            const password = document.getElementById('password').value.trim();

            clearError('phoneError');
            clearError('passwordError');

            let hasError = false;

            if (!validatePhone(phone)) {
                showError('phoneError', '请输入正确的手机号格式');
                hasError = true;
            }

            if (!password) {
                showError('passwordError', '请输入密码');
                hasError = true;
            }

            if (hasError) return;

            try {
                await login(phone, password);

                // 检查是否是管理员
                const userResponse = await checkLogin();
                if (userResponse && userResponse.role === 'admin') {
                    showToast('登录成功');
                    setTimeout(() => {
                        window.location.href = '${pageContext.request.contextPath}/admin';
                    }, 1000);
                } else {
                    showToast('您不是管理员', 'error');
                    await logout();
                }
            } catch (error) {
                console.error('登录失败:', error);
            }
        });
    </script>
</body>
</html>
