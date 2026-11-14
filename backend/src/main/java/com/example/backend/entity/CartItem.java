package com.example.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车项实体，对应表 cart_item。
 */
@Data
public class CartItem {

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 数量 */
    private Integer quantity;

    /** 加入时间 */
    private LocalDateTime createTime;
}
