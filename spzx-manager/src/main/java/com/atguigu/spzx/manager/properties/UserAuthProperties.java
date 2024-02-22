package com.atguigu.spzx.manager.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/22/19:31
 * @Description:
 */
@Data
@ConfigurationProperties(prefix = "spzx.auth")      // 前缀不能使用驼峰命名
public class UserAuthProperties {
    private List<String> noAuthUrls ;
}