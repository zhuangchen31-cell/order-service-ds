const Request = (() => {
  const TOKEN_KEY = 'order_token';
  const USER_KEY = 'order_user';

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function setToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
  }

  function clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  function getUser() {
    const value = localStorage.getItem(USER_KEY);
    return value ? JSON.parse(value) : null;
  }

  function setUser(user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  async function request(url, options = {}) {
    const headers = new Headers(options.headers || {});
    if (!headers.has('Content-Type') && options.body) {
      headers.set('Content-Type', 'application/json');
    }
    const token = getToken();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }

    const response = await fetch(url, { ...options, headers });
    const contentType = response.headers.get('content-type') || '';
    const payload = contentType.includes('application/json') ? await response.json() : await response.text();

    if (!response.ok || payload.success === false) {
      const message = payload && payload.message ? payload.message : `请求失败 (${response.status})`;
      const error = new Error(message);
      error.status = response.status;
      error.payload = payload;
      throw error;
    }
    return payload;
  }

  const api = {
    login: (username, password) => request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }),
    register: (username, password, phone, email) => request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, password, phone, email }),
    }),
    me: () => request('/api/auth/me'),
    products: (params) => request(`/api/products/query?${new URLSearchParams(params).toString()}`),
    addProduct: (product) => request('/api/products/add', { method: 'POST', body: JSON.stringify(product) }),
    updateProduct: (id, product) => request(`/api/products/update/${id}`, { method: 'PUT', body: JSON.stringify(product) }),
    deleteProduct: (id) => request(`/api/products/delete/${id}`, { method: 'DELETE' }),
    orders: (params) => request(`/api/orders?${new URLSearchParams(params).toString()}`),
    createOrder: (order) => request('/api/orders', { method: 'POST', body: JSON.stringify(order) }),
    orderStats: () => request('/api/orders/stats/overview'),
  };

  return { request, api, getToken, setToken, clearAuth, getUser, setUser };
})();
