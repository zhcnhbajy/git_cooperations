package com.example.backend.mapper;

import com.example.backend.entity.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单明细表数据访问接口（注解方式）。
 */
@Mapper
public interface OrderItemMapper {

    String COLUMNS = "id, order_id, product_id, product_name, product_price, quantity";

    /** 批量写入明细，一次 SQL 完成 */
    @Insert("<script>"
            + "INSERT INTO order_item (order_id, product_id, product_name, product_price, quantity) VALUES "
            + "<foreach collection='items' item='item' separator=','>"
            + "(#{item.orderId}, #{item.productId}, #{item.productName}, #{item.productPrice}, #{item.quantity})"
            + "</foreach>"
            + "</script>")
    int batchInsert(@Param("items") List<OrderItem> items);

    @Select("SELECT " + COLUMNS + " FROM order_item WHERE order_id = #{orderId} ORDER BY id")
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
}
