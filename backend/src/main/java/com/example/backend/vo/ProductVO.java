package com.example.backend.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品信息，金额单位为分。
 */
@Data
public class ProductVO {

    private Long id;
    private String name;
    private Integer priceCents;
    private Integer stock;
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
    private String description;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
