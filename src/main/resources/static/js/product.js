/**
 * product.js - 商品API接口模块
 * 
 * 功能：
 * 1. 商品列表分页查询
 * 2. 商品分类筛选
 * 3. 商品详情查询
 * 4. 商品搜索功能
 */

const ProductAPI = (() => {
  // ========== 商品分类接口 ==========
  /**
   * 获取商品分类列表
   * @returns {Promise} 分类列表数据
   */
  async function getCategories() {
    return Request.get('/api/categories');
  }

  // ========== 商品列表接口 ==========
  /**
   * 分页查询商品列表
   * @param {Object} params - 查询参数
   * @param {number} params.pageNum - 页码（默认1）
   * @param {number} params.pageSize - 每页条数（默认10）
   * @param {number} params.categoryId - 分类ID（可选）
   * @param {string} params.keyword - 搜索关键词（可选）
   * @param {number} params.minPrice - 最低价格（可选）
   * @param {number} params.maxPrice - 最高价格（可选）
   * @param {number} params.status - 商品状态（可选）
   * @returns {Promise} 商品分页数据
   */
  async function getProducts(params = {}) {
    const queryParams = {
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10
    };

    // 添加可选筛选条件
    if (params.categoryId) queryParams.categoryId = params.categoryId;
    if (params.keyword) queryParams.keyword = params.keyword;
    if (params.minPrice) queryParams.minPrice = params.minPrice;
    if (params.maxPrice) queryParams.maxPrice = params.maxPrice;
    if (params.status) queryParams.status = params.status;

    return Request.get('/api/products', queryParams);
  }

  /**
   * 根据分类ID查询商品列表
   * @param {number} categoryId - 分类ID
   * @param {number} pageNum - 页码
   * @param {number} pageSize - 每页条数
   * @returns {Promise} 商品列表数据
   */
  async function getProductsByCategory(categoryId, pageNum = 1, pageSize = 10) {
    return getProducts({
      categoryId,
      pageNum,
      pageSize
    });
  }

  /**
   * 搜索商品
   * @param {string} keyword - 搜索关键词
   * @param {number} pageNum - 页码
   * @param {number} pageSize - 每页条数
   * @returns {Promise} 搜索结果
   */
  async function searchProducts(keyword, pageNum = 1, pageSize = 10) {
    return getProducts({
      keyword,
      pageNum,
      pageSize
    });
  }

  // ========== 商品详情接口 ==========
  /**
   * 根据ID获取商品详情
   * @param {number} id - 商品ID
   * @returns {Promise} 商品详情数据
   */
  async function getProductById(id) {
    return Request.get(`/api/products/${id}`);
  }

  // ========== 商品管理接口（需要管理员权限） ==========
  /**
   * 创建商品
   * @param {Object} product - 商品数据
   * @returns {Promise}
   */
  async function createProduct(product) {
    return Request.post('/api/products', product);
  }

  /**
   * 更新商品
   * @param {number} id - 商品ID
   * @param {Object} product - 商品数据
   * @returns {Promise}
   */
  async function updateProduct(id, product) {
    return Request.put(`/api/products/${id}`, product);
  }

  /**
   * 删除商品
   * @param {number} id - 商品ID
   * @returns {Promise}
   */
  async function deleteProduct(id) {
    return Request.del(`/api/products/${id}`);
  }

  // ========== 公开接口 ==========
  return {
    // 分类
    getCategories,
    // 商品列表
    getProducts,
    getProductsByCategory,
    searchProducts,
    // 商品详情
    getProductById,
    // 商品管理
    createProduct,
    updateProduct,
    deleteProduct
  };
})();

// 导出
if (typeof module !== 'undefined' && module.exports) {
  module.exports = ProductAPI;
}