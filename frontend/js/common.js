// 通用 AJAX 请求函数

const API_BASE = '/api';

function normalizeContextPath(contextPath) {
    if (!contextPath || contextPath === '/') {
        return '';
    }
    return contextPath.endsWith('/') ? contextPath.slice(0, -1) : contextPath;
}

function resolveCakeImageUrl(image, contextPath = '') {
    const normalizedContextPath = normalizeContextPath(contextPath);
    const fallbackImage = normalizedContextPath + '/images/no-image.svg';

    if (!image) {
        return fallbackImage;
    }

    const normalizedImage = String(image).trim();
    if (!normalizedImage) {
        return fallbackImage;
    }

    if (/^https?:\/\//i.test(normalizedImage)) {
        return normalizedImage;
    }

    if (normalizedImage.startsWith('/')) {
        return normalizedContextPath + normalizedImage;
    }

    return normalizedContextPath + '/uploads/cakes/' + normalizedImage;
}

// Toast 提示函数
function showToast(message, type = 'info') {
    // 移除已存在的 toast
    const existingToast = document.querySelector('.toast-message');
    if (existingToast) {
        existingToast.remove();
    }

    // 创建 toast 元素
    const toast = document.createElement('div');
    toast.className = `toast-message toast-${type}`;
    toast.textContent = message;

    // 样式
    toast.style.cssText = `
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        padding: 16px 24px;
        border-radius: 50px;
        color: white;
        font-size: 14px;
        font-weight: 500;
        z-index: 10000;
        animation: slideIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        display: flex;
        align-items: center;
        gap: 8px;
        box-shadow: 0 10px 30px rgba(0,0,0,0.15);
        backdrop-filter: blur(12px);
    `;

    // 根据类型设置背景色 (Updated to match Premium Theme)
    switch (type) {
        case 'success':
            toast.style.background = 'rgba(128, 189, 158, 0.95)'; // Muted Green
            toast.innerHTML = '✅ ' + message;
            break;
        case 'error':
            toast.style.background = 'rgba(212, 93, 121, 0.95)'; // Primary Red
            toast.innerHTML = '❌ ' + message;
            break;
        case 'warning':
            toast.style.background = 'rgba(248, 177, 149, 0.95)'; // Peach/Orange
            toast.innerHTML = '⚠️ ' + message;
            break;
        default:
            toast.style.background = 'rgba(108, 91, 123, 0.95)'; // Secondary Purple
            toast.innerHTML = 'ℹ️ ' + message;
    }

    document.body.appendChild(toast);

    // 3秒后自动消失
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// 封装 fetch 请求
// silent: 静默模式，不弹出错误提示（用于后台请求如购物车数量）
// redirectOnUnauth: 401时是否跳转登录页（默认false，购物车页面等需要设为true）
async function apiRequest(url, options = {}, silent = false, redirectOnUnauth = false) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
    };

    const mergedOptions = { ...defaultOptions, ...options };

    try {
        const response = await fetch(url, mergedOptions);

        // 401 未授权
        if (response.status === 401) {
            if (redirectOnUnauth) {
                // 需要跳转登录页
                showToast('请先登录', 'warning');
                setTimeout(() => {
                    window.location.href = '/user/login';
                }, 1000);
            }
            const error = new Error('未登录');
            error.status = 401;
            error.isBusinessError = true;
            throw error;
        }

        const data = await response.json();

        if (data.success) {
            return data;
        } else {
            // 业务错误：标记为非网络错误，保留后端返回的具体消息
            const error = new Error(data.message || '请求失败');
            error.isBusinessError = true;
            throw error;
        }
    } catch (error) {
        // 401 错误不打印日志
        if (error.status !== 401) {
            console.error('API 请求错误:', error);
            if (!silent) {
                // 判断是否为业务错误（后端返回的明确错误消息）
                if (error.isBusinessError && error.message) {
                    // 业务错误：显示后端返回的具体消息
                    showToast(error.message, 'error');
                } else if (error.message &&
                    error.message !== 'Failed to fetch' &&
                    error.message !== 'Load failed' &&
                    error.message !== 'NetworkError when attempting to fetch resource.' &&
                    !error.message.includes('network') &&
                    !error.message.includes('Network')) {
                    // 其他非网络错误消息
                    showToast(error.message, 'error');
                } else {
                    // 真正的网络错误
                    showToast('网络错误，请检查网络连接后重试', 'error');
                }
            }
        }
        throw error;
    }
}

// 静默请求（不弹出错误提示）
async function apiRequestSilent(url, options = {}) {
    return apiRequest(url, options, true);
}

// GET 请求
function apiGet(url, params = {}, silent = false, redirectOnUnauth = false) {
    const fullUrl = params && Object.keys(params).length > 0 ? `${url}?${new URLSearchParams(params)}` : url;
    return apiRequest(fullUrl, { method: 'GET' }, silent, redirectOnUnauth);
}

// POST 请求
function apiPost(url, data = {}) {
    return apiRequest(url, {
        method: 'POST',
        body: JSON.stringify(data),
    });
}

// PUT 请求
function apiPut(url, data = {}) {
    return apiRequest(url, {
        method: 'PUT',
        body: JSON.stringify(data),
    });
}

// DELETE 请求
function apiDelete(url) {
    return apiRequest(url, { method: 'DELETE' });
}

// 检查登录状态
async function checkLogin() {
    try {
        const response = await apiGet(`${API_BASE}/auth/check`);
        return response.data;
    } catch (error) {
        return null;
    }
}

// 用户注册
async function register(phone, password, username) {
    return apiPost(`${API_BASE}/auth/register`, { phone, password, username });
}

// 用户登录
async function login(phone, password) {
    return apiPost(`${API_BASE}/auth/login`, { phone, password });
}

// 用户登出
async function logout() {
    return apiPost(`${API_BASE}/auth/logout`);
}

// 获取蛋糕列表
async function getCakeList(params = {}) {
    return apiGet(`${API_BASE}/cake/list`, params);
}

// 获取蛋糕详情
async function getCakeDetail(id) {
    return apiGet(`${API_BASE}/cake/${id}`);
}

// 获取热门蛋糕
async function getHotCakes() {
    return apiGet(`${API_BASE}/cake/hot`);
}

// 获取购物车（静默模式，未登录不弹错误）
async function getCart() {
    return apiGet(`${API_BASE}/cart`, {}, true);
}

// 获取购物车（需要登录，未登录跳转登录页）
async function getCartWithAuth() {
    return apiGet(`${API_BASE}/cart`, {}, false, true);
}

// 添加到购物车
async function addToCart(cakeId, quantity = 1) {
    return apiPost(`${API_BASE}/cart`, { cakeId, quantity });
}

// 更新购物车
async function updateCartItem(itemId, quantity) {
    return apiPut(`${API_BASE}/cart/${itemId}`, { quantity });
}

// 删除购物车项
async function deleteCartItem(itemId) {
    return apiDelete(`${API_BASE}/cart/${itemId}`);
}

// 创建订单
async function createOrder(addressId) {
    return apiPost(`${API_BASE}/order`, { addressId });
}

// 获取订单列表
async function getOrderList(params = {}) {
    return apiGet(`${API_BASE}/order/list`, params);
}

// 获取订单详情
async function getOrderDetail(id) {
    return apiGet(`${API_BASE}/order/${id}`);
}

// 获取分类列表
async function getCategoryList() {
    return apiGet(`${API_BASE}/category`);
}

// 页面加载完成后检查登录状态
document.addEventListener('DOMContentLoaded', async function () {
    // 检查是否需要登录的页面
    const protectedPages = document.body.dataset.protected === 'true';

    if (protectedPages) {
        const user = await checkLogin();
        if (!user) {
            window.location.href = '/user/login';
            return;
        }

        // 更新用户信息显示
        const userInfoElements = document.querySelectorAll('.user-info');
        userInfoElements.forEach(element => {
            element.textContent = user.username || user.phone;
        });
    }

    // 只有已登录才更新购物车数量（通过检查页面是否有购物车徽章元素）
    const cartBadge = document.querySelector('.cart-count');
    if (cartBadge) {
        updateCartCount();
    }
});

// 更新购物车数量
async function updateCartCount() {
    try {
        const response = await getCart();
        // API返回格式: { data: { items: [...], totalCount } }
        const cartData = response.data || {};
        const cartItems = cartData.items || [];
        const count = cartItems.reduce((sum, item) => sum + item.quantity, 0);

        const cartBadges = document.querySelectorAll('.cart-count');
        cartBadges.forEach(badge => {
            badge.textContent = count;
        });
    } catch (error) {
        // 未登录或获取失败，忽略
    }
}

// ============================================================
// 现代化确认弹窗组件 (替代原生 confirm/alert)
// ============================================================

// 创建弹窗容器样式（只创建一次）
function initModalStyles() {
    if (document.getElementById('modal-styles')) return;

    const style = document.createElement('style');
    style.id = 'modal-styles';
    style.textContent = `
        .confirm-modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            backdrop-filter: blur(4px);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10001;
            opacity: 0;
            transition: opacity 0.3s ease;
        }
        .confirm-modal-overlay.active {
            opacity: 1;
        }
        .confirm-modal {
            background: white;
            border-radius: 20px;
            padding: 32px;
            width: 90%;
            max-width: 400px;
            box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
            transform: scale(0.9) translateY(20px);
            transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
        }
        .confirm-modal-overlay.active .confirm-modal {
            transform: scale(1) translateY(0);
        }
        .confirm-modal-icon {
            width: 64px;
            height: 64px;
            margin: 0 auto 20px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 32px;
        }
        .confirm-modal-icon.warning {
            background: rgba(255, 159, 67, 0.15);
        }
        .confirm-modal-icon.danger {
            background: rgba(238, 82, 83, 0.15);
        }
        .confirm-modal-icon.info {
            background: rgba(72, 219, 251, 0.15);
        }
        .confirm-modal-icon.success {
            background: rgba(16, 172, 132, 0.15);
        }
        .confirm-modal-title {
            font-size: 20px;
            font-weight: 700;
            text-align: center;
            margin-bottom: 12px;
            color: #2D3436;
        }
        .confirm-modal-message {
            font-size: 15px;
            text-align: center;
            color: #636E72;
            margin-bottom: 28px;
            line-height: 1.6;
        }
        .confirm-modal-actions {
            display: flex;
            gap: 12px;
            justify-content: center;
        }
        .confirm-modal-actions .btn {
            min-width: 100px;
            padding: 12px 24px;
        }
    `;
    document.head.appendChild(style);
}

// 显示确认弹窗 (Promise 版本)
function showConfirm(options) {
    return new Promise((resolve) => {
        initModalStyles();

        const {
            title = '确认操作',
            message = '确定要执行此操作吗？',
            confirmText = '确定',
            cancelText = '取消',
            type = 'warning', // warning, danger, info, success
            confirmClass = 'btn-primary'
        } = typeof options === 'string' ? { message: options } : options;

        const icons = {
            warning: '⚠️',
            danger: '🗑️',
            info: 'ℹ️',
            success: '✅'
        };

        const overlay = document.createElement('div');
        overlay.className = 'confirm-modal-overlay';
        overlay.innerHTML = `
            <div class="confirm-modal">
                <div class="confirm-modal-icon ${type}">${icons[type]}</div>
                <div class="confirm-modal-title">${title}</div>
                <div class="confirm-modal-message">${message}</div>
                <div class="confirm-modal-actions">
                    <button class="btn btn-secondary" data-action="cancel">${cancelText}</button>
                    <button class="btn ${confirmClass}" data-action="confirm">${confirmText}</button>
                </div>
            </div>
        `;

        document.body.appendChild(overlay);

        // 触发动画
        requestAnimationFrame(() => {
            overlay.classList.add('active');
        });

        // 绑定事件
        const handleClick = (e) => {
            const action = e.target.dataset.action;
            if (action) {
                overlay.classList.remove('active');
                setTimeout(() => {
                    overlay.remove();
                    resolve(action === 'confirm');
                }, 300);
            }
        };

        // 点击遮罩关闭
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                overlay.classList.remove('active');
                setTimeout(() => {
                    overlay.remove();
                    resolve(false);
                }, 300);
            }
        });

        overlay.querySelector('.confirm-modal-actions').addEventListener('click', handleClick);
    });
}

// 显示删除确认弹窗 (便捷方法)
function showDeleteConfirm(message = '确定要删除吗？此操作不可撤销。') {
    return showConfirm({
        title: '删除确认',
        message: message,
        type: 'danger',
        confirmText: '删除',
        confirmClass: 'btn-danger'
    });
}

// 显示提示弹窗 (只有确认按钮)
function showAlert(options) {
    return new Promise((resolve) => {
        initModalStyles();

        const {
            title = '提示',
            message = '',
            confirmText = '确定',
            type = 'info'
        } = typeof options === 'string' ? { message: options } : options;

        const icons = {
            warning: '⚠️',
            danger: '❌',
            info: 'ℹ️',
            success: '✅'
        };

        const overlay = document.createElement('div');
        overlay.className = 'confirm-modal-overlay';
        overlay.innerHTML = `
            <div class="confirm-modal">
                <div class="confirm-modal-icon ${type}">${icons[type]}</div>
                <div class="confirm-modal-title">${title}</div>
                <div class="confirm-modal-message">${message}</div>
                <div class="confirm-modal-actions">
                    <button class="btn btn-primary" data-action="confirm">${confirmText}</button>
                </div>
            </div>
        `;

        document.body.appendChild(overlay);

        requestAnimationFrame(() => {
            overlay.classList.add('active');
        });

        const handleClick = () => {
            overlay.classList.remove('active');
            setTimeout(() => {
                overlay.remove();
                resolve(true);
            }, 300);
        };

        overlay.querySelector('[data-action="confirm"]').addEventListener('click', handleClick);

        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                handleClick();
            }
        });
    });
}

// 兼容旧代码：confirmDelete 函数（异步版本）
// 注意：此函数返回 Promise，需要用 await 调用
async function confirmDelete(message) {
    return await showDeleteConfirm(message);
}
