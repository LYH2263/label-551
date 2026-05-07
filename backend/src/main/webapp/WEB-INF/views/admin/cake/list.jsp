<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>蛋糕管理 - 网上蛋糕城</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <style>
        .admin-layout { display: flex; min-height: 100vh; }
        .admin-sidebar { width: 250px; background: linear-gradient(180deg, #2c3e50, #1a252f); padding: 20px 0; position: fixed; height: 100vh; }
        .admin-logo { color: white; font-size: 24px; font-weight: 700; padding: 20px 30px; border-bottom: 1px solid rgba(255,255,255,0.1); margin-bottom: 20px; }
        .admin-menu { list-style: none; }
        .admin-menu a { display: flex; align-items: center; gap: 12px; padding: 15px 30px; color: rgba(255,255,255,0.7); text-decoration: none; transition: all 0.3s; }
        .admin-menu a:hover { background: rgba(255,255,255,0.1); color: white; }
        .admin-menu a.active { background: var(--primary); color: white; }
        .admin-content { flex: 1; margin-left: 250px; padding: 30px; background: #f5f6fa; }
        .admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
        .admin-title { font-size: 28px; font-weight: 700; }
        .filter-bar { display: flex; gap: 15px; margin-bottom: 20px; }
        .filter-bar input, .filter-bar select { padding: 10px 15px; border: 1px solid #ddd; border-radius: 8px; }
        .data-table { background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .cake-img { width: 60px; height: 60px; border-radius: 8px; object-fit: cover; }
        .status-on { color: #27ae60; }
        .status-off { color: #e74c3c; }
        .action-btns { display: flex; gap: 8px; }
        .action-btns button { padding: 6px 12px; font-size: 12px; }
        .modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center; }
        .modal.active { display: flex; }
        .modal-content { background: white; border-radius: 20px; padding: 40px; width: 90%; max-width: 600px; max-height: 90vh; overflow-y: auto; }
        .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
        .modal-close { background: none; border: none; font-size: 24px; cursor: pointer; }
    </style>
</head>
<body>
    <div class="admin-layout">
        <aside class="admin-sidebar">
            <div class="admin-logo">🎂 蛋糕城后台</div>
            <ul class="admin-menu">
                <li><a href="${pageContext.request.contextPath}/admin">📊 控制台</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/cake/list" class="active">🍰 蛋糕管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/category/list">📁 分类管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/order/list">📦 订单管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/user/list">👥 用户管理</a></li>
                <li><a href="${pageContext.request.contextPath}/">🏠 返回前台</a></li>
            </ul>
        </aside>

        <main class="admin-content">
            <div class="admin-header">
                <h1 class="admin-title">蛋糕管理</h1>
                <button class="btn btn-primary" onclick="openAddModal()">+ 新增蛋糕</button>
            </div>

            <div class="filter-bar">
                <select id="filterCategory" onchange="loadCakes()">
                    <option value="">全部分类</option>
                </select>
                <input type="text" id="filterKeyword" placeholder="搜索蛋糕名称" onkeyup="loadCakes()">
                <select id="filterStatus" onchange="loadCakes()">
                    <option value="">全部状态</option>
                    <option value="on">已上架</option>
                    <option value="off">已下架</option>
                </select>
            </div>

            <div class="data-table">
                <table>
                    <thead><tr><th>图片</th><th>名称</th><th>分类</th><th>价格</th><th>库存</th><th>状态</th><th>操作</th></tr></thead>
                    <tbody id="cakeList"><tr><td colspan="7" style="text-align:center;padding:30px;">加载中...</td></tr></tbody>
                </table>
            </div>
        </main>
    </div>

    <div id="cakeModal" class="modal">
        <div class="modal-content">
            <div class="modal-header"><h3 id="modalTitle">新增蛋糕</h3><button class="modal-close" onclick="closeModal()">&times;</button></div>
            <form id="cakeForm" enctype="multipart/form-data">
                <input type="hidden" id="cakeId">
                <div class="form-group"><label>蛋糕名称</label><input type="text" id="cakeName" required></div>
                <div class="form-group"><label>分类</label><select id="cakeCategory" required></select></div>
                <div class="form-group"><label>价格</label><input type="number" id="cakePrice" step="0.01" required></div>
                <div class="form-group"><label>原价</label><input type="number" id="cakeOriginalPrice" step="0.01"></div>
                <div class="form-group"><label>库存</label><input type="number" id="cakeStock" required></div>
                <div class="form-group"><label>规格尺寸</label><input type="text" id="cakeSize" placeholder="如：8寸"></div>
                <div class="form-group"><label>口味</label><input type="text" id="cakeFlavor" placeholder="如：草莓味"></div>
                <div class="form-group"><label>图片</label><input type="file" id="cakeImage" accept="image/*"></div>
                <div class="form-group"><label>描述</label><textarea id="cakeDescription" rows="3"></textarea></div>
                <button type="submit" class="btn btn-primary" style="width:100%;">保存</button>
            </form>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        let categories = [];

        async function loadCategories() {
            try {
                const response = await apiGet(contextPath + '/api/category');
                categories = response.data || [];
                let html = '<option value="">全部分类</option>';
                let formHtml = '';
                categories.forEach(c => {
                    html += '<option value="' + c.id + '">' + c.name + '</option>';
                    formHtml += '<option value="' + c.id + '">' + c.name + '</option>';
                });
                document.getElementById('filterCategory').innerHTML = html;
                document.getElementById('cakeCategory').innerHTML = formHtml;
            } catch (error) { console.error('加载分类失败:', error); }
        }

        async function loadCakes() {
            try {
                const params = {};
                const categoryId = document.getElementById('filterCategory').value;
                const keyword = document.getElementById('filterKeyword').value;
                const status = document.getElementById('filterStatus').value;
                if (categoryId) params.categoryId = categoryId;
                if (keyword) params.keyword = keyword;
                if (status) params.status = status;

                const response = await apiGet(contextPath + '/api/admin/cake', params);
                // API返回分页格式: { data: { list: [...], total } }
                const result = response.data || {};
                const cakes = result.list || [];
                renderCakes(cakes);
            } catch (error) {
                document.getElementById('cakeList').innerHTML = '<tr><td colspan="7" style="text-align:center;">加载失败</td></tr>';
            }
        }

        function renderCakes(cakes) {
            if (cakes.length === 0) {
                document.getElementById('cakeList').innerHTML = '<tr><td colspan="7" style="text-align:center;padding:30px;">暂无数据</td></tr>';
                return;
            }
            let html = '';
            cakes.forEach(cake => {
                const cakeImageUrl = resolveCakeImageUrl(cake.image, contextPath);
                html += '<tr>' +
                    '<td><img src="' + cakeImageUrl + '" class="cake-img" onerror="this.src=\'' + contextPath + '/images/no-image.svg\'"></td>' +
                    '<td>' + cake.name + '</td>' +
                    '<td>' + (cake.categoryName || '-') + '</td>' +
                    '<td>¥' + cake.price + '</td>' +
                    '<td>' + cake.stock + '</td>' +
                    '<td class="status-' + cake.status + '">' + (cake.status === 'on' ? '已上架' : '已下架') + '</td>' +
                    '<td class="action-btns">' +
                        '<button class="btn btn-secondary" onclick="editCake(' + cake.id + ')">编辑</button>' +
                        '<button class="btn ' + (cake.status === 'on' ? 'btn-warning' : 'btn-success') + '" onclick="toggleStatus(' + cake.id + ', \'' + cake.status + '\')">' + (cake.status === 'on' ? '下架' : '上架') + '</button>' +
                        '<button class="btn btn-danger" onclick="deleteCake(' + cake.id + ', \'' + cake.name.replace(/'/g, "\\'") + '\')">删除</button>' +
                    '</td>' +
                '</tr>';
            });
            document.getElementById('cakeList').innerHTML = html;
        }

        function openAddModal() {
            document.getElementById('modalTitle').textContent = '新增蛋糕';
            document.getElementById('cakeForm').reset();
            document.getElementById('cakeId').value = '';
            document.getElementById('cakeModal').classList.add('active');
        }

        async function editCake(id) {
            try {
                const response = await apiGet(contextPath + '/api/admin/cake/' + id);
                const cake = response.data;
                document.getElementById('modalTitle').textContent = '编辑蛋糕';
                document.getElementById('cakeId').value = cake.id;
                document.getElementById('cakeName').value = cake.name;
                document.getElementById('cakeCategory').value = cake.categoryId;
                document.getElementById('cakePrice').value = cake.price;
                document.getElementById('cakeOriginalPrice').value = cake.originalPrice || '';
                document.getElementById('cakeStock').value = cake.stock;
                document.getElementById('cakeSize').value = cake.size || '';
                document.getElementById('cakeFlavor').value = cake.flavor || '';
                document.getElementById('cakeDescription').value = cake.description || '';
                document.getElementById('cakeModal').classList.add('active');
            } catch (error) { console.error('获取蛋糕失败:', error); }
        }

        function closeModal() { document.getElementById('cakeModal').classList.remove('active'); }

        document.getElementById('cakeForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            const id = document.getElementById('cakeId').value;
            const imageFile = document.getElementById('cakeImage').files[0];

            // 如果有图片文件，先上传图片
            let imagePath = null;
            if (imageFile) {
                try {
                    const formData = new FormData();
                    formData.append('file', imageFile);
                    const uploadResp = await fetch(contextPath + '/api/admin/cake', {
                        method: 'POST',
                        body: formData,
                        credentials: 'include'
                    });
                    const uploadResult = await uploadResp.json();
                    if (uploadResult.success) {
                        // 上传成功，获取文件名
                        imagePath = uploadResult.data;
                        if (imagePath && imagePath.startsWith('/uploads/cakes/')) {
                            imagePath = imagePath.substring('/uploads/cakes/'.length);
                        }
                    } else {
                        showToast(uploadResult.message || '图片上传失败', 'error');
                        return;
                    }
                } catch (error) {
                    showToast('图片上传失败', 'error');
                    return;
                }
            }

            // 提交蛋糕数据
            const data = {
                name: document.getElementById('cakeName').value.trim(),
                categoryId: parseInt(document.getElementById('cakeCategory').value),
                price: document.getElementById('cakePrice').value,
                originalPrice: document.getElementById('cakeOriginalPrice').value || null,
                stock: parseInt(document.getElementById('cakeStock').value),
                size: document.getElementById('cakeSize').value.trim(),
                flavor: document.getElementById('cakeFlavor').value.trim(),
                description: document.getElementById('cakeDescription').value.trim(),
                status: 'on'
            };
            if (imagePath) {
                data.image = imagePath;
            }

            try {
                if (id) {
                    await apiPut(contextPath + '/api/admin/cake/' + id, data);
                    showToast('修改成功', 'success');
                } else {
                    await apiPost(contextPath + '/api/admin/cake', data);
                    showToast('添加成功', 'success');
                }
                closeModal();
                loadCakes();
            } catch (error) {
                console.error('操作失败:', error);
            }
        });

        async function toggleStatus(id, currentStatus) {
            const newStatus = currentStatus === 'on' ? 'off' : 'on';
            try {
                await apiPut(contextPath + '/api/admin/cake/' + id + '/status', { status: newStatus });
                showToast('操作成功', 'success');
                loadCakes();
            } catch (error) { console.error('操作失败:', error); }
        }

        async function deleteCake(id, name) {
            const confirmed = await showDeleteConfirm('确定要删除蛋糕"' + name + '"吗？删除后无法恢复。');
            if (!confirmed) return;
            try {
                await apiDelete(contextPath + '/api/admin/cake/' + id);
                showToast('删除成功', 'success');
                loadCakes();
            } catch (error) {
                console.error('删除失败:', error);
                showToast('删除失败，请重试', 'error');
            }
        }

        loadCategories();
        loadCakes();
    </script>
</body>
</html>
