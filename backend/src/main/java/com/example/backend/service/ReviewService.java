package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.ReviewCreateDTO;
import com.example.backend.entity.Order;
import com.example.backend.entity.OrderItem;
import com.example.backend.entity.Review;
import com.example.backend.entity.User;
import com.example.backend.mapper.OrderItemMapper;
import com.example.backend.mapper.OrderMapper;
import com.example.backend.mapper.ReviewMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评价服务：仅已完成订单中的商品可评价，同一订单同一商品只能评价一次。
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int STATUS_FINISHED = 3;

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;

    public ReviewVO create(Long userId, ReviewCreateDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权评价该订单");
        }
        if (order.getStatus() != STATUS_FINISHED) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT, "订单未完成，暂不能评价");
        }
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        boolean inOrder = items.stream()
                .anyMatch(item -> item.getProductId().equals(dto.getProductId()));
        if (!inOrder) {
            throw new BusinessException(ResultCode.NOT_FOUND, "该商品不在订单中");
        }
        if (reviewMapper.selectByOrderIdAndProductId(order.getId(), dto.getProductId()) != null) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT, "该商品已评价，请勿重复评价");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(dto.getProductId());
        review.setOrderId(order.getId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        reviewMapper.insert(review);

        Review saved = reviewMapper.selectByOrderIdAndProductId(order.getId(), dto.getProductId());
        ReviewVO vo = new ReviewVO();
        vo.setId(saved.getId());
        vo.setRating(saved.getRating());
        vo.setContent(saved.getContent());
        vo.setCreateTime(saved.getCreateTime());
        User user = userMapper.selectById(userId);
        vo.setUsername(user == null ? null : user.getUsername());
        return vo;
    }
}
