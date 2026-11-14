package com.example.backend.entity;

import lombok.Data;

/**
 * 商品分类实体，对应表 category。
 */
@Data
public class Category {

    /** 主键ID */
    private Long id;

    /** 分类名称 */
    private String name;

    /** 父分类ID，0 表示一级分类 */
    private Long parentId;

    /** 排序值 */
    private Integer sortOrder;
}
