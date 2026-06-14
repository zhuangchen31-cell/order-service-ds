/**
 * productApp.js - 商品列表应用逻辑
 * 
 * 功能：
 * 1. 商品列表分页展示
 * 2. 分类筛选功能
 * 3. 关键词搜索功能
 * 4. 商品详情查看
 * 5. Mock/API模式切换
 */

const ProductApp = (() => {
  // ========== 工具函数 ==========
  function $(sel) { return document.querySelector(sel); }
  function $$(sel) { return document.querySelectorAll(sel); }
  function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }

  // Toast提示
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

  // ========== 页面状态 ==========
  let currentPage = 1;
  let totalPages = 1;
  let totalProducts = 0;
  let currentCategoryId = null;
  let currentKeyword = '';
  let isLoading = false;

  // ========== 分类加载 ==========
  /**
   * 加载商品分类列表
   */
  async function loadCategories() {
    try {
      const res = await ProductAPI.getCategories();
      if (res.success && res.data) {
        renderCategoryTabs(res.data);
      }
    } catch (err) {
      console.error('加载分类失败:', err);
      showToast('加载分类失败', 'error');
    }
  }

  /**
   * 渲染分类标签
   * @param {Array} categories - 分类列表
   */
  function renderCategoryTabs(categories) {
    const container = $('#category-tabs');
    if (!container) return;

    let html = `<button class="category-tab active" data-id="" onclick="ProductApp.selectCategory(null)">
      全部商品
    </button>`;

    categories.forEach(cat => {
      html += `<button class="category-tab" data-id="${cat.id}" onclick="ProductApp.selectCategory(${cat.id})">
        ${cat.icon || '📦'} ${cat.name}
      </button>`;
    });

    container.innerHTML = html;
  }

  /**
   * 选择分类
   * @param {number|null} categoryId - 分类ID
   */
  function selectCategory(categoryId) {
    currentCategoryId = categoryId;
    currentPage = 1;

    // 更新标签样式
    $$('.category-tab').forEach(tab => {
      const id = tab.dataset.id;
      if ((categoryId === null && id === '') || parseInt(id) === categoryId) {
        tab.classList.add('active');
      } else {
        tab.classList.remove('active');
      }
    });

    // 重新加载商品列表
    loadProducts();
  }

  // ========== 商品列表加载 ==========
  /**
   * 加载商品列表（分页查询）
   * @param {number} page - 页码
   */
  async function loadProducts(page = 1) {
    if (isLoading) return;
    isLoading = true;

    currentPage = page;

    // 显示加载状态
    const tbody = $('#product-list');
    if (tbody) {
      tbody.innerHTML = '<div class="loading">正在加载商品数据...</div>';
    }

    try {
      // 构建查询参数
      const params = {
        pageNum: page,
        pageSize: 12,
        categoryId: currentCategoryId,
        keyword: currentKeyword
      };

      // 发送异步请求（每次翻页都会发送新请求）
      console.log(`发送商品列表请求: 第${page}页, 分类=${currentCategoryId || '全部'}, 关键词=${currentKeyword || '无'}`);
      const res = await ProductAPI.getProducts(params);

      if (res.success) {
        const data = res.data;
        totalPages = data.pages || 1;
        totalProducts = data.total || 0;
        renderProductList(data.list || []);
        renderPagination();
        updateStats();
      } else {
        showToast(res.message || '加载失败', 'error');
      }
    } catch (err) {
      console.error('加载商品失败:', err);
      showToast('加载商品失败: ' + err.message, 'error');
      if (tbody) {
        tbody.innerHTML = '<div class="error">加载失败，请稍后重试</div>';
      }
    } finally {
      isLoading = false;
    }
  }

  /**
   * 渲染商品列表
   * @param {Array} products - 商品列表
   */
  function renderProductList(products) {
    const container = $('#product-list');
    if (!container) return;

    if (!products || products.length === 0) {
      container.innerHTML = '<div class="empty">暂无商品数据</div>';
      return;
    }

    container.innerHTML = products.map(p => {
      // 获取促销标签图标
      const promoIconMap = { flash: '⚡', new: '🆕', fullreduction: '🎁', coupon: '🎫', special: '🔥' };
      const promoTag = p.promotion
        ? `<span class="promotion-tag promo-tag-${p.promotion.type}">
            ${promoIconMap[p.promotion.type] || '🏷️'} ${escapeHtml(p.promotion.label)}
          </span>`
        : '';

      return `
      <div class="product-card" onclick="ProductApp.viewProduct(${p.id})">
        <div class="product-image">
          ${promoTag}
          <img src="${p.image || 'https://picsum.photos/200'}" alt="${escapeHtml(p.name)}" loading="lazy">
          ${p.status === 0 ? '<span class="sold-out">已售罄</span>' : ''}
        </div>
        <div class="product-info">
          <div class="product-category">${escapeHtml(p.categoryName || '未分类')}</div>
          <div class="product-name">${escapeHtml(p.name)}</div>
          <div class="product-price">
            <span class="current-price">${formatMoney(p.price)}</span>
            ${p.originalPrice > p.price ? `<span class="original-price">${formatMoney(p.originalPrice)}</span>` : ''}
          </div>
          <div class="product-meta">
            <span>库存: ${p.stock || 0}</span>
            <span>销量: ${p.sales || 0}</span>
          </div>
        </div>
      </div>
    `}).join('');
  }

  /**
   * 渲染分页组件
   */
  function renderPagination() {
    const infoContainer = $('#pagination-info');
    const pagesContainer = $('#pagination-pages');

    if (infoContainer) {
      infoContainer.innerHTML = `共 ${totalProducts} 件商品，第 ${currentPage}/${totalPages} 页`;
    }

    if (!pagesContainer) return;

    let html = '';

    // 上一页
    html += `<button class="page-btn" ${currentPage <= 1 ? 'disabled' : ''} 
      onclick="ProductApp.goToPage(${currentPage - 1})">上一页</button>`;

    // 页码按钮
    const maxVisible = 5;
    let startPage = Math.max(1, currentPage - Math.floor(maxVisible / 2));
    let endPage = Math.min(totalPages, startPage + maxVisible - 1);

    if (endPage - startPage < maxVisible - 1) {
      startPage = Math.max(1, endPage - maxVisible + 1);
    }

    if (startPage > 1) {
      html += `<button class="page-btn" onclick="ProductApp.goToPage(1)">1</button>`;
      if (startPage > 2) html += `<span class="page-ellipsis">...</span>`;
    }

    for (let i = startPage; i <= endPage; i++) {
      html += `<button class="page-btn ${i === currentPage ? 'active' : ''}" 
        onclick="ProductApp.goToPage(${i})">${i}</button>`;
    }

    if (endPage < totalPages) {
      if (endPage < totalPages - 1) html += `<span class="page-ellipsis">...</span>`;
      html += `<button class="page-btn" onclick="ProductApp.goToPage(${totalPages})">${totalPages}</button>`;
    }

    // 下一页
    html += `<button class="page-btn" ${currentPage >= totalPages ? 'disabled' : ''} 
      onclick="ProductApp.goToPage(${currentPage + 1})">下一页</button>`;

    pagesContainer.innerHTML = html;
  }

  /**
   * 更新统计信息
   */
  function updateStats() {
    const statsContainer = $('#product-stats');
    if (statsContainer) {
      statsContainer.innerHTML = `
        <span class="stat-item">当前分类: ${currentCategoryId ? '已筛选' : '全部'}</span>
        <span class="stat-item">搜索词: ${currentKeyword || '无'}</span>
        <span class="stat-item">商品总数: ${totalProducts}</span>
      `;
    }
  }

  /**
   * 翻页
   * @param {number} page - 目标页码
   */
  function goToPage(page) {
    if (page >= 1 && page <= totalPages && page !== currentPage) {
      loadProducts(page);
    }
  }

  // ========== 搜索功能 ==========
  /**
   * 搜索商品
   */
  function searchProducts() {
    const keyword = $('#search-keyword')?.value.trim() || '';
    currentKeyword = keyword;
    currentPage = 1;
    loadProducts(1);
  }

  /**
   * 清除搜索
   */
  function clearSearch() {
    if ($('#search-keyword')) {
      $('#search-keyword').value = '';
    }
    currentKeyword = '';
    currentPage = 1;
    loadProducts(1);
  }

  // ========== 商品详情 ==========
  /**
   * 查看商品详情
   * @param {number} id - 商品ID
   */
  async function viewProduct(id) {
    try {
      const res = await ProductAPI.getProductById(id);
      if (res.success && res.data) {
        showProductDetail(res.data);
      } else {
        showToast('商品不存在', 'error');
      }
    } catch (err) {
      showToast('加载详情失败: ' + err.message, 'error');
    }
  }

  // 当前活跃的子组件实例（用于关闭弹窗时清理）
  let activePromoTag = null;
  let activeCountdown = null;

  // 当前查看的商品数据（用于购物车操作）
  let currentProduct = null;

  /**
   * 显示商品详情弹窗（父组件）
   *
   * 父→子传值流程：
   *   1. 从product.promotion提取促销数据
   *   2. 创建PromotionTag子组件实例，传入 {type, label, discount}
   *   3. 若有endTime，创建CountdownTimer子组件实例，传入 {endTime, onExpire}
   *   4. 子组件.render(container) 渲染到父组件提供的DOM容器中
   *
   * @param {Object} product - 商品数据
   */
  function showProductDetail(product) {
    const modal = $('#modal-product');
    if (!modal) return;

    // 保存当前商品引用（供addToCart和buyNow使用）
    currentProduct = product;

    // 构建详情HTML（包含子组件占位容器）
    modal.querySelector('.modal-body').innerHTML = `
      <div class="product-detail">
        <div class="detail-image">
          <img src="${product.image || 'https://picsum.photos/300'}" alt="${escapeHtml(product.name)}">
        </div>
        <div class="detail-info">
          <h3>${escapeHtml(product.name)}</h3>
          <div class="detail-category">分类: ${escapeHtml(product.categoryName || '未分类')}</div>

          <!-- 促销信息区域 — 子组件PromotionTag和CountdownTimer挂载点 -->
          <div class="promotion-area" id="promo-area">
            <div id="promo-tag-container"></div>
            <div id="promo-countdown-container"></div>
          </div>

          <div class="detail-price">
            <span class="current-price">${formatMoney(product.price)}</span>
            ${product.originalPrice > product.price ?
              `<span class="original-price">${formatMoney(product.originalPrice)}</span>
               <span class="discount">优惠 ${Math.round((1 - product.price / product.originalPrice) * 100)}%</span>` : ''}
          </div>
          <div class="detail-meta">
            <span>库存: ${product.stock || 0} 件</span>
            <span>销量: ${product.sales || 0} 件</span>
            <span>状态: ${product.status === 1 ? '在售' : '已售罄'}</span>
          </div>
          <div class="detail-desc">
            <p>${escapeHtml(product.description || '暂无描述')}</p>
          </div>
          <div class="detail-actions">
            <button class="btn btn-primary" ${product.status === 0 ? 'disabled' : ''}
              onclick="ProductApp.buyNow(${product.id})">
              ${product.status === 1 ? '立即购买' : '已售罄'}
            </button>
            <button class="btn btn-success" ${product.status === 0 ? 'disabled' : ''}
              onclick="ProductApp.addToCart(${product.id})">
              🛒 加入购物车
            </button>
            <button class="btn btn-default" onclick="ProductApp.closeModal()">关闭</button>
          </div>
        </div>
      </div>
    `;

    // ========== 父组件向子组件传值 ==========
    const promotion = product.promotion;

    if (promotion) {
      // 1. 创建PromotionTag子组件，父传子：{ type, label, discount }
      const tagContainer = modal.querySelector('#promo-tag-container');
      if (tagContainer) {
        activePromoTag = PromotionTag.create({
          type: promotion.type,         // ← 父传子：促销类型
          label: promotion.label,       // ← 父传子：标签文字
          discount: promotion.discount  // ← 父传子：折扣信息
        });
        activePromoTag.render(tagContainer);
        console.log('✅ [父→子] PromotionTag子组件已渲染，传入props:', promotion);
      }

      // 2. 若有倒计时结束时间，创建CountdownTimer子组件
      if (promotion.endTime) {
        const countdownContainer = modal.querySelector('#promo-countdown-container');
        if (countdownContainer) {
          activeCountdown = CountdownTimer.create({
            endTime: promotion.endTime,  // ← 父传子：结束时间
            showLabels: true,
            compact: false,
            onExpire: () => {
              // 倒计时到期回调
              console.log('⏰ 促销倒计时已到期');
              if (activePromoTag) {
                activePromoTag.update({ type: 'new', label: '促销已结束', discount: '' });
              }
            },
            onTick: (remaining) => {
              // 每秒回调（可用于外部状态同步）
              if (remaining.total <= 3600) {
                // 不足1小时时，倒计时数字变红
                const el = activeCountdown ? activeCountdown.getElement() : null;
                if (el && !el.classList.contains('countdown-urgent')) {
                  el.classList.add('countdown-urgent');
                }
              }
            }
          });
          activeCountdown.render(countdownContainer);
          console.log('✅ [父→子] CountdownTimer子组件已渲染，结束时间:', promotion.endTime);
        }
      }
    }

    modal.classList.remove('hidden');
  }

  /**
   * 添加商品到购物车
   * 通过全局CartHelper操作Pinia Store
   * @param {number} id - 商品ID
   */
  function addToCart(id) {
    if (!currentProduct || currentProduct.id !== id) return;
    if (currentProduct.status === 0) {
      showToast('该商品已售罄', 'error');
      return;
    }

    // 调用全局CartHelper（Pinia Store），实现跨页面状态共享
    if (window.CartHelper) {
      window.CartHelper.addToCart(currentProduct, 1);
      showToast(`"${currentProduct.name}" 已加入购物车`, 'success');
      // 更新导航栏购物车徽标
      updateCartBadge();
    } else {
      // 降级：直接跳转到购物车页面
      showToast('正在跳转到购物车...', 'info');
      setTimeout(() => {
        window.location.href = '/cart.html';
      }, 500);
    }
  }

  /**
   * 立即购买
   * 加入购物车后跳转到下单成功页面
   * @param {number} id - 商品ID
   */
  function buyNow(id) {
    if (!currentProduct || currentProduct.id !== id) return;
    if (currentProduct.status === 0) {
      showToast('该商品已售罄', 'error');
      return;
    }

    // 加入购物车并立即跳转到下单成功页
    if (window.CartHelper) {
      window.CartHelper.addToCart(currentProduct, 1);
    }
    closeModal();
    // 跳转到下单成功页面
    window.location.href = '/order-success.html';
  }

  /**
   * 更新导航栏购物车徽标数量
   */
  function updateCartBadge() {
    const badges = document.querySelectorAll('.cart-badge');
    const count = window.CartHelper ? window.CartHelper.getTotalCount() : 0;
    badges.forEach(badge => {
      badge.textContent = count;
      badge.style.display = count > 0 ? 'inline-flex' : 'none';
    });
  }

  /**
   * 关闭弹窗（清理子组件）
   */
  function closeModal() {
    // 销毁子组件实例，清除定时器
    if (activeCountdown) {
      activeCountdown.destroy();
      activeCountdown = null;
    }
    if (activePromoTag) {
      activePromoTag.destroy();
      activePromoTag = null;
    }

    const modal = $('#modal-product');
    if (modal) modal.classList.add('hidden');
  }

  // ========== Mock模式切换 ==========
  /**
   * 切换Mock/API模式
   */
  function toggleMockMode() {
    const currentMode = Request.getConfig().useMock;
    Request.setMockMode(!currentMode);
    
    const btn = $('#btn-toggle-mock');
    if (btn) {
      btn.textContent = currentMode ? '切换为Mock数据' : '切换为真实API';
    }

    showToast(`已切换为${!currentMode ? 'Mock数据模式' : '真实API模式'}`);
    
    // 重新加载数据
    loadCategories();
    loadProducts(1);
  }

  // ========== 初始化 ==========
  /**
   * 初始化应用
   */
  function init() {
    // 默认使用Mock数据（因为后端商品API可能未实现）
    Request.setMockMode(true);

    // 加载分类和商品
    loadCategories();
    loadProducts(1);

    // 绑定搜索事件
    const searchInput = $('#search-keyword');
    if (searchInput) {
      searchInput.addEventListener('keydown', e => {
        if (e.key === 'Enter') searchProducts();
      });
    }

    const searchBtn = $('#btn-search');
    if (searchBtn) {
      searchBtn.addEventListener('click', searchProducts);
    }

    const clearBtn = $('#btn-clear-search');
    if (clearBtn) {
      clearBtn.addEventListener('click', clearSearch);
    }

    // Mock切换按钮
    const mockBtn = $('#btn-toggle-mock');
    if (mockBtn) {
      mockBtn.textContent = '切换为真实API';
      mockBtn.addEventListener('click', toggleMockMode);
    }

    // 更新购物车徽标
    setTimeout(updateCartBadge, 300);

    console.log('商品应用已初始化，当前使用Mock数据模式');
  }

  // ========== 公开方法 ==========
  return {
    init,
    selectCategory,
    goToPage,
    searchProducts,
    clearSearch,
    viewProduct,
    closeModal,
    toggleMockMode,
    addToCart,
    buyNow,
    // 状态获取
    getCurrentPage: () => currentPage,
    getTotalProducts: () => totalProducts,
    getCurrentCategory: () => currentCategoryId
  };
})();

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
  // 检查是否在商品页面
  if (document.querySelector('#product-page')) {
    ProductApp.init();
  }
});