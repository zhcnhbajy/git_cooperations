package com.example.backend.controller;

import com.example.backend.common.CurrentUser;
import com.example.backend.common.LoginUser;
import com.example.backend.common.PageResult;
import com.campusswap.common.Result;
import com.example.backend.dto.OrderCreateDTO;
import com.example.backend.service.OrderService;
import com.example.backend.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单模块（需登录）：下单、查询与状态流转，仅操作当前用户自己的订单。
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** E1 结算下单（事务扣库存） */
    @PostMapping
    public Result<OrderVO> create(@CurrentUser LoginUser user, @Valid @RequestBody OrderCreateDTO dto) {
        return Result.success("下单成功", orderService.create(user.id(), dto));
    }

    /** E2 我的订单列表（按状态筛选） */
    @GetMapping
    public Result<PageResult<OrderVO>> list(@CurrentUser LoginUser user,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) Integer pageNum,
                                            @RequestParam(required = false) Integer pageSize) {
        return Result.success(orderService.listMy(user.id(), status, pageNum, pageSize));
    }

    /** E3 我的订单详情 */
    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@CurrentUser LoginUser user, @PathVariable String orderNo) {
        return Result.success(orderService.detailMy(user.id(), orderNo));
    }

    /** E4 模拟付款：待支付 -> 已支付 */
    @PostMapping("/{orderNo}/pay")
    public Result<OrderVO> pay(@CurrentUser LoginUser user, @PathVariable String orderNo) {
        return Result.success("支付成功", orderService.pay(user.id(), orderNo));
    }

    /** E5 取消待支付订单（库存回补） */
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@CurrentUser LoginUser user, @PathVariable String orderNo) {
        orderService.cancel(user.id(), orderNo);
        return Result.success("订单已取消", null);
    }

    /** E6 确认收货：已发货 -> 已完成 */
    @PostMapping("/{orderNo}/confirm")
    public Result<Void> confirm(@CurrentUser LoginUser user, @PathVariable String orderNo) {
        orderService.confirm(user.id(), orderNo);
        return Result.success("确认收货成功", null);
    }
}
