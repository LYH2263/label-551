<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>分类管理 - 网上蛋糕城</title>
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
        .data-table { background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .action-btns { display: flex; gap: 8px; }
        .action-btns button { padding: 6px 12px; font-size: 12px; }
        .modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; align-items: center; justify-content: center; }
        .modal.active { display: flex; }
        .modal-content { background: white; border-radius: 20px; padding: 40px; width: 90%; max-width: 500px; }
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
                <li><a href="${pageContext.request.contextPath}/admin/cake/list">🍰 蛋糕管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/category/list" class="active">📁 分类管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/order/list">📦 订单管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/user/list">👥 用户管理</a></li>
                <li><a href="${pageContext.request.contextPath}/">🏠 返回前台</a></li>
            </ul>
        </aside>

        <main class="admin-content">
            <div class="admin-header">
                <h1 class="admin-title">分类管理</h1>
                <button class="btn btn-primary" onclick="openAddModal()">+ 新增分类</button>
            </div>

            <div class="data-table">
                <table>
                    <thead><tr><th>ID</th><th>分类名称</th><th>描述</th><th>排序</th><th>操作</th></tr></thead>
                    <tbody id="categoryList"><tr><td colspan="5" style="text-align:center;padding:30px;">加载中...</td></tr></tbody>
                </table>
            </div>
        </main>
    </div>

    <div id="categoryModal" class="modal">
        <div class="modal-content">
            <div class="modal-header"><h3 id="modalTitle">新增分类</h3><button class="modal-close" onclick="closeModal()">&times;</button></div>
            <form id="categoryForm">
                <input type="hidden" id="categoryId">
                <div class="form-group"><label>分类名称</label><input type="text" id="categoryName" required></div>
                <div class="form-group"><label>描述</label><textarea id="categoryDescription" rows="3"></textarea></div>
                <div class="form-group"><label>排序</label><input type="number" id="categorySortOrder" value="0"></div>
                <button type="submit" class="btn btn-primary" style="width:100%;">保存</button>
            </form>
        </div>
    </div>

    <div id="deleteOptionModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>删除分类确认</h3>
                <button class="modal-close" onclick="closeDeleteModal()">&times;</button>
            </div>
            <input type="hidden" id="deleteCategoryId">
            <div style="text-align:center;margin-bottom:30px;">
                <div style="width:80px;height:80px;background:linear-gradient(135deg,#fff3cd,#ffc107);border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto 20px;font-size:40px;">⚠️</div>
                <p style="font-size:18px;color:#333;margin-bottom:10px;">该分类下有 <strong id="deleteProductCount" style="color:var(--primary);">0</strong> 个商品</p>
                <p style="color:#666;">请选择删除分类后商品的处理方式：</p>
            </div>
            <div style="display:flex;flex-direction:column;gap:15px;">
                <button class="btn btn-secondary" onclick="confirmDeleteCategory('move')" style="padding:15px;display:flex;align-items:center;gap:15px;">
                    <span style="font-size:24px;">📦</span>
                    <div style="text-align:left;">
                        <div style="font-weight:600;">移动到未分类</div>
                        <div style="font-size:12px;color:#666;">保留商品，将其移至"未分类"</div>
                    </div>
                </button>
                <button class="btn btn-danger" onclick="confirmDeleteCategory('delete')" style="padding:15px;display:flex;align-items:center;gap:15px;">
                    <span style="font-size:24px;">🗑️</span>
                    <div style="text-align:left;">
                        <div style="font-weight:600;">连同商品一起删除</div>
                        <div style="font-size:12px;color:rgba(255,255,255,0.8);">删除分类及其下所有商品（不可恢复）</div>
                    </div>
                </button>
                <button class="btn" onclick="closeDeleteModal()" style="padding:12px;background:#f5f5f5;color:#666;">取消</button>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';

        async function loadCategories() {
            try {
                const response = await apiGet(contextPath + '/api/admin/category');
                const categories = response.data || [];
                renderCategories(categories);
            } catch (error) {
                document.getElementById('categoryList').innerHTML = '<tr><td colspan="5" style="text-align:center;">加载失败</td></tr>';
            }
        }

        function renderCategories(categories) {
            if (categories.length === 0) {
                document.getElementById('categoryList').innerHTML = '<tr><td colspan="5" style="text-align:center;padding:30px;">暂无数据</td></tr>';
                return;
            }
            let html = '';
            categories.forEach(cat => {
                html += '<tr>' +
                    '<td>' + cat.id + '</td>' +
                    '<td>' + cat.name + '</td>' +
                    '<td>' + (cat.description || '-') + '</td>' +
                    '<td>' + cat.sortOrder + '</td>' +
                    '<td class="action-btns">' +
                        '<button class="btn btn-secondary" onclick="editCategory(' + cat.id + ')">编辑</button>' +
                        '<button class="btn btn-danger" onclick="deleteCategory(' + cat.id + ')">删除</button>' +
                    '</td>' +
                '</tr>';
            });
            document.getElementById('categoryList').innerHTML = html;
        }

        function openAddModal() {
            document.getElementById('modalTitle').textContent = '新增分类';
            document.getElementById('categoryForm').reset();
            document.getElementById('categoryId').value = '';
            document.getElementById('categoryModal').classList.add('active');
        }

        async function editCategory(id) {
            try {
                const response = await apiGet(contextPath + '/api/admin/category/' + id);
                const cat = response.data;
                document.getElementById('modalTitle').textContent = '编辑分类';
                document.getElementById('categoryId').value = cat.id;
                document.getElementById('categoryName').value = cat.name;
                document.getElementById('categoryDescription').value = cat.description || '';
                document.getElementById('categorySortOrder').value = cat.sortOrder || 0;
                document.getElementById('categoryModal').classList.add('active');
            } catch (error) { console.error('获取分类失败:', error); }
        }

        function closeModal() { document.getElementById('categoryModal').classList.remove('active'); }

        document.getElementById('categoryForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            const id = document.getElementById('categoryId').value;
            const data = {
                name: document.getElementById('categoryName').value.trim(),
                description: document.getElementById('categoryDescription').value.trim(),
                sortOrder: parseInt(document.getElementById('categorySortOrder').value) || 0
            };

            try {
                if (id) {
                    await apiPut(contextPath + '/api/admin/category/' + id, data);
                    showToast('修改成功', 'success');
                } else {
                    await apiPost(contextPath + '/api/admin/category', data);
                    showToast('添加成功', 'success');
                }
                closeModal();
                loadCategories();
            } catch (error) { console.error('操作失败:', error); }
        });

        async function deleteCategory(id) {
            // 先检查分类下是否有商品
            try {
                const response = await apiGet(contextPath + '/api/admin/category/' + id + '/check');
                const hasProducts = response.data && response.data.hasProducts;
                const productCount = response.data ? response.data.productCount : 0;

                if (hasProducts) {
                    // 有关联商品，显示处理选项弹窗
                    showCategoryDeleteOptions(id, productCount);
                } else {
                    const confirmed = await showDeleteConfirm('确定要删除这个分类吗？');
                    if (!confirmed) return;
                    await apiDelete(contextPath + '/api/admin/category/' + id);
                    showToast('删除成功', 'success');
                    loadCategories();
                }
            } catch (error) {
                console.error('删除失败:', error);
            }
        }

        function showCategoryDeleteOptions(categoryId, productCount) {
            document.getElementById('deleteProductCount').textContent = productCount;
            document.getElementById('deleteCategoryId').value = categoryId;
            document.getElementById('deleteOptionModal').classList.add('active');
        }

        function closeDeleteModal() {
            document.getElementById('deleteOptionModal').classList.remove('active');
        }

        async function confirmDeleteCategory(action) {
            const categoryId = document.getElementById('deleteCategoryId').value;
            closeDeleteModal();

            try {
                if (action === 'move') {
                    // 移动商品到未分类，然后删除分类
                    await apiDelete(contextPath + '/api/admin/category/' + categoryId + '?force=true');
                    showToast('分类已删除，商品已移至未分类', 'success');
                } else if (action === 'delete') {
                    // 删除分类及其所有商品
                    await apiDelete(contextPath + '/api/admin/category/' + categoryId + '?force=true&deleteProducts=true');
                    showToast('分类及商品已删除', 'success');
                }
                loadCategories();
            } catch (error) {
                console.error('删除失败:', error);
                showToast('删除失败，请重试', 'error');
            }
        }

        loadCategories();
    </script>
</body>
</html>
