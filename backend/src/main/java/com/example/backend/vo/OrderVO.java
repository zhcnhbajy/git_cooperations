package com.example.backend.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单信息，金额单位为分。后台列表额外填充 userId / username。
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private Integer totalAmountCents;
    private Integer status;
    private String statusText;
    private String address;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private List<OrderItemVO> items;
}
