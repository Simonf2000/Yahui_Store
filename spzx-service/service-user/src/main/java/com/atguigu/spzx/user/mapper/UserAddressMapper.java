package com.atguigu.spzx.user.mapper;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/20:42
 * @Description:
 */
import com.atguigu.spzx.model.entity.user.UserAddress;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserAddressMapper {

    List<UserAddress> findUserAddressList(Long userId);

    UserAddress getById(Long id);
}