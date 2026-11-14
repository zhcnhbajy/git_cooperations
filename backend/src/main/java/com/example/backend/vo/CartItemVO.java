package com.example.backend.vo;

import lombok.Data;

/**
 * 购物车行，金额单位为分。
 */
@Data
public class CartItemVO {

    private Long id;
    private Long productId;
    private String productName;
    private Integer priceCents;
    private Integer quantity;
    private Integer stock;
    private String imageUrl;
    private Boolean available;
    /** 不可结算原因：OFF_SHELF / OUT_OF_STOCK */
    private String invalidReason;
}
