package com.example.backend.mapper;

import com.example.backend.entity.Product;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商品表数据访问接口（注解方式）。
 */
@Mapper
public interface ProductMapper {

    String COLUMNS = "id, category_id, name, price, stock, description, image_url, status, create_time";

    @Select("SELECT " + COLUMNS + " FROM product WHERE id = #{id}")
    Product selectById(@Param("id") Long id);

    /** 前台商品列表：只查已上架商品，categoryIds 为空表示不按分类筛选 */
    @Select("<script>"
            + "SELECT " + COLUMNS + " FROM product WHERE status = 1 "
            + "<if test='categoryIds != null and categoryIds.size() > 0'>"
            + "AND category_id IN "
            + "<foreach collection='categoryIds' item='cid' open='(' separator=',' close=')'>#{cid}</foreach>"
            + "</if>"
            + " ORDER BY id DESC LIMIT #{offset}, #{limit}"
            + "</script>")
    List<Product> selectOnSale(@Param("categoryIds") List<Long> categoryIds,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    /** 前台商品列表总数，与 selectOnSale 条件一致 */
    @Select("<script>"
            + "SELECT COUNT(*) FROM product WHERE status = 1 "
            + "<if test='categoryIds != null and categoryIds.size() > 0'>"
            + "AND category_id IN "
            + "<foreach collection='categoryIds' item='cid' open='(' separator=',' close=')'>#{cid}</foreach>"
            + "</if>"
            + "</script>")
    long countOnSale(@Param("categoryIds") List<Long> categoryIds);

    /** 推荐商品：已上架、有库存，按创建时间倒序 */
    @Select("SELECT " + COLUMNS + " FROM product WHERE status = 1 AND stock > 0 "
            + "ORDER BY create_time DESC, id DESC LIMIT #{limit}")
    List<Product> selectRecommend(@Param("limit") int limit);

    /** 后台商品列表：包含已下架商品，status 为 null 时不筛选状态 */
    @Select("<script>"
            + "SELECT " + COLUMNS + " FROM product WHERE 1 = 1 "
            + "<if test='status != null'>AND status = #{status} </if>"
            + "<if test='categoryIds != null and categoryIds.size() > 0'>"
            + "AND category_id IN "
            + "<foreach collection='categoryIds' item='cid' open='(' separator=',' close=')'>#{cid}</foreach>"
            + "</if>"
            + " ORDER BY id DESC LIMIT #{offset}, #{limit}"
            + "</script>")
    List<Product> selectForAdmin(@Param("categoryIds") List<Long> categoryIds,
                                 @Param("status") Integer status,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);

    @Select("<script>"
            + "SELECT COUNT(*) FROM product WHERE 1 = 1 "
            + "<if test='status != null'>AND status = #{status} </if>"
            + "<if test='categoryIds != null and categoryIds.size() > 0'>"
            + "AND category_id IN "
            + "<foreach collection='categoryIds' item='cid' open='(' separator=',' close=')'>#{cid}</foreach>"
            + "</if>"
            + "</script>")
    long countForAdmin(@Param("categoryIds") List<Long> categoryIds,
                       @Param("status") Integer status);

    /**
     * 扣减库存：带条件更新，返回 0 表示库存不足或商品已下架。
     */
    @Update("UPDATE product SET stock = stock - #{quantity} "
            + "WHERE id = #{id} AND status = 1 AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") int quantity);

    /** 取消订单时回补库存 */
    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{id}")
    int restoreStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Insert("INSERT INTO product (category_id, name, price, stock, description, image_url, status, create_time) "
            + "VALUES (#{categoryId}, #{name}, #{price}, #{stock}, #{description}, #{imageUrl}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("UPDATE product SET category_id = #{categoryId}, name = #{name}, price = #{price}, "
            + "stock = #{stock}, description = #{description}, image_url = #{imageUrl}, status = #{status} "
            + "WHERE id = #{id}")
    int update(Product product);

    @Update("UPDATE product SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
