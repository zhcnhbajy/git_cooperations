package com.example.backend.mapper;

import com.example.backend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户表数据访问接口（注解方式）。
 */
@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password, role, create_time FROM `user` WHERE id = #{id}")
    User selectById(@Param("id") Long id);

    @Select("SELECT id, username, password, role, create_time FROM `user` WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    @Insert("INSERT INTO `user` (username, password, role, create_time) "
            + "VALUES (#{username}, #{password}, #{role}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}
