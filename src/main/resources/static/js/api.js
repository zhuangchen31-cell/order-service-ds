/**
 * API 通信模块
 * - JWT Token 管理（存储/读取/清除）
 * - 统一请求封装（自动附带 Authorization Header）
 * - 登录/注册/订单 CRUD API
 */
const API = (() => {
  const BASE = '';

  // ========== Token 管理 ==========
  const TOKEN_KEY = 'order_token';
  const USER_KEY = 'order_user';

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function setToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
  }

  function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  function getUser() {
    const u = localStorage.getItem(USER_KEY);
    return u ? JSON.parse(u) : null;
  }

  function setUser(user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  function isLoggedIn() {
    return !!getToken();
  }

  // ========== HTTP 请求封装 ==========
  async function request(url, options = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    const token = getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const resp = await fetch(BASE + url, { ...options, headers });
    const data = await resp.json();

    if (!resp.ok) {
      throw new Error(data.message || `请求失败 (${resp.status})`);
    }

    if (data.success === false) {
      throw new Error(data.message || '操作失败');
    }

    return data;
  }

  // ========== 认证 API ==========
  async function login(username, password) {
    return request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
  }

  async function register(username, password, phone, email) {
    return request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, password, phone, email }),
    });
  }

  async function getCurrentUser() {
    return request('/api/auth/info');
  }

  // ========== 订单 API ==========
  async function getOrders(params = {}) {
    const query = new URLSearchParams(params).toString();
    return request(`/api/orders?${query}`);
  }

  async function getOrderById(id) {
    return request(`/api/orders/${id}`);
  }

  async function createOrder(order) {
    return request('/api/orders', {
      method: 'POST',
      body: JSON.stringify(order),
    });
  }

  async function updateOrder(id, order) {
    return request(`/api/orders/${id}`, {
      method: 'PUT',
      body: JSON.stringify(order),
    });
  }

  async function deleteOrder(id) {
    return request(`/api/orders/${id}`, {
      method: 'DELETE',
    });
  }

  async function batchDeleteOrders(ids) {
    return request('/api/orders/batch', {
      method: 'DELETE',
      body: JSON.stringify(ids),
    });
  }

  async function getOrderStats() {
    return request('/api/orders/stats/overview');
  }

  // ========== 订单明细 API ==========
  async function getOrderItems(orderId) {
    return request(`/api/order-items/order/${orderId}`);
  }

  async function createOrderItem(item) {
    return request('/api/order-items', {
      method: 'POST',
      body: JSON.stringify(item),
    });
  }

  // ========== 公开接口 ==========
  return {
    // Token
    getToken, setToken, clearToken,
    getUser, setUser, isLoggedIn,
    // Auth
    login, register, getCurrentUser,
    // Orders
    getOrders, getOrderById, createOrder,
    updateOrder, deleteOrder, batchDeleteOrders,
    getOrderStats,
    // Order Items
    getOrderItems, createOrderItem,
  };
})();
