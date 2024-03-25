package com.atguigu.spzx.user.service.impl;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/20:41
 * @Description:
 */
import com.atguigu.spzx.common.util.AuthContextUtil;
import com.atguigu.spzx.model.entity.user.UserAddress;
import com.atguigu.spzx.model.entity.user.UserInfo;
import com.atguigu.spzx.user.mapper.UserAddressMapper;
import com.atguigu.spzx.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> findUserAddressList() {
        UserInfo userInfo = AuthContextUtil.getUserInfo();
        List<UserAddress> userAddressList = userAddressMapper.findUserAddressList(userInfo.getId());
        return userAddressList;
    }

    @Override
    public UserAddress getById(Long id) {
        return userAddressMapper.getById(id);
    }
}