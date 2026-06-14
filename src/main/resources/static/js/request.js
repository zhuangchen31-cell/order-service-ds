/**
 * request.js - 统一异步请求工具封装
 * 
 * 功能特性：
 * 1. 支持真实API请求和Mock数据两种模式
 * 2. 自动携带JWT Token认证
 * 3. 统一错误处理和响应拦截
 * 4. 支持请求超时和重试机制
 * 5. 支持分页查询参数封装
 */

const Request = (() => {
  // ========== 配置项 ==========
  const CONFIG = {
    baseURL: '',           // API基础路径
    timeout: 10000,        // 请求超时时间（毫秒）
    retryCount: 2,         // 失败重试次数
    retryDelay: 1000,      // 重试延迟（毫秒）
    useMock: false,        // 是否使用Mock数据（可通过 setMockMode 切换）
    tokenKey: 'order_token' // Token存储键名
  };

  // ========== Token管理 ==========
  /**
   * 获取存储的Token
   * @returns {string|null} Token字符串
   */
  function getToken() {
    return localStorage.getItem(CONFIG.tokenKey);
  }

  /**
   * 设置Token
   * @param {string} token - JWT Token
   */
  function setToken(token) {
    localStorage.setItem(CONFIG.tokenKey, token);
  }

  /**
   * 清除Token
   */
  function clearToken() {
    localStorage.removeItem(CONFIG.tokenKey);
  }

  /**
   * 检查是否已登录
   * @returns {boolean}
   */
  function isLoggedIn() {
    return !!getToken();
  }

  // ========== Mock数据管理 ==========
  const MockData = {
    // 商品分类数据
    categories: [
      { id: 1, name: '电子产品', icon: '📱' },
      { id: 2, name: '服装鞋帽', icon: '👕' },
      { id: 3, name: '家居用品', icon: '🏠' },
      { id: 4, name: '食品饮料', icon: '🍎' },
      { id: 5, name: '图书文具', icon: '📚' },
      { id: 6, name: '运动户外', icon: '⚽' }
    ],

    // 商品列表数据（100条）
    products: generateMockProducts(100),

    // 根据分类ID筛选商品
    getProductsByCategory(categoryId) {
      if (!categoryId) return this.products;
      return this.products.filter(p => p.categoryId === categoryId);
    },

    // 分页获取商品
    getProductsPage(pageNum, pageSize, categoryId) {
      const filtered = categoryId ? 
        this.products.filter(p => p.categoryId === categoryId) : 
        this.products;
      
      const start = (pageNum - 1) * pageSize;
      const end = start + pageSize;
      const list = filtered.slice(start, end);
      
      return {
        success: true,
        message: '查询成功',
        data: {
          list: list,
          total: filtered.length,
          pageNum: pageNum,
          pageSize: pageSize,
          pages: Math.ceil(filtered.length / pageSize)
        }
      };
    },

    // 根据ID获取商品详情
    getProductById(id) {
      const product = this.products.find(p => p.id === id);
      return {
        success: true,
        message: product ? '查询成功' : '商品不存在',
        data: product || null
      };
    }
  };

  /**
   * 生成Mock商品数据
   * @param {number} count - 商品数量
   * @returns {Array} 商品列表
   */
  function generateMockProducts(count) {
    const products = [];
    const categoryNames = ['电子产品', '服装鞋帽', '家居用品', '食品饮料', '图书文具', '运动户外'];
    const productNames = {
      1: ['iPhone 15 Pro', 'MacBook Air', '华为手机', '小米平板', '蓝牙耳机', '智能手表', '机械键盘', '显示器'],
      2: ['T恤衫', '牛仔裤', '运动鞋', '羽绒服', '连衣裙', '帽子', '袜子', '皮带'],
      3: ['沙发', '床垫', '台灯', '餐具', '毛巾', '收纳盒', '窗帘', '地毯'],
      4: ['牛奶', '饼干', '咖啡', '茶叶', '坚果', '水果', '饮料', '零食'],
      5: ['小说', '笔记本', '钢笔', '教材', '画册', '文具套装', '书包', '词典'],
      6: ['篮球', '跑步鞋', '瑜伽垫', '帐篷', '登山包', '泳镜', '羽毛球', '滑板']
    };

    for (let i = 1; i <= count; i++) {
      const categoryId = Math.ceil(i / 16); // 每个分类约16个商品
      const names = productNames[categoryId] || ['商品'];
      const nameIndex = (i - 1) % names.length;
      
      // 生成促销信息（约60%商品有促销）
      const hasPromotion = Math.random() < 0.6;
      const promoTypes = ['flash', 'new', 'fullreduction', 'coupon', 'special'];
      const promoType = promoTypes[Math.floor(Math.random() * promoTypes.length)];
      const promoConfig = {
        flash:        { label: '限时优惠', discount: '立减' + Math.floor(Math.random() * 200 + 50) + '元' },
        new:          { label: '新品上市', discount: '尝鲜价' },
        fullreduction:{ label: '满减促销', discount: '满' + (Math.floor(Math.random() * 5 + 2) * 100) + '减' + Math.floor(Math.random() * 100 + 20) },
        coupon:       { label: '优惠券', discount: '领券减' + Math.floor(Math.random() * 50 + 10) + '元' },
        special:      { label: '特价秒杀', discount: Math.floor(Math.random() * 40 + 10) + '% OFF' }
      };

      // 需要倒计时的促销类型
      const needsCountdown = (promoType === 'flash' || promoType === 'special');
      // 倒计时结束时间（1-72小时后）
      const countdownEnd = needsCountdown
        ? new Date(Date.now() + (Math.random() * 72 + 1) * 3600000).toISOString()
        : null;

      const promotion = hasPromotion ? {
        type: promoType,
        label: promoConfig[promoType].label,
        discount: promoConfig[promoType].discount,
        endTime: countdownEnd           // 父组件将此传给倒计时子组件
      } : null;

      products.push({
        id: i,
        name: names[nameIndex] + ' ' + Math.floor(i / 8 + 1),
        categoryId: categoryId,
        categoryName: categoryNames[categoryId - 1],
        price: parseFloat((Math.random() * 1000 + 50).toFixed(2)),
        originalPrice: parseFloat((Math.random() * 1500 + 100).toFixed(2)),
        stock: Math.floor(Math.random() * 500) + 10,
        sales: Math.floor(Math.random() * 1000),
        image: `https://picsum.photos/200?random=${i}`,
        description: `这是${categoryNames[categoryId - 1]}分类下的优质商品，品质保证，欢迎选购。`,
        status: Math.random() > 0.1 ? 1 : 0, // 90%上架
        promotion: promotion,           // 促销信息（父组件传给PromotionTag和CountdownTimer子组件）
        createdAt: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString()
      });
    }
    return products;
  }

  // ========== 核心请求方法 ==========
  /**
   * 创建带超时的fetch请求
   * @param {string} url - 请求URL
   * @param {Object} options - fetch选项
   * @returns {Promise} 响应Promise
   */
  function fetchWithTimeout(url, options = {}) {
    return new Promise((resolve, reject) => {
      const timer = setTimeout(() => {
        reject(new Error('请求超时'));
      }, CONFIG.timeout);

      fetch(url, options)
        .then(response => {
          clearTimeout(timer);
          resolve(response);
        })
        .catch(error => {
          clearTimeout(timer);
          reject(error);
        });
    });
  }

  /**
   * 核心请求方法（支持重试）
   * @param {string} url - 请求URL
   * @param {Object} options - 请求选项
   * @param {number} retry - 当前重试次数
   * @returns {Promise} 响应数据
   */
  async function request(url, options = {}, retry = 0) {
    // 构建请求头
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    };

    // 自动添加Token认证
    const token = getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    // 构建完整URL
    const fullUrl = CONFIG.baseURL + url;

    try {
      const response = await fetchWithTimeout(fullUrl, {
        ...options,
        headers
      });

      // 解析响应
      const data = await response.json();

      // HTTP状态码检查
      if (!response.ok) {
        // 401未授权，清除Token
        if (response.status === 401) {
          clearToken();
          throw new Error('登录已过期，请重新登录');
        }
        throw new Error(data.message || `请求失败 (${response.status})`);
      }

      // 业务状态检查
      if (data.success === false) {
        throw new Error(data.message || '操作失败');
      }

      return data;

    } catch (error) {
      // 重试机制（仅对网络错误重试）
      if (retry < CONFIG.retryCount && 
          (error.message === '请求超时' || error.message.includes('network'))) {
        console.warn(`请求失败，${CONFIG.retryDelay}ms后重试 (${retry + 1}/${CONFIG.retryCount})`);
        await new Promise(resolve => setTimeout(resolve, CONFIG.retryDelay));
        return request(url, options, retry + 1);
      }
      throw error;
    }
  }

  /**
   * Mock请求处理
   * @param {string} url - 请求URL
   * @param {Object} options - 请求选项
   * @returns {Promise} Mock响应数据
   */
  async function mockRequest(url, options = {}) {
    // 模拟网络延迟（100-300ms）
    const delay = Math.random() * 200 + 100;
    await new Promise(resolve => setTimeout(resolve, delay));

    // 解析URL和参数
    const urlObj = new URL(url, 'http://localhost');
    const pathname = urlObj.pathname;
    const params = Object.fromEntries(urlObj.searchParams);

    // 路由匹配
    if (pathname === '/api/products') {
      const pageNum = parseInt(params.pageNum) || 1;
      const pageSize = parseInt(params.pageSize) || 10;
      const categoryId = params.categoryId ? parseInt(params.categoryId) : null;
      return MockData.getProductsPage(pageNum, pageSize, categoryId);
    }

    if (pathname.match(/^\/api\/products\/\d+$/)) {
      const id = parseInt(pathname.split('/').pop());
      return MockData.getProductById(id);
    }

    if (pathname === '/api/categories') {
      return {
        success: true,
        message: '查询成功',
        data: MockData.categories
      };
    }

    // 未匹配的Mock路由
    return {
      success: false,
      message: 'Mock数据未定义此接口'
    };
  }

  /**
   * 统一请求入口（自动选择真实API或Mock）
   * @param {string} url - 请求URL
   * @param {Object} options - 请求选项
   * @returns {Promise} 响应数据
   */
  async function send(url, options = {}) {
    if (CONFIG.useMock) {
      return mockRequest(url, options);
    }
    return request(url, options);
  }

  // ========== 快捷请求方法 ==========
  /**
   * GET请求
   * @param {string} url - 请求URL
   * @param {Object} params - 查询参数
   * @returns {Promise}
   */
  function get(url, params = {}) {
    const query = new URLSearchParams(params).toString();
    const fullUrl = query ? `${url}?${query}` : url;
    return send(fullUrl, { method: 'GET' });
  }

  /**
   * POST请求
   * @param {string} url - 请求URL
   * @param {Object} data - 请求体数据
   * @returns {Promise}
   */
  function post(url, data = {}) {
    return send(url, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  /**
   * PUT请求
   * @param {string} url - 请求URL
   * @param {Object} data - 请求体数据
   * @returns {Promise}
   */
  function put(url, data = {}) {
    return send(url, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  }

  /**
   * DELETE请求
   * @param {string} url - 请求URL
   * @returns {Promise}
   */
  function del(url) {
    return send(url, { method: 'DELETE' });
  }

  // ========== 配置方法 ==========
  /**
   * 设置Mock模式
   * @param {boolean} useMock - 是否使用Mock数据
   */
  function setMockMode(useMock) {
    CONFIG.useMock = useMock;
    console.log(`请求模式已切换为: ${useMock ? 'Mock数据' : '真实API'}`);
  }

  /**
   * 设置基础URL
   * @param {string} baseURL - API基础路径
   */
  function setBaseURL(baseURL) {
    CONFIG.baseURL = baseURL;
  }

  /**
   * 设置超时时间
   * @param {number} timeout - 超时毫秒数
   */
  function setTimeout(timeout) {
    CONFIG.timeout = timeout;
  }

  // ========== 公开接口 ==========
  return {
    // Token管理
    getToken,
    setToken,
    clearToken,
    isLoggedIn,

    // 核心请求
    send,
    request,
    get,
    post,
    put,
    del,

    // 配置
    setMockMode,
    setBaseURL,
    setTimeout,
    getConfig: () => CONFIG,

    // Mock数据（调试用）
    MockData
  };
})();

// 导出（支持ES Module和全局变量）
if (typeof module !== 'undefined' && module.exports) {
  module.exports = Request;
}