package com.example.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价实体，对应表 review。
 */
@Data
public class Review {

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 关联订单ID */
    private Long orderId;

    /** 评分，1~5 星 */
    private Integer rating;

    /** 评价文字 */
    private String content;

    /** 评价时间 */
    private LocalDateTime createTime;
}
