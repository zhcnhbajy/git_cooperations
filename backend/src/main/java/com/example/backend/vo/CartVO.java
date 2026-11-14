package com.example.backend.vo;

import lombok.Data;

import java.util.List;

/**
 * 购物车，金额单位为分。
 */
@Data
public class CartVO {

    private List<CartItemVO> items;
    private Integer totalAmountCents;
    private Integer totalQuantity;
}
