# 电商前端异步请求功能 — 实现思路说明

## 一、整体架构设计

本次实现采用 **三层前端架构**，将请求工具、业务API、页面逻辑分离：

```
┌─────────────────────────────────────────────────────┐
│                   product.html                       │
│              (商品列表页面 - 视图层)                    │
├─────────────────────────────────────────────────────┤
│  productApp.js           product.js                  │
│  (页面逻辑 + 状态管理)     (商品API接口模块)             │
├─────────────────────────────────────────────────────┤
│                   request.js                         │
│           (统一异步请求工具 - 基础层)                    │
├─────────────────────────────────────────────────────┤
│        Mock数据系统    │    真实API (fetch)            │
│    (内置100条商品数据)  │   (对接后端SpringBoot接口)      │
└─────────────────────────────────────────────────────┘
```

### 核心设计原则
1. **单一职责**：request.js 只负责HTTP通信，product.js 只负责商品接口定义，productApp.js 只负责页面交互
2. **可切换数据源**：通过 `Request.setMockMode(true/false)` 一键切换 Mock/真实API
3. **每次翻页发送独立请求**：不缓存全量数据，按需分页加载

---

## 二、request.js — 统一异步请求工具

### 2.1 模块化设计（IIFE模式）
使用立即执行函数表达式（IIFE）创建闭包，避免全局变量污染，对外暴露有限接口。

```javascript
const Request = (() => {
  // 私有变量和函数
  const CONFIG = { ... };
  function getToken() { ... }
  
  // 公开接口
  return {
    get, post, put, del,
    setMockMode, setBaseURL,
    getToken, setToken, clearToken
  };
})();
```

### 2.2 核心功能特性

| 特性 | 实现方式 |
|------|---------|
| **请求超时** | `fetchWithTimeout()` — 包装原生fetch，通过 `setTimeout` + `Promise.race` 实现10秒超时 |
| **失败重试** | 递归调用 + 重试计数器，最多重试2次，仅对网络超时/断网错误重试 |
| **JWT认证** | 自动从localStorage读取Token，注入`Authorization: Bearer xxx`请求头 |
| **401处理** | 收到401响应自动清除本地Token，抛出"登录已过期"提示 |
| **统一错误处理** | HTTP状态码检查 + 业务success字段检查，统一抛出Error |

### 2.3 请求流程

```
send(url, options)
  ├─ useMock == true → mockRequest(url, options)
  │   ├─ 模拟100-300ms延迟
  │   ├─ 解析URL路径和查询参数
  │   ├─ 路由匹配 (/api/products, /api/products/:id, /api/categories)
  │   └─ 返回Mock响应 {success, message, data}
  │
  └─ useMock == false → request(url, options, retry=0)
      ├─ 构建请求头 (Content-Type + Authorization)
      ├─ fetchWithTimeout() 发送请求
      ├─ 检查HTTP状态码 (401清除Token)
      ├─ 检查业务状态 (data.success)
      └─ 网络错误 → 重试 (最多2次，间隔1秒)
```

### 2.4 Mock数据系统

内置于 request.js 的 MockData 对象包含：
- **6个商品分类**：电子产品、服装鞋帽、家居用品、食品饮料、图书文具、运动户外
- **100条商品数据**：通过 `generateMockProducts(100)` 函数生成
  - 每条商品包含：id, name, categoryId, categoryName, price, originalPrice, stock, sales, image, description, status, createdAt
  - 价格随机生成（50-1000元），库存随机（10-510），销量随机（0-1000）
  - 90%商品状态为"在售"，10%为"已售罄"
- **Mock路由器**：模拟真实API的路由解析
  - `GET /api/products?pageNum=1&pageSize=12&categoryId=2` → 分页+分类筛选
  - `GET /api/products/50` → 商品详情
  - `GET /api/categories` → 分类列表

---

## 三、product.js — 商品API接口模块

### 3.1 接口定义

| 方法 | 接口路径 | 说明 |
|------|---------|------|
| `getCategories()` | GET /api/categories | 获取商品分类列表 |
| `getProducts(params)` | GET /api/products | 分页查询商品（支持categoryId/keyword/minPrice/maxPrice/status筛选）|
| `getProductsByCategory(categoryId, pageNum, pageSize)` | GET /api/products | 按分类筛选商品 |
| `searchProducts(keyword, pageNum, pageSize)` | GET /api/products | 关键词搜索商品 |
| `getProductById(id)` | GET /api/products/{id} | 获取商品详情 |
| `createProduct(product)` | POST /api/products | 创建商品（管理端）|
| `updateProduct(id, product)` | PUT /api/products/{id} | 更新商品（管理端）|
| `deleteProduct(id)` | DELETE /api/products/{id} | 删除商品（管理端）|

### 3.2 参数封装
`getProducts(params)` 方法将传入的筛选条件（分类ID、关键词、价格区间、状态）自动拼接为URL查询参数，底层调用 `Request.get()` 发送GET请求。

---

## 四、productApp.js — 商品列表应用逻辑

### 4.1 状态管理
使用模块级变量维护页面状态：
```javascript
let currentPage = 1;        // 当前页码
let totalPages = 1;         // 总页数
let totalProducts = 0;      // 商品总数
let currentCategoryId = null; // 当前筛选分类（null=全部）
let currentKeyword = '';    // 当前搜索关键词
let isLoading = false;      // 加载锁，防止重复请求
```

### 4.2 分页查询实现（一页一次请求）

**核心机制**：每次翻页都发送独立的异步请求，不缓存数据。

```javascript
async function loadProducts(page = 1) {
  if (isLoading) return;     // 防重复请求锁
  isLoading = true;
  
  const params = {
    pageNum: page,           // 页码
    pageSize: 12,            // 每页12条
    categoryId: currentCategoryId,  // 分类筛选
    keyword: currentKeyword  // 关键词搜索
  };
  
  // 发送异步请求 — 每次翻页都是新请求
  const res = await ProductAPI.getProducts(params);
  
  // 更新状态
  totalPages = res.data.pages;
  totalProducts = res.data.total;
  renderProductList(res.data.list);
  renderPagination();
  isLoading = false;
}
```

### 4.3 分类筛选实现

```
用户点击分类标签 → selectCategory(categoryId)
  ├─ 更新 currentCategoryId 状态
  ├─ 重置 currentPage = 1（回到第一页）
  ├─ 更新标签高亮样式
  └─ 调用 loadProducts(1) → 发送带 categoryId 的新请求
```

### 4.4 搜索功能实现

```
用户输入关键词 → searchProducts()
  ├─ 读取输入框值 → currentKeyword
  ├─ 重置 currentPage = 1
  └─ 调用 loadProducts(1) → 发送带 keyword 的新请求
```

### 4.5 分页组件渲染

- 显示"共X件商品，第N/M页"
- 最大可见5个连续页码按钮
- 首尾页码 + 省略号（…）逻辑
- 上一页/下一页按钮边界禁用

### 4.6 模式切换

```javascript
function toggleMockMode() {
  Request.setMockMode(!currentMode);  // 切换数据源
  loadCategories();                    // 重新加载分类
  loadProducts(1);                     // 重新加载商品
}
```

---

## 五、数据流图（以分页查询为例）

```
用户点击"第3页"
  │
  ▼
ProductApp.goToPage(3)
  │
  ▼
ProductApp.loadProducts(3)
  │ 构建 params: {pageNum:3, pageSize:12, categoryId:2}
  ▼
ProductAPI.getProducts(params)
  │ 拼接URL: /api/products?pageNum=3&pageSize=12&categoryId=2
  ▼
Request.get(url, params)
  │
  ├── [Mock模式] → mockRequest()
  │     ├── 解析URL参数
  │     ├── 按categoryId过滤100条Mock数据
  │     ├── 切片取第25-36条（第3页）
  │     └── 返回 {success:true, data:{list:[...], total, pages}}
  │
  └── [真实API] → request()
        ├── fetch(GET /api/products?pageNum=3&pageSize=12&categoryId=2)
        ├── 携带JWT Token
        └── 返回后端分页数据

  ▼
ProductApp 接收响应
  ├── updateState(totalPages, totalProducts)
  ├── renderProductList(data.list) → 渲染12张商品卡片
  └── renderPagination() → 渲染分页按钮
```

---

## 六、技术要点总结

1. **异步请求**：基于fetch API + async/await，兼容现代浏览器
2. **请求工具封装**：IIFE模块模式，支持超时、重试、Token注入、错误统一处理
3. **Mock数据**：内置完整的Mock数据引擎，前后端分离开发时无需后端接口
4. **分页查询**：每次翻页发送独立请求，按需加载，不预加载全量数据
5. **分类筛选**：通过URL查询参数传递categoryId，支持与关键词组合筛选
6. **模式切换**：一键在Mock/真实API间切换，方便开发调试和演示
7. **零依赖**：纯原生JavaScript实现，无npm依赖，无需构建工具
