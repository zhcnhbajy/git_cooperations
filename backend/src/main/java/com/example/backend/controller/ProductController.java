package com.example.backend.controller;

import com.example.backend.common.PageResult;
import com.campusswap.common.Result;
import com.example.backend.service.ProductService;
import com.example.backend.vo.ProductDetailVO;
import com.example.backend.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品模块（前台，公开）：推荐 / 列表 / 详情。
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** C1 推荐商品（3~8 件） */
    @GetMapping("/recommend")
    public Result<List<ProductVO>> recommend(@RequestParam(required = false) Integer limit) {
        return Result.success(productService.recommend(limit));
    }

    /** C2 商品列表（分类筛选 + 分页，仅已上架） */
    @GetMapping
    public Result<PageResult<ProductVO>> list(@RequestParam(required = false) Long categoryId,
                                              @RequestParam(required = false) Integer pageNum,
                                              @RequestParam(required = false) Integer pageSize) {
        return Result.success(productService.list(categoryId, pageNum, pageSize));
    }

    /** C3 商品详情（含评价列表） */
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productService.detail(id));
    }
}
