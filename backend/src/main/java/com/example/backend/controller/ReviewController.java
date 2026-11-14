package com.example.backend.controller;

import com.example.backend.common.CurrentUser;
import com.example.backend.common.LoginUser;
import com.campusswap.common.Result;
import com.example.backend.dto.ReviewCreateDTO;
import com.example.backend.service.ReviewService;
import com.example.backend.vo.ReviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评价模块（需登录）：仅已完成订单中的商品可评价。
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** F1 提交评价 */
    @PostMapping
    public Result<ReviewVO> create(@CurrentUser LoginUser user, @Valid @RequestBody ReviewCreateDTO dto) {
        return Result.success("评价成功", reviewService.create(user.id(), dto));
    }
}
