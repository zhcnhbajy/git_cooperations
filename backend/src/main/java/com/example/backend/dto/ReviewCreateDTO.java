package com.example.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交评价请求。
 */
@Data
public class ReviewCreateDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分必须在 1~5 之间")
    @Max(value = 5, message = "评分必须在 1~5 之间")
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    @Size(max = 500, message = "评价内容长度不能超过 500")
    private String content;
}
