package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.CartAddDTO;
import com.example.backend.entity.CartItem;
import com.example.backend.entity.Product;
import com.example.backend.entity.User;
import com.example.backend.mapper.CartItemMapper;
import com.example.backend.mapper.ProductMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.vo.CartItemVO;
import com.example.backend.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务，所有操作均以当前登录用户为范围。
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private static final String OFF_SHELF = "OFF_SHELF";
    private static final String OUT_OF_STOCK = "OUT_OF_STOCK";

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    public CartVO list(Long userId) {
        List<CartItem> cartItems = cartItemMapper.selectByUserId(userId);
        List<CartItemVO> items = new ArrayList<>();
        int totalAmountCents = 0;
        int totalQuantity = 0;
        for (CartItem cartItem : cartItems) {
            CartItemVO vo = toVO(cartItem);
            items.add(vo);
            if (Boolean.TRUE.equals(vo.getAvailable())) {
                totalAmountCents += vo.getPriceCents() * vo.getQuantity();
                totalQuantity += vo.getQuantity();
            }
        }
        CartVO vo = new CartVO();
        vo.setItems(items);
        vo.setTotalAmountCents(totalAmountCents);
        vo.setTotalQuantity(totalQuantity);
        return vo;
    }

    public CartItemVO add(Long userId, CartAddDTO dto) {
        checkNotAdmin(userId);
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT, "商品已下架，无法加入购物车");
        }
        CartItem exists = cartItemMapper.selectByUserIdAndProductId(userId, dto.getProductId());
        if (exists != null) {
            int newQuantity = exists.getQuantity() + dto.getQuantity();
            if (newQuantity > product.getStock()) {
                throw stockError(product);
            }
            cartItemMapper.increaseQuantity(exists.getId(), dto.getQuantity());
            return toVO(cartItemMapper.selectById(exists.getId()));
        }
        if (dto.getQuantity() > product.getStock()) {
            throw stockError(product);
        }
        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setProductId(product.getId());
        cartItem.setQuantity(dto.getQuantity());
        cartItemMapper.insert(cartItem);
        return toVO(cartItemMapper.selectById(cartItem.getId()));
    }

    public CartItemVO updateQuantity(Long userId, Long id, Integer quantity) {
        CartItem cartItem = requireOwnedItem(userId, id);
        Product product = productMapper.selectById(cartItem.getProductId());
        if (product != null && quantity > product.getStock()) {
            throw stockError(product);
        }
        cartItemMapper.updateQuantity(id, userId, quantity);
        return toVO(cartItemMapper.selectById(id));
    }

    public void remove(Long userId, Long id) {
        requireOwnedItem(userId, id);
        cartItemMapper.deleteByIdAndUserId(id, userId);
    }

    public void clear(Long userId) {
        cartItemMapper.deleteByUserId(userId);
    }

    /** 管理员账号不允许加购物车（不能下单购买） */
    private void checkNotAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getRole() != null && user.getRole() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "管理员账号不能购买商品");
        }
    }

    private CartItem requireOwnedItem(Long userId, Long id) {
        CartItem cartItem = cartItemMapper.selectById(id);
        if (cartItem == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "购物车项不存在");
        }
        if (!cartItem.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该购物车项");
        }
        return cartItem;
    }

    private BusinessException stockError(Product product) {
        return new BusinessException(ResultCode.STOCK_NOT_ENOUGH,
                "库存不足，当前库存 " + product.getStock());
    }

    private CartItemVO toVO(CartItem cartItem) {
        CartItemVO vo = new CartItemVO();
        vo.setId(cartItem.getId());
        vo.setProductId(cartItem.getProductId());
        vo.setQuantity(cartItem.getQuantity());

        Product product = productMapper.selectById(cartItem.getProductId());
        if (product == null) {
            vo.setProductName("商品已删除");
            vo.setPriceCents(0);
            vo.setStock(0);
            vo.setAvailable(false);
            vo.setInvalidReason(OFF_SHELF);
            return vo;
        }
        vo.setProductName(product.getName());
        vo.setPriceCents(toCents(product.getPrice()));
        vo.setStock(product.getStock());
        vo.setImageUrl(product.getImageUrl());

        boolean onSale = product.getStatus() != null && product.getStatus() == 1;
        boolean enough = product.getStock() >= cartItem.getQuantity();
        vo.setAvailable(onSale && enough);
        if (!onSale) {
            vo.setInvalidReason(OFF_SHELF);
        } else if (!enough) {
            vo.setInvalidReason(OUT_OF_STOCK);
        }
        return vo;
    }

    private int toCents(BigDecimal amount) {
        return amount == null ? 0 : amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
