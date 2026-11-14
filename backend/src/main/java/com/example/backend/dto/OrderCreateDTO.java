package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 结算下单请求。
 */
@Data
public class OrderCreateDTO {

    @NotEmpty(message = "请至少选择一件商品")
    private List<Long> cartItemIds;

    @NotBlank(message = "收货地址不能为空")
    @Size(max = 255, message = "收货地址长度不能超过 255")
    private String address;
}
