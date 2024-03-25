package com.atguigu.spzx.user.service;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/20:41
 * @Description:
 */
import com.atguigu.spzx.model.entity.user.UserAddress;
import java.util.List;

public interface UserAddressService {
    List<UserAddress> findUserAddressList();

    UserAddress getById(Long id);
}