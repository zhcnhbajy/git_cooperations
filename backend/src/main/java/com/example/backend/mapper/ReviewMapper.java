package com.example.backend.mapper;

import com.example.backend.entity.Review;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评价表数据访问接口（注解方式）。
 */
@Mapper
public interface ReviewMapper {

    String COLUMNS = "id, user_id, product_id, order_id, rating, content, create_time";

    @Select("SELECT " + COLUMNS + " FROM review WHERE product_id = #{productId} "
            + "ORDER BY create_time DESC, id DESC")
    List<Review> selectByProductId(@Param("productId") Long productId);

    /** 用于校验同一订单同一商品是否已评价 */
    @Select("SELECT " + COLUMNS + " FROM review WHERE order_id = #{orderId} AND product_id = #{productId}")
    Review selectByOrderIdAndProductId(@Param("orderId") Long orderId,
                                       @Param("productId") Long productId);

    @Insert("INSERT INTO review (user_id, product_id, order_id, rating, content, create_time) "
            + "VALUES (#{userId}, #{productId}, #{orderId}, #{rating}, #{content}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Review review);
}
