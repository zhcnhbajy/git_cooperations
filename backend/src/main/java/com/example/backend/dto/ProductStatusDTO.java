package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台上架 / 下架商品请求。
 */
@Data
public class ProductStatusDTO {

    /** 状态：0-已下架，1-已上架 */
    @NotNull(message = "商品状态不能为空")
    private Integer status;
}
