// 表单验证工具函数

// 手机号格式验证（中国大陆）
function validatePhone(phone) {
    const regex = /^1[3-9]\d{9}$/;
    return regex.test(phone);
}

// 密码强度验证（至少6位）
function validatePassword(password) {
    return password && password.length >= 6;
}

// 密码强度验证（强密码：至少8位，包含字母和数字）
function validateStrongPassword(password) {
    if (!password || password.length < 8) {
        return false;
    }
    const hasLetter = /[a-zA-Z]/.test(password);
    const hasNumber = /\d/.test(password);
    return hasLetter && hasNumber;
}

// 用户名验证（2-20个字符）
function validateUsername(username) {
    return username && username.length >= 2 && username.length <= 20;
}

// 邮箱验证
function validateEmail(email) {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

// 显示错误信息
function showError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }
}

// 清除错误信息
function clearError(elementId) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = '';
        errorElement.style.display = 'none';
    }
}

// 显示成功信息
function showSuccess(elementId, message) {
    const successElement = document.getElementById(elementId);
    if (successElement) {
        successElement.textContent = message;
        successElement.style.display = 'block';
    }
}

// Toast 提示
function showToast(message, type = 'success') {
    // 移除已存在的 toast
    const existingToast = document.querySelector('.toast');
    if (existingToast) {
        existingToast.remove();
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 3000);
}

// 确认对话框 (已迁移到 common.js 的现代化弹窗)
// 此函数保留用于向后兼容，实际调用 common.js 中的 showDeleteConfirm
async function confirmDelete(message) {
    // 如果 showDeleteConfirm 存在（common.js 已加载），使用现代化弹窗
    if (typeof showDeleteConfirm === 'function') {
        return await showDeleteConfirm(message || '确定要删除吗？');
    }
    // 回退到原生 confirm（common.js 未加载时）
    return confirm(message || '确定要删除吗？');
}

// 格式化价格
function formatPrice(price) {
    return '¥' + parseFloat(price).toFixed(2);
}

// 格式化日期
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
}

// 获取URL参数
function getUrlParam(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

// 构建URL
function buildUrl(baseUrl, params) {
    const url = new URL(baseUrl, window.location.origin);
    Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
            url.searchParams.set(key, params[key]);
        }
    });
    return url.toString();
}
