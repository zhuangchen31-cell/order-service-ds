package com.ecommerce.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.order.common.StatusCode;
import com.ecommerce.order.common.exception.BusinessException;
import com.ecommerce.order.dto.ApiResponse;
import com.ecommerce.order.entity.Product;
import com.ecommerce.order.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

/**
 * 商品控制器 — 基于RESTful规范
 *
 * 接口映射:
 *   POST   /api/products      → 新增商品 (add)
 *   DELETE /api/products/{id}  → 删除商品 (delete)
 *   PUT    /api/products/{id}  → 更新商品 (update)
 *   GET    /api/products       → 分页查询商品列表 (query)
 *   GET    /api/products/{id}  → 查询单个商品 (query by id)
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // ==================== 新增商品 ====================

    /**
     * 新增商品
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> add(@Valid @RequestBody Product product) {
        // 参数校验
        if (!StringUtils.hasText(product.getName())) {
            throw new BusinessException("商品名称不能为空");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("商品价格必须大于0");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new BusinessException("库存数量不能小于0");
        }

        // 默认值设置
        if (product.getStatus() == null) {
            product.setStatus(1); // 默认上架
        }
        if (product.getSales() == null) {
            product.setSales(0);
        }

        boolean saved = productService.save(product);
        if (saved) {
            return ResponseEntity.ok(ApiResponse.success("商品创建成功", product));
        }
        return ResponseEntity.ok(ApiResponse.error(StatusCode.OPERATION_FAILED, "商品创建失败"));
    }

    // ==================== 删除商品 ====================

    /**
     * 删除商品（逻辑删除）
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在，ID: " + id);
        }

        boolean removed = productService.removeById(id);
        if (removed) {
            return ResponseEntity.ok(ApiResponse.success("商品删除成功", null));
        }
        return ResponseEntity.ok(ApiResponse.error(StatusCode.OPERATION_FAILED, "商品删除失败"));
    }

    // ==================== 更新商品 ====================

    /**
     * 更新商品
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> update(
            @PathVariable Long id,
            @RequestBody Product product) {

        Product existing = productService.getById(id);
        if (existing == null) {
            throw new BusinessException("商品不存在，ID: " + id);
        }

        // 参数校验
        if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("商品价格必须大于0");
        }
        if (product.getStock() != null && product.getStock() < 0) {
            throw new BusinessException("库存数量不能小于0");
        }

        product.setId(id); // 确保ID一致
        boolean updated = productService.updateById(product);
        if (updated) {
            Product updatedProduct = productService.getById(id);
            return ResponseEntity.ok(ApiResponse.success("商品更新成功", updatedProduct));
        }
        return ResponseEntity.ok(ApiResponse.error(StatusCode.OPERATION_FAILED, "商品更新失败"));
    }

    // ==================== 查询商品列表（分页+筛选） ====================

    /**
     * 分页查询商品列表
     * GET /api/products?pageNum=1&pageSize=10&categoryId=2&keyword=xxx&status=1&minPrice=10&maxPrice=1000
     *
     * @param pageNum    页码（默认1）
     * @param pageSize   每页条数（默认10）
     * @param categoryId 分类ID（可选）
     * @param keyword    搜索关键词（可选，模糊匹配商品名）
     * @param status     商品状态（可选，1-上架 0-下架）
     * @param minPrice   最低价格（可选）
     * @param maxPrice   最高价格（可选）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Product>>> query(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        // 参数校验
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 10;

        // 构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        if (minPrice != null) {
            wrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Product::getPrice, maxPrice);
        }

        // 按创建时间降序
        wrapper.orderByDesc(Product::getCreatedAt);

        // 分页查询
        Page<Product> page = new Page<>(pageNum, pageSize);
        Page<Product> resultPage = productService.page(page, wrapper);

        // 包装分页响应
        ApiResponse<Page<Product>> response = ApiResponse.page(
                resultPage,
                resultPage.getTotal(),
                resultPage.getCurrent(),
                resultPage.getSize(),
                resultPage.getPages());
        return ResponseEntity.ok(response);
    }

    // ==================== 查询单个商品 ====================

    /**
     * 根据ID查询商品详情
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> queryById(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在，ID: " + id);
        }
        return ResponseEntity.ok(ApiResponse.success(product));
    }
}
