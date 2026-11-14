package com.example.backend.controller;

import com.example.backend.common.CurrentUser;
import com.example.backend.common.LoginUser;
import com.campusswap.common.Result;
import com.example.backend.dto.CartAddDTO;
import com.example.backend.dto.CartUpdateDTO;
import com.example.backend.service.CartService;
import com.example.backend.vo.CartItemVO;
import com.example.backend.vo.CartVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 购物车模块（需登录），仅操作当前用户自己的购物车。
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /** D1 购物车列表（含合计） */
    @GetMapping
    public Result<CartVO> list(@CurrentUser LoginUser user) {
        return Result.success(cartService.list(user.id()));
    }

    /** D2 加入购物车 */
    @PostMapping
    public Result<CartItemVO> add(@CurrentUser LoginUser user, @Valid @RequestBody CartAddDTO dto) {
        return Result.success("已加入购物车", cartService.add(user.id(), dto));
    }

    /** D3 修改数量 */
    @PutMapping("/{id}")
    public Result<CartItemVO> update(@CurrentUser LoginUser user, @PathVariable Long id,
                                     @Valid @RequestBody CartUpdateDTO dto) {
        return Result.success(cartService.updateQuantity(user.id(), id, dto.getQuantity()));
    }

    /** D4 删除单项 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@CurrentUser LoginUser user, @PathVariable Long id) {
        cartService.remove(user.id(), id);
        return Result.success("已删除", null);
    }

    /** D5 清空购物车 */
    @DeleteMapping
    public Result<Void> clear(@CurrentUser LoginUser user) {
        cartService.clear(user.id());
        return Result.success("购物车已清空", null);
    }
}
