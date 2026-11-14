package com.example.backend.mapper;

import com.example.backend.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 订单表数据访问接口（注解方式）。
 */
@Mapper
public interface OrderMapper {

    String COLUMNS = "id, order_no, user_id, total_amount, status, address, create_time";

    @Select("SELECT " + COLUMNS + " FROM orders WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT " + COLUMNS + " FROM orders WHERE id = #{id}")
    Order selectById(@Param("id") Long id);

    @Select("SELECT " + COLUMNS + " FROM orders WHERE order_no = #{orderNo} AND user_id = #{userId}")
    Order selectByOrderNoAndUserId(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    @Select("<script>"
            + "SELECT " + COLUMNS + " FROM orders WHERE user_id = #{userId} "
            + "<if test='status != null'>AND status = #{status} </if>"
            + "ORDER BY create_time DESC, id DESC LIMIT #{offset}, #{limit}"
            + "</script>")
    List<Order> selectByUserId(@Param("userId") Long userId,
                               @Param("status") Integer status,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    @Select("<script>"
            + "SELECT COUNT(*) FROM orders WHERE user_id = #{userId} "
            + "<if test='status != null'>AND status = #{status} </if>"
            + "</script>")
    long countByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /** 后台：查询全部用户订单，status 为 null 时不筛选 */
    @Select("<script>"
            + "SELECT " + COLUMNS + " FROM orders WHERE 1 = 1 "
            + "<if test='status != null'>AND status = #{status} </if>"
            + "ORDER BY create_time DESC, id DESC LIMIT #{offset}, #{limit}"
            + "</script>")
    List<Order> selectAll(@Param("status") Integer status,
                          @Param("offset") int offset,
                          @Param("limit") int limit);

    @Select("<script>"
            + "SELECT COUNT(*) FROM orders WHERE 1 = 1 "
            + "<if test='status != null'>AND status = #{status} </if>"
            + "</script>")
    long countAll(@Param("status") Integer status);

    @Insert("INSERT INTO orders (order_no, user_id, total_amount, status, address, create_time) "
            + "VALUES (#{orderNo}, #{userId}, #{totalAmount}, #{status}, #{address}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    /**
     * 状态流转：带原状态条件更新，返回 0 表示状态已被改变，当前操作不允许。
     */
    @Update("UPDATE orders SET status = #{toStatus} WHERE order_no = #{orderNo} AND status = #{fromStatus}")
    int updateStatus(@Param("orderNo") String orderNo,
                     @Param("fromStatus") Integer fromStatus,
                     @Param("toStatus") Integer toStatus);
}
