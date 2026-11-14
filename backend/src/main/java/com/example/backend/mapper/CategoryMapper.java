package com.example.backend.mapper;

import com.example.backend.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类表数据访问接口（注解方式）。
 */
@Mapper
public interface CategoryMapper {

    @Select("SELECT id, name, parent_id, sort_order FROM category ORDER BY parent_id, sort_order, id")
    List<Category> selectAll();

    @Select("SELECT id, name, parent_id, sort_order FROM category "
            + "WHERE parent_id = #{parentId} ORDER BY sort_order, id")
    List<Category> selectByParentId(@Param("parentId") Long parentId);

    @Select("SELECT id, name, parent_id, sort_order FROM category WHERE id = #{id}")
    Category selectById(@Param("id") Long id);
}
