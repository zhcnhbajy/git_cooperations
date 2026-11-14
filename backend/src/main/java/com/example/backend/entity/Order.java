package com.example.backend.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体，对应表 orders。
 */
@Data
public class Order {

    /** 主键ID */
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 下单用户ID */
    private Long userId;

    /** 订单总金额（库中 DECIMAL(10,2)，单位元） */
    private BigDecimal totalAmount;

    /** 状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消 */
    private Integer status;

    /** 收货地址 */
    private String address;

    /** 下单时间 */
    private LocalDateTime createTime;
}
