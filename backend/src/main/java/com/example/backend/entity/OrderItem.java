package com.example.backend.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细实体，对应表 order_item，保存下单时的商品名称与单价快照。
 */
@Data
public class OrderItem {

    /** 主键ID */
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 商品ID */
    private Long productId;

    /** 商品名称快照 */
    private String productName;

    /** 商品单价快照（库中 DECIMAL(10,2)，单位元） */
    private BigDecimal productPrice;

    /** 购买数量 */
    private Integer quantity;
}
