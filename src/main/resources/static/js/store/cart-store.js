/**
 * cart-store.js — Pinia 购物车全局状态管理
 *
 * 功能：使用Pinia定义购物车Store，管理全局购物车状态
 *
 * 核心功能：
 *   1. 添加商品到购物车 (addToCart)
 *   2. 从购物车删除商品 (removeFromCart)
 *   3. 更新商品数量 (updateQuantity)
 *   4. 计算总价 (totalPrice getter)
 *   5. 计算总数量 (totalCount getter)
 *   6. 清空购物车 (clearCart)
 *   7. localStorage持久化
 *
 * 使用方式（需先加载Vue3 + Pinia CDN）：
 *   <script src="https://unpkg.com/vue@3/dist/vue.global.prod.js"></script>
 *   <script src="https://unpkg.com/pinia@2/dist/pinia.iife.prod.js"></script>
 *   <script src="/js/store/cart-store.js"></script>
 *
 *   // 在Vue组件中使用:
 *   const cartStore = useCartStore();
 *   cartStore.addToCart(product);
 */

// ========== Pinia Store 定义 ==========
// 使用Pinia的defineStore定义购物车Store
const useCartStore = Pinia.defineStore('cart', {
  // ========== 状态 ==========
  state: () => ({
    items: [],           // 购物车商品列表
    loadedFromStorage: false
  }),

  // ========== 计算属性(Getters) ==========
  getters: {
    /**
     * 购物车商品总数
     */
    totalCount: (state) => {
      return state.items.reduce((sum, item) => sum + item.quantity, 0);
    },

    /**
     * 购物车总价（保留2位小数）
     */
    totalPrice: (state) => {
      const total = state.items.reduce(
        (sum, item) => sum + item.price * item.quantity, 0
      );
      return Math.round(total * 100) / 100;
    },

    /**
     * 已选商品数量
     */
    selectedCount: (state) => {
      return state.items.filter(item => item.checked).length;
    },

    /**
     * 购物车是否为空
     */
    isEmpty: (state) => {
      return state.items.length === 0;
    },

    /**
     * 选中商品总价
     */
    selectedTotalPrice: (state) => {
      const total = state.items
        .filter(item => item.checked)
        .reduce((sum, item) => sum + item.price * item.quantity, 0);
      return Math.round(total * 100) / 100;
    }
  },

  // ========== 动作(Actions) ==========
  actions: {
    /**
     * 从localStorage加载购物车数据
     */
    loadFromStorage() {
      if (this.loadedFromStorage) return;
      try {
        const saved = localStorage.getItem('vue-cart-items');
        if (saved) {
          const parsed = JSON.parse(saved);
          if (Array.isArray(parsed)) {
            this.items = parsed;
          }
        }
      } catch (e) {
        console.error('加载购物车数据失败:', e);
      }
      this.loadedFromStorage = true;
    },

    /**
     * 保存购物车数据到localStorage
     */
    saveToStorage() {
      try {
        localStorage.setItem('vue-cart-items', JSON.stringify(this.items));
      } catch (e) {
        console.error('保存购物车数据失败:', e);
      }
    },

    /**
     * 添加商品到购物车
     * @param {Object} product — 商品对象
     * @param {number} [product.id] — 商品ID
     * @param {string} [product.name] — 商品名称
     * @param {number} [product.price] — 商品价格
     * @param {string} [product.image] — 商品图片
     * @param {number} [product.stock] — 库存
     * @param {number} [quantity=1] — 添加数量
     */
    addToCart(product, quantity = 1) {
      if (!product || !product.id) {
        console.error('addToCart: 无效的商品数据');
        return;
      }

      const existing = this.items.find(item => item.id === product.id);

      if (existing) {
        // 已存在：增加数量（不超过库存）
        const newQty = existing.quantity + quantity;
        existing.quantity = Math.min(newQty, product.stock || 999);
      } else {
        // 新商品：添加到购物车
        this.items.push({
          id: product.id,
          name: product.name,
          price: product.price,
          image: product.image || 'https://picsum.photos/100',
          categoryName: product.categoryName || '',
          stock: product.stock || 999,
          quantity: Math.min(quantity, product.stock || 999),
          checked: true,
          addedAt: new Date().toISOString()
        });
      }

      this.saveToStorage();
      console.log(`🛒 已添加: ${product.name} (数量: ${quantity})`);
    },

    /**
     * 从购物车删除商品
     * @param {number} productId — 商品ID
     */
    removeFromCart(productId) {
      const index = this.items.findIndex(item => item.id === productId);
      if (index !== -1) {
        const removed = this.items[index];
        this.items.splice(index, 1);
        this.saveToStorage();
        console.log(`🗑️ 已删除: ${removed.name}`);
      }
    },

    /**
     * 更新商品数量
     * @param {number} productId — 商品ID
     * @param {number} quantity — 新数量
     */
    updateQuantity(productId, quantity) {
      const item = this.items.find(item => item.id === productId);
      if (!item) return;

      const qty = parseInt(quantity) || 1;
      if (qty <= 0) {
        // 数量≤0则删除
        this.removeFromCart(productId);
      } else {
        item.quantity = Math.min(qty, item.stock || 999);
        this.saveToStorage();
      }
    },

    /**
     * 切换商品选中状态
     * @param {number} productId — 商品ID
     */
    toggleCheck(productId) {
      const item = this.items.find(item => item.id === productId);
      if (item) {
        item.checked = !item.checked;
        this.saveToStorage();
      }
    },

    /**
     * 全选/取消全选
     * @param {boolean} checked — 是否全选
     */
    toggleAll(checked) {
      this.items.forEach(item => {
        item.checked = checked;
      });
      this.saveToStorage();
    },

    /**
     * 清空购物车
     */
    clearCart() {
      this.items = [];
      this.saveToStorage();
      console.log('🧹 购物车已清空');
    },

    /**
     * 结算（删除已选中的商品并跳转到下单成功页）
     */
    checkout() {
      const checkedItems = this.items.filter(item => item.checked);
      if (checkedItems.length === 0) {
        return { success: false, message: '请选择要结算的商品' };
      }

      // 删除已结算商品
      this.items = this.items.filter(item => !item.checked);
      this.saveToStorage();

      return { success: true, count: checkedItems.length };
    }
  }
});
