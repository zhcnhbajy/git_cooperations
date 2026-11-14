package com.example.backend.service;

import com.example.backend.common.BusinessException;
import com.example.backend.common.LoginUser;
import com.example.backend.common.PageResult;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.ProductSaveDTO;
import com.example.backend.entity.Category;
import com.example.backend.entity.Product;
import com.example.backend.entity.Review;
import com.example.backend.entity.User;
import com.example.backend.mapper.CategoryMapper;
import com.example.backend.mapper.ProductMapper;
import com.example.backend.mapper.ReviewMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.vo.ProductDetailVO;
import com.example.backend.vo.ProductVO;
import com.example.backend.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品服务：前台浏览 + 后台商品管理。
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;

    /** 推荐商品，条数限制在 3~8 */
    public List<ProductVO> recommend(Integer limit) {
        int size = limit == null ? 6 : Math.min(8, Math.max(3, limit));
        return productMapper.selectRecommend(size).stream().map(this::toVO).toList();
    }

    /** 前台商品列表：只含已上架商品 */
    public PageResult<ProductVO> list(Long categoryId, Integer pageNum, Integer pageSize) {
        int page = PageResult.normalizePage(pageNum);
        int size = PageResult.normalizeSize(pageSize);
        List<Long> categoryIds = resolveCategoryIds(categoryId);
        if (categoryIds != null && categoryIds.isEmpty()) {
            return PageResult.of(List.of(), 0, page, size);
        }
        long total = productMapper.countOnSale(categoryIds);
        List<ProductVO> list = productMapper.selectOnSale(categoryIds, (page - 1) * size, size)
                .stream().map(this::toVO).toList();
        return PageResult.of(list, total, page, size);
    }

    /** 商品详情，含评价列表 */
    public ProductDetailVO detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException(ResultCode.STATUS_CONFLICT, "商品已下架");
        }
        ProductDetailVO vo = new ProductDetailVO();
        BeanUtils.copyProperties(toVO(product), vo);
        vo.setReviews(loadReviews(id));
        return vo;
    }

    /** 后台商品列表：包含已下架商品 */
    public PageResult<ProductVO> adminList(LoginUser operator, Long categoryId, Integer status,
                                           Integer pageNum, Integer pageSize) {
        checkAdmin(operator);
        int page = PageResult.normalizePage(pageNum);
        int size = PageResult.normalizeSize(pageSize);
        List<Long> categoryIds = resolveCategoryIds(categoryId);
        if (categoryIds != null && categoryIds.isEmpty()) {
            return PageResult.of(List.of(), 0, page, size);
        }
        long total = productMapper.countForAdmin(categoryIds, status);
        List<ProductVO> list = productMapper.selectForAdmin(categoryIds, status, (page - 1) * size, size)
                .stream().map(this::toVO).toList();
        return PageResult.of(list, total, page, size);
    }

    public ProductVO create(LoginUser operator, ProductSaveDTO dto) {
        checkAdmin(operator);
        Category category = requireSecondLevelCategory(dto.getCategoryId());
        Product product = buildProduct(null, dto, category);
        productMapper.insert(product);
        return toVO(productMapper.selectById(product.getId()));
    }

    public ProductVO update(LoginUser operator, Long id, ProductSaveDTO dto) {
        checkAdmin(operator);
        Product exists = productMapper.selectById(id);
        if (exists == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        Category category = requireSecondLevelCategory(dto.getCategoryId());
        Product product = buildProduct(id, dto, category);
        productMapper.update(product);
        return toVO(productMapper.selectById(id));
    }

    /** 上架 / 下架 */
    public ProductVO updateStatus(LoginUser operator, Long id, Integer status) {
        checkAdmin(operator);
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "商品状态只能是 0 或 1");
        }
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        productMapper.updateStatus(id, status);
        return toVO(productMapper.selectById(id));
    }

    private void checkAdmin(LoginUser operator) {
        if (operator == null || !operator.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private Category requireSecondLevelCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }
        if (category.getParentId() != null && category.getParentId() == 0L) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "商品必须挂在二级分类下");
        }
        return category;
    }

    private Product buildProduct(Long id, ProductSaveDTO dto, Category category) {
        if (dto.getStatus() != null && dto.getStatus() != 0 && dto.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "商品状态只能是 0 或 1");
        }
        Product product = new Product();
        product.setId(id);
        product.setCategoryId(category.getId());
        product.setName(dto.getName());
        product.setPrice(BigDecimal.valueOf(dto.getPriceCents()).movePointLeft(2));
        product.setStock(dto.getStock());
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        product.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        return product;
    }

    /**
     * 解析分类筛选条件：一级分类返回其全部二级分类 ID；二级分类返回自身。
     * 返回 null 表示不筛选；返回空集合表示分类不存在。
     */
    private List<Long> resolveCategoryIds(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            return List.of();
        }
        List<Category> children = categoryMapper.selectByParentId(categoryId);
        if (children.isEmpty()) {
            return List.of(categoryId);
        }
        return children.stream().map(Category::getId).toList();
    }

    private List<ReviewVO> loadReviews(Long productId) {
        List<Review> reviews = reviewMapper.selectByProductId(productId);
        List<ReviewVO> list = new ArrayList<>();
        for (Review review : reviews) {
            ReviewVO vo = new ReviewVO();
            vo.setId(review.getId());
            vo.setRating(review.getRating());
            vo.setContent(review.getContent());
            vo.setCreateTime(review.getCreateTime());
            User user = userMapper.selectById(review.getUserId());
            vo.setUsername(user == null ? "匿名用户" : user.getUsername());
            list.add(vo);
        }
        return list;
    }

    private ProductVO toVO(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setPriceCents(toCents(product.getPrice()));
        vo.setStock(product.getStock());
        vo.setCategoryId(product.getCategoryId());
        Category category = categoryMapper.selectById(product.getCategoryId());
        vo.setCategoryName(category == null ? null : category.getName());
        vo.setImageUrl(product.getImageUrl());
        vo.setDescription(product.getDescription());
        vo.setStatus(product.getStatus());
        vo.setCreateTime(product.getCreateTime());
        return vo;
    }

    private int toCents(BigDecimal amount) {
        return amount == null ? 0 : amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
