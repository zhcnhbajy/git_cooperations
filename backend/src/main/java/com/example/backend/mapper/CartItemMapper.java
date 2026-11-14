package com.example.backend.mapper;

import com.example.backend.entity.CartItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 购物车表数据访问接口（注解方式）。
 */
@Mapper
public interface CartItemMapper {

    String COLUMNS = "id, user_id, product_id, quantity, create_time";

    @Select("SELECT " + COLUMNS + " FROM cart_item WHERE user_id = #{userId} ORDER BY id DESC")
    List<CartItem> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT " + COLUMNS + " FROM cart_item WHERE id = #{id}")
    CartItem selectById(@Param("id") Long id);

    @Select("SELECT " + COLUMNS + " FROM cart_item WHERE id = #{id} AND user_id = #{userId}")
    CartItem selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT " + COLUMNS + " FROM cart_item WHERE user_id = #{userId} AND product_id = #{productId}")
    CartItem selectByUserIdAndProductId(@Param("userId") Long userId,
                                        @Param("productId") Long productId);

    @Insert("INSERT INTO cart_item (user_id, product_id, quantity, create_time) "
            + "VALUES (#{userId}, #{productId}, #{quantity}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CartItem cartItem);

    /** 同一商品重复加入时累加数量 */
    @Update("UPDATE cart_item SET quantity = quantity + #{quantity} WHERE id = #{id}")
    int increaseQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    @Update("UPDATE cart_item SET quantity = #{quantity} WHERE id = #{id} AND user_id = #{userId}")
    int updateQuantity(@Param("id") Long id,
                       @Param("userId") Long userId,
                       @Param("quantity") int quantity);

    @Delete("DELETE FROM cart_item WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM cart_item WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /** 下单成功后删除已结算的购物车行 */
    @Delete("<script>"
            + "DELETE FROM cart_item WHERE user_id = #{userId} AND id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    int deleteByIdsAndUserId(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}
