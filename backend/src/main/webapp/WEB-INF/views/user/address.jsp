<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>收货地址 - 网上蛋糕城</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user.css">
    <style>
        .address-list { display: flex; flex-direction: column; gap: 20px; }
        .address-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border: 2px solid transparent; transition: all 0.3s; }
        .address-card:hover { border-color: var(--primary); }
        .address-card.default { border-color: var(--primary); background: #fff5f5; }
        .address-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
        .address-name { font-weight: 600; font-size: 18px; }
        .address-phone { color: var(--text-light); margin-left: 15px; }
        .address-detail { color: var(--text-light); line-height: 1.6; }
        .address-actions { margin-top: 15px; display: flex; gap: 10px; }
        .address-actions button { padding: 8px 16px; font-size: 13px; }
        .default-badge { background: var(--primary); color: white; padding: 4px 12px; border-radius: 20px; font-size: 12px; }
        .add-address-btn { margin-bottom: 30px; }
        .modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center; }
        .modal.active { display: flex; }
        .modal-content { background: white; border-radius: 20px; padding: 40px; width: 90%; max-width: 500px; max-height: 90vh; overflow-y: auto; }
        .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
        .modal-header h3 { font-size: 24px; }
        .modal-close { background: none; border: none; font-size: 24px; cursor: pointer; color: var(--text-light); }
    </style>
</head>
<body>
    <%@ include file="/WEB-INF/views/common/header.jsp" %>

    <div class="container">
        <h1 class="page-title">收货地址</h1>

        <div class="content-wrapper">
            <div class="sidebar">
                <ul class="sidebar-menu">
                    <li><a href="${pageContext.request.contextPath}/user/profile">个人信息</a></li>
                    <li><a href="${pageContext.request.contextPath}/user/address" class="active">收货地址</a></li>
                    <li><a href="${pageContext.request.contextPath}/order/list">我的订单</a></li>
                </ul>
            </div>

            <div class="main-content">
                <button class="btn btn-primary add-address-btn" onclick="openAddModal()">+ 新增收货地址</button>

                <div id="addressList" class="address-list">
                    <div class="card" style="text-align: center; padding: 60px;">
                        <div class="loading"></div>
                        <p style="margin-top: 20px; color: var(--text-light);">加载中...</p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 地址编辑弹窗 -->
    <div id="addressModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3 id="modalTitle">新增地址</h3>
                <button class="modal-close" onclick="closeModal()">&times;</button>
            </div>
            <form id="addressForm">
                <input type="hidden" id="addressId">
                <div class="form-group">
                    <label>收货人姓名</label>
                    <input type="text" id="receiverName" required placeholder="请输入收货人姓名">
                </div>
                <div class="form-group">
                    <label>联系电话</label>
                    <input type="tel" id="addressPhone" required placeholder="请输入联系电话">
                </div>
                <div class="form-group">
                    <label>省份</label>
                    <input type="text" id="province" required placeholder="请输入省份">
                </div>
                <div class="form-group">
                    <label>城市</label>
                    <input type="text" id="city" required placeholder="请输入城市">
                </div>
                <div class="form-group">
                    <label>区/县</label>
                    <input type="text" id="district" required placeholder="请输入区/县">
                </div>
                <div class="form-group">
                    <label>详细地址</label>
                    <textarea id="detail" required placeholder="请输入详细地址（街道、门牌号等）" rows="3"></textarea>
                </div>
                <div class="form-group">
                    <label style="display: flex; align-items: center; gap: 10px;">
                        <input type="checkbox" id="isDefault" style="width: auto;">
                        设为默认地址
                    </label>
                </div>
                <button type="submit" class="btn btn-primary" style="width: 100%;">保存</button>
            </form>
        </div>
    </div>

    <%@ include file="/WEB-INF/views/common/footer.jsp" %>

    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';

        async function loadAddresses() {
            try {
                const response = await apiGet(contextPath + '/api/address');
                const addresses = response.data || [];
                renderAddresses(addresses);
            } catch (error) {
                document.getElementById('addressList').innerHTML = '<div class="card" style="text-align:center;padding:60px;"><p>加载失败，请刷新重试</p></div>';
            }
        }

        function renderAddresses(addresses) {
            if (addresses.length === 0) {
                document.getElementById('addressList').innerHTML = '<div class="card" style="text-align:center;padding:60px;"><p style="color:var(--text-light);">暂无收货地址，请添加</p></div>';
                return;
            }

            let html = '';
            addresses.forEach(addr => {
                html += '<div class="address-card ' + (addr.isDefault ? 'default' : '') + '">' +
                    '<div class="address-header">' +
                        '<div><span class="address-name">' + addr.receiverName + '</span><span class="address-phone">' + addr.phone + '</span></div>' +
                        (addr.isDefault ? '<span class="default-badge">默认</span>' : '') +
                    '</div>' +
                    '<div class="address-detail">' + addr.province + ' ' + addr.city + ' ' + addr.district + ' ' + addr.detail + '</div>' +
                    '<div class="address-actions">' +
                        '<button class="btn btn-secondary" onclick="editAddress(' + addr.id + ')">编辑</button>' +
                        (!addr.isDefault ? '<button class="btn btn-secondary" onclick="setDefault(' + addr.id + ')">设为默认</button>' : '') +
                        '<button class="btn btn-danger" onclick="deleteAddress(' + addr.id + ')">删除</button>' +
                    '</div>' +
                '</div>';
            });
            document.getElementById('addressList').innerHTML = html;
        }

        function openAddModal() {
            document.getElementById('modalTitle').textContent = '新增地址';
            document.getElementById('addressForm').reset();
            document.getElementById('addressId').value = '';
            document.getElementById('addressModal').classList.add('active');
        }

        async function editAddress(id) {
            try {
                const response = await apiGet(contextPath + '/api/address/' + id);
                const addr = response.data;
                document.getElementById('modalTitle').textContent = '编辑地址';
                document.getElementById('addressId').value = addr.id;
                document.getElementById('receiverName').value = addr.receiverName;
                document.getElementById('addressPhone').value = addr.phone;
                document.getElementById('province').value = addr.province;
                document.getElementById('city').value = addr.city;
                document.getElementById('district').value = addr.district;
                document.getElementById('detail').value = addr.detail;
                document.getElementById('isDefault').checked = addr.isDefault;
                document.getElementById('addressModal').classList.add('active');
            } catch (error) {
                console.error('获取地址失败:', error);
            }
        }

        function closeModal() {
            document.getElementById('addressModal').classList.remove('active');
        }

        document.getElementById('addressForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const id = document.getElementById('addressId').value;
            const data = {
                receiverName: document.getElementById('receiverName').value.trim(),
                phone: document.getElementById('addressPhone').value.trim(),
                province: document.getElementById('province').value.trim(),
                city: document.getElementById('city').value.trim(),
                district: document.getElementById('district').value.trim(),
                detail: document.getElementById('detail').value.trim(),
                isDefault: document.getElementById('isDefault').checked
            };

            // 手机号校验
            if (!/^1[3-9]\d{9}$/.test(data.phone)) {
                showToast('请输入正确的手机号格式', 'error');
                return;
            }

            try {
                if (id) {
                    await apiPut(contextPath + '/api/address/' + id, data);
                    showToast('修改成功', 'success');
                } else {
                    await apiPost(contextPath + '/api/address', data);
                    showToast('添加成功', 'success');
                }
                closeModal();
                loadAddresses();
            } catch (error) {
                console.error('保存失败:', error);
            }
        });

        async function setDefault(id) {
            try {
                await apiPut(contextPath + '/api/address/' + id + '/default', {});
                showToast('设置成功', 'success');
                loadAddresses();
            } catch (error) {
                console.error('设置失败:', error);
            }
        }

        async function deleteAddress(id) {
            const confirmed = await showDeleteConfirm('确定要删除这个地址吗？');
            if (!confirmed) return;
            try {
                await apiDelete(contextPath + '/api/address/' + id);
                showToast('删除成功', 'success');
                loadAddresses();
            } catch (error) {
                console.error('删除失败:', error);
            }
        }

        loadAddresses();
    </script>
</body>
</html>
