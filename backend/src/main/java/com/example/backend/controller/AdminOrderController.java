package com.example.backend.controller;

import com.example.backend.common.CurrentUser;
import com.example.backend.common.LoginUser;
import com.example.backend.common.PageResult;
import com.campusswap.common.Result;
import com.example.backend.service.OrderService;
import com.example.backend.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台订单管理（仅管理员），管理员校验在 Service 层完成。
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    /** G1 全部订单列表（按状态筛选） */
    @GetMapping
    public Result<PageResult<OrderVO>> list(@CurrentUser LoginUser operator,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) Integer pageNum,
                                            @RequestParam(required = false) Integer pageSize) {
        return Result.success(orderService.adminList(operator, status, pageNum, pageSize));
    }

    /** G2 订单详情（完整明细） */
    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@CurrentUser LoginUser operator, @PathVariable String orderNo) {
        return Result.success(orderService.adminDetail(operator, orderNo));
    }

    /** G3 标记发货：已支付 -> 已发货 */
    @PutMapping("/{orderNo}/ship")
    public Result<Void> ship(@CurrentUser LoginUser operator, @PathVariable String orderNo) {
        orderService.ship(operator, orderNo);
        return Result.success("发货成功", null);
    }
}
