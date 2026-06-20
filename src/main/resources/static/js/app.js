const App = (() => {
  const $ = (selector) => document.querySelector(selector);
  const $$ = (selector) => Array.from(document.querySelectorAll(selector));
  const money = (value) => `¥${Number(value || 0).toFixed(2)}`;
  const escapeHtml = (value) => {
    const div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  };

  const state = {
    productPage: 1,
    productPages: 1,
    productTotal: 0,
    pageSize: 8,
  };

  const statusMap = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已收货', 4: '已取消' };

  function toast(message, type = 'success') {
    const el = $('#toast');
    el.textContent = message;
    el.className = `toast ${type === 'error' ? 'error' : ''}`;
    setTimeout(() => el.classList.add('hidden'), 2600);
  }

  function bindEvents() {
    $('#login-form').addEventListener('submit', handleLogin);
    $('#register-form').addEventListener('submit', handleRegister);
    $('#show-register').addEventListener('click', () => toggleAuth('register'));
    $('#show-login').addEventListener('click', () => toggleAuth('login'));
    $('#logout-btn').addEventListener('click', logout);
    $('#product-search').addEventListener('click', () => loadProducts(1));
    $('#product-keyword').addEventListener('keydown', (event) => {
      if (event.key === 'Enter') loadProducts(1);
    });
    $('#product-category').addEventListener('change', () => loadProducts(1));
    $('#product-prev').addEventListener('click', () => loadProducts(state.productPage - 1));
    $('#product-next').addEventListener('click', () => loadProducts(state.productPage + 1));
    $('#clear-cart').addEventListener('click', () => {
      CartStore.clear();
      toast('购物车已清空');
    });
    $('#checkout-btn').addEventListener('click', checkout);
    $('#refresh-orders').addEventListener('click', loadOrders);
    $$('[data-route]').forEach(button => button.addEventListener('click', () => navigate(button.dataset.route)));
  }

  function toggleAuth(type) {
    $('#login-form').classList.toggle('hidden', type !== 'login');
    $('#register-form').classList.toggle('hidden', type !== 'register');
  }

  async function handleLogin(event) {
    event.preventDefault();
    try {
      const username = $('#login-username').value.trim();
      const password = $('#login-password').value.trim();
      if (!username || !password) throw new Error('请输入用户名和密码');
      const res = await Request.api.login(username, password);
      Request.setToken(res.data.tokenInfo.token);
      Request.setUser(res.data.user);
      await enterApp();
      toast('登录成功');
    } catch (error) {
      toast(error.message, 'error');
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    try {
      const username = $('#reg-username').value.trim();
      const password = $('#reg-password').value.trim();
      if (!username || password.length < 6) throw new Error('用户名不能为空，密码至少6位');
      const res = await Request.api.register(username, password, $('#reg-phone').value.trim(), $('#reg-email').value.trim());
      Request.setToken(res.data.tokenInfo.token);
      Request.setUser(res.data.user);
      await enterApp();
      toast('注册成功');
    } catch (error) {
      toast(error.message, 'error');
    }
  }

  function logout() {
    Request.clearAuth();
    $('#main-view').classList.add('hidden');
    $('#auth-view').classList.remove('hidden');
    toggleAuth('login');
    toast('已退出登录');
  }

  async function enterApp() {
    const user = Request.getUser();
    $('#current-user').textContent = user ? user.username : '用户';
    $('#auth-view').classList.add('hidden');
    $('#main-view').classList.remove('hidden');
    navigate(location.hash.replace('#/', '') || 'products');
  }

  function navigate(route) {
    const known = ['products', 'cart', 'orders', 'success'];
    const target = known.includes(route) ? route : '404';
    $$('.page').forEach(page => page.classList.add('hidden'));
    $(`#page-${target}`).classList.remove('hidden');
    $$('nav button').forEach(button => button.classList.toggle('active', button.dataset.route === target));
    history.replaceState(null, '', `#/${target}`);

    if (target === 'products') loadProducts(state.productPage || 1);
    if (target === 'orders') {
      loadStats();
      loadOrders();
    }
    if (target === 'cart') renderCart(CartStore.getState());
  }

  async function loadProducts(page = 1) {
    if (page < 1 || page > state.productPages && state.productPages > 0) return;
    state.productPage = page;
    $('#product-grid').innerHTML = '<div class="empty">商品加载中...</div>';
    try {
      const res = await Request.api.products({
        pageNum: page,
        pageSize: state.pageSize,
        status: 1,
        keyword: $('#product-keyword').value.trim(),
        category: $('#product-category').value,
      });
      state.productTotal = res.total || 0;
      state.productPages = res.pages || 1;
      renderProducts(res.data || []);
      $('#product-page-info').textContent = `共 ${state.productTotal} 件商品，第 ${state.productPage}/${state.productPages} 页`;
      $('#product-prev').disabled = state.productPage <= 1;
      $('#product-next').disabled = state.productPage >= state.productPages;
    } catch (error) {
      $('#product-grid').innerHTML = `<div class="empty">${escapeHtml(error.message)}</div>`;
    }
  }

  function renderProducts(products) {
    if (!products.length) {
      $('#product-grid').innerHTML = '<div class="empty">暂无商品</div>';
      return;
    }
    $('#product-grid').innerHTML = products.map(product => `
      <article class="product-card">
        <div class="img-wrap">
          <img src="${escapeHtml(product.imageUrl || '')}" alt="${escapeHtml(product.name)}" loading="lazy">
        </div>
        <div class="body">
          <div class="meta"><span class="tag">${escapeHtml(product.category)}</span><span>库存 ${product.stock}</span></div>
          <h3>${escapeHtml(product.name)}</h3>
          <p>${escapeHtml(product.description || '')}</p>
          <div class="meta"><span class="price">${money(product.price)}</span><button class="primary compact" onclick='App.addToCart(${JSON.stringify(product)})'>加入购物车</button></div>
        </div>
      </article>
    `).join('');
  }

  function addToCart(product) {
    CartStore.add(product);
    toast('已加入购物车');
  }

  function renderCart(cart) {
    $('#cart-badge').textContent = cart.totalCount;
    $('#cart-total').textContent = money(cart.totalAmount);
    if (!cart.items.length) {
      $('#cart-list').innerHTML = '<div class="empty">购物车为空</div>';
      return;
    }
    $('#cart-list').innerHTML = cart.items.map(item => `
      <div class="cart-item">
        <img src="${escapeHtml(item.imageUrl || '')}" alt="${escapeHtml(item.name)}">
        <div>
          <strong>${escapeHtml(item.name)}</strong>
          <p>${escapeHtml(item.category)} · ${money(item.price)}</p>
        </div>
        <div class="cart-actions">
          <input type="number" min="1" value="${item.quantity}" onchange="App.changeCartQuantity(${item.id}, this.value)">
          <strong>${money(item.price * item.quantity)}</strong>
          <button class="danger small" onclick="App.removeFromCart(${item.id})">删除</button>
        </div>
      </div>
    `).join('');
  }

  function removeFromCart(id) {
    CartStore.remove(id);
    toast('商品已删除');
  }

  function changeCartQuantity(id, quantity) {
    CartStore.changeQuantity(id, quantity);
  }

  async function checkout() {
    const cart = CartStore.getState();
    const user = Request.getUser();
    if (!cart.items.length) {
      toast('请先添加商品', 'error');
      return;
    }
    try {
      const res = await Request.api.createOrder({
        userId: user && user.id ? user.id : 2,
        totalAmount: cart.totalAmount,
        status: 1,
        receiverName: user ? user.username : '测试用户',
        receiverPhone: user && user.phone ? user.phone : '13800138000',
        shippingAddress: '默认收货地址',
        remark: `购物车下单：${cart.items.map(item => `${item.name}x${item.quantity}`).join('，')}`,
      });
      CartStore.clear();
      $('#success-order-no').textContent = `订单号：${res.data.orderNo}`;
      navigate('success');
    } catch (error) {
      toast(error.message, 'error');
    }
  }

  async function loadOrders() {
    $('#order-body').innerHTML = '<tr><td colspan="7">订单加载中...</td></tr>';
    try {
      const res = await Request.api.orders({ pageNum: 1, pageSize: 20 });
      const orders = res.data || [];
      if (!orders.length) {
        $('#order-body').innerHTML = '<tr><td colspan="7">暂无订单</td></tr>';
        return;
      }
      $('#order-body').innerHTML = orders.map(order => `
        <tr>
          <td>${escapeHtml(order.orderNo)}</td>
          <td>${order.userId}</td>
          <td>${money(order.totalAmount)}</td>
          <td>${statusMap[order.status] || '未知'}</td>
          <td>${escapeHtml(order.receiverName || '-')}</td>
          <td>${escapeHtml(order.receiverPhone || '-')}</td>
          <td>${order.createdAt ? new Date(order.createdAt).toLocaleString('zh-CN') : '-'}</td>
        </tr>
      `).join('');
    } catch (error) {
      $('#order-body').innerHTML = `<tr><td colspan="7">${escapeHtml(error.message)}</td></tr>`;
    }
  }

  async function loadStats() {
    try {
      const res = await Request.api.orderStats();
      const data = res.data || {};
      $('#stat-total').textContent = data.totalOrders || 0;
      $('#stat-pending').textContent = data.status_0 || 0;
      $('#stat-paid').textContent = data.status_1 || 0;
      $('#stat-today').textContent = data.todayOrders || 0;
    } catch (error) {
      toast(error.message, 'error');
    }
  }

  function init() {
    bindEvents();
    CartStore.subscribe(renderCart);
    window.addEventListener('hashchange', () => navigate(location.hash.replace('#/', '') || 'products'));
    if (Request.getToken()) {
      enterApp();
    }
  }

  return { init, navigate, addToCart, removeFromCart, changeCartQuantity };
})();

document.addEventListener('DOMContentLoaded', App.init);
