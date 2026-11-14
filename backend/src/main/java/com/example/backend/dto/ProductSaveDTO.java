package com.example.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台新增 / 编辑商品请求。金额单位为分。
 */
@Data
public class ProductSaveDTO {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称长度不能超过 100")
    private String name;

    @NotNull(message = "商品价格不能为空")
    @Min(value = 1, message = "商品价格必须大于 0")
    private Integer priceCents;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @Size(max = 500, message = "商品描述长度不能超过 500")
    private String description;

    @Size(max = 255, message = "图片地址长度不能超过 255")
    private String imageUrl;

    /** 状态：0-已下架，1-已上架，默认 1 */
    private Integer status;
}
