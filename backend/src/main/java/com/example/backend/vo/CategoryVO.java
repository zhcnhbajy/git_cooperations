package com.example.backend.vo;

import lombok.Data;

import java.util.List;

/**
 * 分类树节点。
 */
@Data
public class CategoryVO {

    private Long id;
    private String name;
    private List<CategoryVO> children;
}
