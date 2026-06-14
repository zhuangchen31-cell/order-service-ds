package com.ecommerce.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@TableName("product")
public class Product {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称 */
    private String name;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 商品价格 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 库存数量 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /** 商品图片URL */
    private String image;

    /** 商品描述 */
    private String description;

    /** 商品状态：1-上架 0-下架 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标志 */
    @TableLogic
    private Integer deleted;
}
