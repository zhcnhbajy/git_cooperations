package com.example.backend.controller;

import com.example.backend.common.CurrentUser;
import com.example.backend.common.LoginUser;
import com.example.backend.common.PageResult;
import com.campusswap.common.Result;
import com.example.backend.dto.ProductSaveDTO;
import com.example.backend.dto.ProductStatusDTO;
import com.example.backend.service.ProductService;
import com.example.backend.vo.ProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台商品管理（仅管理员），管理员校验在 Service 层完成。
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    /** H1 商品列表（含下架，按分类 / 状态筛选） */
    @GetMapping
    public Result<PageResult<ProductVO>> list(@CurrentUser LoginUser operator,
                                              @RequestParam(required = false) Long categoryId,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) Integer pageNum,
                                              @RequestParam(required = false) Integer pageSize) {
        return Result.success(productService.adminList(operator, categoryId, status, pageNum, pageSize));
    }

    /** H2 新增商品 */
    @PostMapping
    public Result<ProductVO> create(@CurrentUser LoginUser operator, @Valid @RequestBody ProductSaveDTO dto) {
        return Result.success("创建成功", productService.create(operator, dto));
    }

    /** H3 编辑商品 */
    @PutMapping("/{id}")
    public Result<ProductVO> update(@CurrentUser LoginUser operator, @PathVariable Long id,
                                    @Valid @RequestBody ProductSaveDTO dto) {
        return Result.success("更新成功", productService.update(operator, id, dto));
    }

    /** H4 上架 / 下架 */
    @PutMapping("/{id}/status")
    public Result<ProductVO> updateStatus(@CurrentUser LoginUser operator, @PathVariable Long id,
                                          @Valid @RequestBody ProductStatusDTO dto) {
        return Result.success("状态已更新", productService.updateStatus(operator, id, dto.getStatus()));
    }
}
