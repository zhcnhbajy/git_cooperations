package com.example.backend.vo;

import lombok.Data;

/**
 * 订单明细（含下单时的名称与单价快照），金额单位为分。
 */
@Data
public class OrderItemVO {

    private Long productId;
    private String productName;
    private Integer priceCents;
    private Integer quantity;
    private Integer subtotalCents;
}
