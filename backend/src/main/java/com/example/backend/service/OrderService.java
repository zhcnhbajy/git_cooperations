package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.common.LoginUser;
import com.example.backend.common.PageResult;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.OrderCreateDTO;
import com.example.backend.entity.CartItem;
import com.example.backend.entity.Order;
import com.example.backend.entity.OrderItem;
import com.example.backend.entity.Product;
import com.example.backend.entity.User;
import com.example.backend.mapper.CartItemMapper;
import com.example.backend.mapper.OrderItemMapper;
import com.example.backend.mapper.OrderMapper;
import com.example.backend.mapper.ProductMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.vo.OrderItemVO;
import com.example.backend.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务：用户下单与状态流转 + 后台订单管理。
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final int STATUS_WAIT_PAY = 0;
    private static final int STATUS_PAID = 1;
    private static final int STATUS_SHIPPED = 2;
    private static final int STATUS_FINISHED = 3;
    private static final int STATUS_CANCELED = 4;

    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    /**
     * 结算下单：校验购物车归属、商品上架与库存，扣库存并写入订单及明细，
     * 任一环节失败整体回滚。
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderVO create(Long userId, OrderCreateDTO dto) {
        checkNotAdmin(userId);
        List<CartItem> cartItems = new ArrayList<>();
        for (Long cartItemId : dto.getCartItemIds()) {
            CartItem cartItem = cartItemMapper.selectById(cartItemId);
            if (cartItem == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "购物车项不存在");
            }
            if (!cartItem.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该购物车项");
            }
            cartItems.add(cartItem);
        }

        List<OrderItem> orderItems = new ArrayList<>();
        int totalAmountCents = 0;
        for (CartItem cartItem : cartItems) {
            Product product = productMapper.selectById(cartItem.getProductId());
            if (product == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
            }
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException(ResultCode.STATUS_CONFLICT, "商品已下架");
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH,
                        "「" + product.getName() + "」库存不足，仅剩 " + product.getStock() + " 件");
            }
            if (productMapper.deductStock(product.getId(), cartItem.getQuantity()) == 0) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH,
                        "「" + product.getName() + "」库存不足");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItems.add(orderItem);

            totalAmountCents += toCents(product.getPrice()) * cartItem.getQuantity();
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(BigDecimal.valueOf(totalAmountCents).movePointLeft(2));
        order.setStatus(STATUS_WAIT_PAY);
        order.setAddress(dto.getAddress());
        orderMapper.insert(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
        }
        orderItemMapper.batchInsert(orderItems);
        cartItemMapper.deleteByIdsAndUserId(userId, dto.getCartItemIds());

        return toVO(orderMapper.selectById(order.getId()), orderItems, false);
    }

    public PageResult<OrderVO> listMy(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        int page = PageResult.normalizePage(pageNum);
        int size = PageResult.normalizeSize(pageSize);
        long total = orderMapper.countByUserId(userId, status);
        List<OrderVO> list = orderMapper.selectByUserId(userId, status, (page - 1) * size, size)
                .stream().map(order -> toVO(order, loadItems(order.getId()), false)).toList();
        return PageResult.of(list, total, page, size);
    }

    public OrderVO detailMy(Long userId, String orderNo) {
        Order order = requireOrder(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该订单");
        }
        return toVO(order, loadItems(order.getId()), false);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderVO pay(Long userId, String orderNo) {
        Order order = requireOwnOrder(orderNo, userId);
        if (order.getStatus() != STATUS_WAIT_PAY) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT, "当前订单状态不允许支付");
        }
        if (orderMapper.updateStatus(orderNo, STATUS_WAIT_PAY, STATUS_PAID) == 0) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT, "当前订单状态不允许支付");
        }
        return toVO(orderMapper.selectByOrderNo(orderNo), loadItems(order.getId()), false);
    }

    /** 取消待支付订单，并回补库存 */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, String orderNo) {
        Order order = requireOwnOrder(orderNo, userId);
        if (order.getStatus() != STATUS_WAIT_PAY) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT, "当前订单状态不允许取消");
        }
        if (orderMapper.updateStatus(orderNo, STATUS_WAIT_PAY, STATUS_CANCELED) == 0) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT, "当前订单状态不允许取消");
        }
        for (OrderItem item : loadItems(order.getId())) {
            productMapper.restoreStock(item.getProductId(), item.getQuantity());
        }
    }

    /** 确认收货：已发货 -> 已完成 */
    public void confirm(Long userId, String orderNo) {
        Order order = requireOwnOrder(orderNo, userId);
        if (order.getStatus() != STATUS_SHIPPED) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT, "当前订单状态不允许确认收货");
        }
        if (orderMapper.updateStatus(orderNo, STATUS_SHIPPED, STATUS_FINISHED) == 0) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT, "当前订单状态不允许确认收货");
        }
    }

    /** 后台：全部订单列表，额外填充下单用户名 */
    public PageResult<OrderVO> adminList(LoginUser operator, Integer status,
                                         Integer pageNum, Integer pageSize) {
        checkAdmin(operator);
        int page = PageResult.normalizePage(pageNum);
        int size = PageResult.normalizeSize(pageSize);
        long total = orderMapper.countAll(status);
        List<OrderVO> list = orderMapper.selectAll(status, (page - 1) * size, size)
                .stream().map(order -> toVO(order, loadItems(order.getId()), true)).toList();
        return PageResult.of(list, total, page, size);
    }

    public OrderVO adminDetail(LoginUser operator, String orderNo) {
        checkAdmin(operator);
        Order order = requireOrder(orderNo);
        return toVO(order, loadItems(order.getId()), true);
    }

    /** 后台：已支付订单标记为已发货 */
    public void ship(LoginUser operator, String orderNo) {
        checkAdmin(operator);
        Order order = requireOrder(orderNo);
        if (order.getStatus() != STATUS_PAID) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT, "当前订单状态不允许发货");
        }
        if (orderMapper.updateStatus(orderNo, STATUS_PAID, STATUS_SHIPPED) == 0) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT, "当前订单状态不允许发货");
        }
    }

    private void checkAdmin(LoginUser operator) {
        if (operator == null || !operator.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    /** 管理员账号不允许下单购买 */
    private void checkNotAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getRole() != null && user.getRole() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "管理员账号不能购买商品");
        }
    }

    private Order requireOrder(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private Order requireOwnOrder(String orderNo, Long userId) {
        Order order = requireOrder(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该订单");
        }
        return order;
    }

    private List<OrderItem> loadItems(Long orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }

    private OrderVO toVO(Order order, List<OrderItem> items, boolean withUser) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setTotalAmountCents(toCents(order.getTotalAmount()));
        vo.setStatus(order.getStatus());
        vo.setStatusText(statusText(order.getStatus()));
        vo.setAddress(order.getAddress());
        vo.setCreateTime(order.getCreateTime());

        List<OrderItemVO> itemVOs = new ArrayList<>();
        for (OrderItem item : items) {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setProductId(item.getProductId());
            itemVO.setProductName(item.getProductName());
            itemVO.setPriceCents(toCents(item.getProductPrice()));
            itemVO.setQuantity(item.getQuantity());
            itemVO.setSubtotalCents(toCents(item.getProductPrice()) * item.getQuantity());
            itemVOs.add(itemVO);
        }
        vo.setItems(itemVOs);

        if (withUser) {
            User user = userMapper.selectById(order.getUserId());
            vo.setUsername(user == null ? null : user.getUsername());
        }
        return vo;
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case STATUS_WAIT_PAY -> "待支付";
            case STATUS_PAID -> "已支付";
            case STATUS_SHIPPED -> "已发货";
            case STATUS_FINISHED -> "已完成";
            case STATUS_CANCELED -> "已取消";
            default -> "";
        };
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(ORDER_NO_FORMAT)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private int toCents(BigDecimal amount) {
        return amount == null ? 0 : amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
