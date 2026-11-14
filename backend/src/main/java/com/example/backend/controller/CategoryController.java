package com.example.backend.controller;

import com.campusswap.common.Result;
import com.example.backend.service.CategoryService;
import com.example.backend.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类模块（公开）：两级分类树。
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** B1 分类树（两级） */
    @GetMapping
    public Result<List<CategoryVO>> tree() {
        return Result.success(categoryService.tree());
    }
}
