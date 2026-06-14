/**
 * 电商订单管理系统 - 主应用逻辑
 * 包含：登录/注册、订单列表查询、订单CRUD、统计概览
 */
const App = (() => {
  // ========== 工具函数 ==========
  function $(sel) { return document.querySelector(sel); }
  function $$(sel) { return document.querySelectorAll(sel); }
  function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }

  // Toast 提示
  function showToast(msg, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = msg;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
  }

  // 格式化金额
  function formatMoney(amount) {
    return '¥' + (amount || 0).toFixed(2);
  }

  // 格式化日期
  function formatDate(dateStr) {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString('zh-CN');
  }

  // 订单状态映射
  const STATUS_MAP = {
    0: '待支付', 1: '已支付', 2: '已发货', 3: '已收货', 4: '已取消'
  };

  // ========== 页面状态 ==========
  let currentPage = 1;
  let totalPages = 1;
  let editingOrderId = null;

  // ========== 页面切换 ==========
  function showPage(pageId) {
    $$('.page').forEach(p => p.classList.add('hidden'));
    const target = $(`#page-${pageId}`);
    if (target) target.classList.remove('hidden');
  }

  // ========== 认证 ==========
  async function handleLogin(e) {
    e.preventDefault();
    const username = $('#login-username').value.trim();
    const password = $('#login-password').value.trim();
    if (!username || !password) {
      showToast('请输入用户名和密码', 'error');
      return;
    }
    try {
      const res = await API.login(username, password);
      API.setToken(res.data.token);
      API.setUser(res.data.user);
      showToast('登录成功');
      showMainApp();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  async function handleRegister(e) {
    e.preventDefault();
    const username = $('#reg-username').value.trim();
    const password = $('#reg-password').value.trim();
    const phone = $('#reg-phone').value.trim();
    const email = $('#reg-email').value.trim();
    if (!username || !password) {
      showToast('请填写用户名和密码', 'error');
      return;
    }
    if (password.length < 6) {
      showToast('密码长度至少6位', 'error');
      return;
    }
    try {
      await API.register(username, password, phone, email);
      showToast('注册成功，正在登录...');
      const loginRes = await API.login(username, password);
      API.setToken(loginRes.data.token);
      API.setUser(loginRes.data.user);
      showMainApp();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  function handleLogout() {
    API.clearToken();
    $('#app-main').classList.add('hidden');
    $('#app-auth').classList.remove('hidden');
    showToast('已退出登录');
  }

  function showMainApp() {
    $('#app-auth').classList.add('hidden');
    $('#app-main').classList.remove('hidden');
    const user = API.getUser();
    if (user) {
      $('#header-username').textContent = user.username;
    }
    loadOrderList();
    loadStats();
  }

  function toggleAuthForm(form) {
    if (form === 'login') {
      $('#form-login').classList.remove('hidden');
      $('#form-register').classList.add('hidden');
    } else {
      $('#form-login').classList.add('hidden');
      $('#form-register').classList.remove('hidden');
    }
  }

  // ========== 订单列表 ==========
  async function loadOrderList(page = 1) {
    currentPage = page;
    const params = {
      pageNum: page,
      pageSize: 10,
    };

    const userId = $('#filter-userId').value.trim();
    const status = $('#filter-status').value;
    const keyword = $('#filter-keyword').value.trim();
    const startDate = $('#filter-startDate').value;
    const endDate = $('#filter-endDate').value;

    if (userId) params.userId = userId;
    if (status) params.status = status;
    if (keyword) params.keyword = keyword;
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;

    try {
      const res = await API.getOrders(params);
      const pageData = res.data || {};
      totalPages = pageData.pages || 1;
      renderOrderTable(pageData.list || []);
      renderPagination(pageData.total || 0);
    } catch (err) {
      showToast('加载订单失败: ' + err.message, 'error');
    }
  }

  function renderOrderTable(orders) {
    const tbody = $('#order-tbody');
    if (!orders || orders.length === 0) {
      tbody.innerHTML = '<tr><td colspan="9" style="text-align:center;padding:40px;color:#999;">暂无订单数据</td></tr>';
      return;
    }
    tbody.innerHTML = orders.map(o => `
      <tr>
        <td><strong>${escapeHtml(o.orderNo)}</strong></td>
        <td>${o.userId}</td>
        <td>${formatMoney(o.totalAmount)}</td>
        <td><span class="status-tag status-${o.status}">${STATUS_MAP[o.status] || '未知'}</span></td>
        <td>${escapeHtml(o.receiverName || '-')}</td>
        <td>${escapeHtml(o.receiverPhone || '-')}</td>
        <td>${escapeHtml(o.shippingAddress || '-')}</td>
        <td>${formatDate(o.createdAt)}</td>
        <td class="actions">
          <button class="btn btn-primary" style="height:30px;padding:0 10px;font-size:12px;"
                  onclick="App.viewOrder(${o.id})">详情</button>
          <button class="btn btn-warning" style="height:30px;padding:0 10px;font-size:12px;"
                  onclick="App.editOrder(${o.id})">编辑</button>
          <button class="btn btn-danger" style="height:30px;padding:0 10px;font-size:12px;"
                  onclick="App.confirmDelete(${o.id}, '${escapeHtml(o.orderNo)}')">删除</button>
        </td>
      </tr>
    `).join('');
  }

  function renderPagination(total) {
    const container = $('#pagination-info');
    container.innerHTML = `<span class="total">共 ${total} 条记录，第 ${currentPage}/${totalPages} 页</span>`;

    const pagesDiv = $('#pagination-pages');
    let html = `<button ${currentPage <= 1 ? 'disabled' : ''} onclick="App.goToPage(${currentPage - 1})">‹</button>`;
    for (let i = 1; i <= totalPages; i++) {
      if (totalPages > 7 && i > 3 && i < totalPages - 2 && Math.abs(i - currentPage) > 1) {
        if (i === 4) html += '<button disabled>…</button>';
        continue;
      }
      html += `<button class="${i === currentPage ? 'active' : ''}" onclick="App.goToPage(${i})">${i}</button>`;
    }
    html += `<button ${currentPage >= totalPages ? 'disabled' : ''} onclick="App.goToPage(${currentPage + 1})">›</button>`;
    pagesDiv.innerHTML = html;
  }

  function goToPage(page) {
    if (page >= 1 && page <= totalPages) loadOrderList(page);
  }

  function searchOrders() {
    loadOrderList(1);
  }

  function resetFilters() {
    $('#filter-userId').value = '';
    $('#filter-status').value = '';
    $('#filter-keyword').value = '';
    $('#filter-startDate').value = '';
    $('#filter-endDate').value = '';
    loadOrderList(1);
  }

  // ========== 订单详情 ==========
  async function viewOrder(id) {
    try {
      const res = await API.getOrderById(id);
      const o = res.data;
      // 加载订单明细
      let itemsHtml = '';
      try {
        const itemsRes = await API.getOrderItems(id);
        if (itemsRes.data && itemsRes.data.length > 0) {
          itemsHtml = itemsRes.data.map(item => `
            <tr>
              <td>${item.productId}</td>
              <td>${escapeHtml(item.productName)}</td>
              <td>${escapeHtml(item.skuCode || '-')}</td>
              <td>${formatMoney(item.price)}</td>
              <td>${item.quantity}</td>
              <td>${formatMoney(item.subTotal)}</td>
            </tr>
          `).join('');
        }
      } catch (e) { /* 忽略明细加载错误 */ }

      const modal = $('#modal-view');
      modal.querySelector('.modal-body').innerHTML = `
        <div style="margin-bottom:16px">
          <strong>订单编号：</strong>${escapeHtml(o.orderNo)}<br>
          <strong>订单状态：</strong><span class="status-tag status-${o.status}">${STATUS_MAP[o.status]}</span><br>
          <strong>下单时间：</strong>${formatDate(o.createdAt)}<br>
          <strong>更新时间：</strong>${formatDate(o.updatedAt)}
        </div>
        <div style="margin-bottom:16px">
          <strong>收货人：</strong>${escapeHtml(o.receiverName || '-')}<br>
          <strong>联系电话：</strong>${escapeHtml(o.receiverPhone || '-')}<br>
          <strong>收货地址：</strong>${escapeHtml(o.shippingAddress || '-')}<br>
          <strong>备注：</strong>${escapeHtml(o.remark || '-')}
        </div>
        <div>
          <strong>订单金额：</strong><span style="font-size:18px;color:#F56C6C;">${formatMoney(o.totalAmount)}</span>
        </div>
        ${itemsHtml ? `
        <div style="margin-top:16px">
          <strong>商品明细：</strong>
          <table style="margin-top:8px;border:1px solid #eee">
            <thead><tr><th>商品ID</th><th>商品名称</th><th>SKU</th><th>单价</th><th>数量</th><th>小计</th></tr></thead>
            <tbody>${itemsHtml}</tbody>
          </table>
        </div>` : ''}
      `;
      modal.classList.remove('hidden');
    } catch (err) {
      showToast('加载订单详情失败: ' + err.message, 'error');
    }
  }

  // ========== 订单编辑 ==========
  async function editOrder(id) {
    try {
      const res = await API.getOrderById(id);
      const o = res.data;
      editingOrderId = id;
      $('#edit-orderNo').value = o.orderNo || '';
      $('#edit-userId').value = o.userId || '';
      $('#edit-totalAmount').value = o.totalAmount || '';
      $('#edit-status').value = o.status ?? 0;
      $('#edit-receiverName').value = o.receiverName || '';
      $('#edit-receiverPhone').value = o.receiverPhone || '';
      $('#edit-shippingAddress').value = o.shippingAddress || '';
      $('#edit-remark').value = o.remark || '';
      $('#modal-edit-title').textContent = '编辑订单';
      $('#modal-edit').classList.remove('hidden');
    } catch (err) {
      showToast('加载订单失败: ' + err.message, 'error');
    }
  }

  function showCreateModal() {
    editingOrderId = null;
    $('#edit-orderNo').value = '';
    $('#edit-userId').value = '';
    $('#edit-totalAmount').value = '';
    $('#edit-status').value = 0;
    $('#edit-receiverName').value = '';
    $('#edit-receiverPhone').value = '';
    $('#edit-shippingAddress').value = '';
    $('#edit-remark').value = '';
    $('#modal-edit-title').textContent = '创建订单';
    $('#modal-edit').classList.remove('hidden');
  }

  async function saveOrder() {
    const order = {
      orderNo: $('#edit-orderNo').value.trim() || ('ORD' + Date.now()),
      userId: parseInt($('#edit-userId').value) || 0,
      totalAmount: parseFloat($('#edit-totalAmount').value) || 0,
      status: parseInt($('#edit-status').value) || 0,
      receiverName: $('#edit-receiverName').value.trim(),
      receiverPhone: $('#edit-receiverPhone').value.trim(),
      shippingAddress: $('#edit-shippingAddress').value.trim(),
      remark: $('#edit-remark').value.trim(),
    };

    if (!order.userId) {
      showToast('请输入用户ID', 'error');
      return;
    }

    try {
      if (editingOrderId) {
        await API.updateOrder(editingOrderId, order);
        showToast('订单更新成功');
      } else {
        await API.createOrder(order);
        showToast('订单创建成功');
      }
      closeModal('modal-edit');
      loadOrderList(currentPage);
      loadStats();
    } catch (err) {
      showToast('保存失败: ' + err.message, 'error');
    }
  }

  function confirmDelete(id, orderNo) {
    if (confirm(`确定要删除订单 ${orderNo} 吗？`)) {
      API.deleteOrder(id).then(() => {
        showToast('删除成功');
        loadOrderList(currentPage);
        loadStats();
      }).catch(err => showToast('删除失败: ' + err.message, 'error'));
    }
  }

  // ========== 统计 ==========
  async function loadStats() {
    try {
      const res = await API.getOrderStats();
      const d = res.data || {};
      $('#stat-total').textContent = d.totalOrders || 0;
      $('#stat-pending').textContent = d.status_0 || 0;
      $('#stat-paid').textContent = d.status_1 || 0;
      $('#stat-shipped').textContent = d.status_2 || 0;
      $('#stat-received').textContent = d.status_3 || 0;
      $('#stat-today').textContent = d.todayOrders || 0;
    } catch (e) { /* 忽略统计错误 */ }
  }

  // ========== Modal 操作 ==========
  function closeModal(modalId) {
    $(`#${modalId}`).classList.add('hidden');
  }

  // ========== 初始化 ==========
  function init() {
    // 登录事件
    $('#form-login').addEventListener('submit', handleLogin);
    $('#form-register').addEventListener('submit', handleRegister);
    $('#btn-logout').addEventListener('click', handleLogout);

    // 筛选事件
    $('#btn-search').addEventListener('click', searchOrders);
    $('#btn-reset').addEventListener('click', resetFilters);
    $('#filter-keyword').addEventListener('keydown', e => {
      if (e.key === 'Enter') searchOrders();
    });

    // Modal 关闭
    $$('.modal-close, .modal-overlay').forEach(el => {
      el.addEventListener('click', function(e) {
        if (e.target === this || this.classList.contains('modal-close')) {
          const modal = this.closest('.modal-overlay');
          if (modal) modal.classList.add('hidden');
        }
      });
    });

    // 编辑保存
    $('#btn-save-order').addEventListener('click', saveOrder);

    // 检查已登录状态
    if (API.isLoggedIn()) {
      showMainApp();
    }
  }

  // ========== 公开方法 ==========
  return {
    init,
    goToPage,
    searchOrders,
    resetFilters,
    viewOrder,
    editOrder,
    showCreateModal,
    confirmDelete,
    closeModal: (id) => closeModal(id),
    toggleAuthForm,
  };
})();

document.addEventListener('DOMContentLoaded', () => App.init());
