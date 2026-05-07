<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户管理 - 网上蛋糕城</title>
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
        .filter-bar input { padding: 10px 15px; border: 1px solid #ddd; border-radius: 8px; }
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
                <li><a href="${pageContext.request.contextPath}/admin/category/list">📁 分类管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/order/list">📦 订单管理</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/user/list" class="active">👥 用户管理</a></li>
                <li><a href="${pageContext.request.contextPath}/">🏠 返回前台</a></li>
            </ul>
        </aside>

        <main class="admin-content">
            <div class="admin-header">
                <h1 class="admin-title">用户管理</h1>
            </div>

            <div class="filter-bar">
                <input type="text" id="filterKeyword" placeholder="搜索手机号/用户名" onkeyup="loadUsers()">
            </div>

            <div class="data-table">
                <table>
                    <thead><tr><th>ID</th><th>手机号</th><th>用户名</th><th>角色</th><th>注册时间</th><th>操作</th></tr></thead>
                    <tbody id="userList"><tr><td colspan="6" style="text-align:center;padding:30px;">加载中...</td></tr></tbody>
                </table>
            </div>
        </main>
    </div>

    <div id="userModal" class="modal">
        <div class="modal-content">
            <div class="modal-header"><h3 id="modalTitle">编辑用户</h3><button class="modal-close" onclick="closeModal()">&times;</button></div>
            <form id="userForm">
                <input type="hidden" id="userId">
                <div class="form-group"><label>手机号</label><input type="text" id="userPhone" readonly disabled></div>
                <div class="form-group"><label>用户名</label><input type="text" id="userName" required></div>
                <div class="form-group"><label>角色</label>
                    <select id="userRole">
                        <option value="user">普通用户</option>
                        <option value="admin">管理员</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary" style="width:100%;">保存</button>
            </form>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/common.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';

        async function loadUsers() {
            try {
                const keyword = document.getElementById('filterKeyword').value;
                const params = keyword ? { keyword: keyword } : {};

                const response = await apiGet(contextPath + '/api/admin/user', params);
                // API返回格式: { data: { list: [...], total } }
                const result = response.data || {};
                const users = result.list || [];
                renderUsers(users);
            } catch (error) {
                document.getElementById('userList').innerHTML = '<tr><td colspan="5" style="text-align:center;">加载失败</td></tr>';
            }
        }

        function renderUsers(users) {
            if (users.length === 0) {
                document.getElementById('userList').innerHTML = '<tr><td colspan="6" style="text-align:center;padding:30px;">暂无数据</td></tr>';
                return;
            }
            let html = '';
            users.forEach(user => {
                html += '<tr>' +
                    '<td>' + user.id + '</td>' +
                    '<td>' + user.phone + '</td>' +
                    '<td>' + user.username + '</td>' +
                    '<td><span class="badge badge-' + (user.role === 'admin' ? 'primary' : 'secondary') + '">' + (user.role === 'admin' ? '管理员' : '普通用户') + '</span></td>' +
                    '<td>' + user.createdAt + '</td>' +
                    '<td class="action-btns">' +
                        '<button class="btn btn-secondary" onclick="editUser(' + user.id + ')">编辑</button>' +
                        (user.role !== 'admin' ? '<button class="btn btn-danger" onclick="deleteUser(' + user.id + ')">删除</button>' : '') +
                    '</td>' +
                '</tr>';
            });
            document.getElementById('userList').innerHTML = html;
        }

        async function editUser(id) {
            try {
                const response = await apiGet(contextPath + '/api/admin/user/' + id);
                const user = response.data;
                document.getElementById('modalTitle').textContent = '编辑用户';
                document.getElementById('userId').value = user.id;
                document.getElementById('userPhone').value = user.phone;
                document.getElementById('userName').value = user.username;
                document.getElementById('userRole').value = user.role;
                document.getElementById('userModal').classList.add('active');
            } catch (error) { console.error('获取用户失败:', error); }
        }

        function closeModal() { document.getElementById('userModal').classList.remove('active'); }

        document.getElementById('userForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            const id = document.getElementById('userId').value;
            const data = {
                username: document.getElementById('userName').value.trim(),
                role: document.getElementById('userRole').value
            };

            try {
                await apiPut(contextPath + '/api/admin/user/' + id, data);
                showToast('修改成功', 'success');
                closeModal();
                loadUsers();
            } catch (error) { console.error('操作失败:', error); }
        });

        async function deleteUser(id) {
            const confirmed = await showDeleteConfirm('确定要删除这个用户吗？删除后无法恢复。');
            if (!confirmed) return;
            try {
                await apiDelete(contextPath + '/api/admin/user/' + id);
                showToast('删除成功', 'success');
                loadUsers();
            } catch (error) { console.error('删除失败:', error); }
        }

        loadUsers();
    </script>
</body>
</html>
