package com.atguigu.spzx.user.mapper;

import com.atguigu.spzx.model.entity.user.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/11:40
 * @Description:
 */
@Mapper
public interface UserInfoMapper {
    UserInfo getUserInfoByUserName(String username);

    void insert(UserInfo userInfo);
}
