package com.example.backend.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商品详情，包含评价列表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductDetailVO extends ProductVO {

    private List<ReviewVO> reviews;
}
