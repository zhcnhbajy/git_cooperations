package com.example.backend.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体，对应表 product。
 */
@Data
public class Product {

    /** 主键ID */
    private Long id;

    /** 所属二级分类ID */
    private Long categoryId;

    /** 商品名称 */
    private String name;

    /** 商品价格（库中 DECIMAL(10,2)，单位元） */
    private BigDecimal price;

    /** 库存，不允许为负 */
    private Integer stock;

    /** 商品描述 */
    private String description;

    /** 图片地址 */
    private String imageUrl;

    /** 状态：0-已下架，1-已上架 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
