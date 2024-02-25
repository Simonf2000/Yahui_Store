package com.atguigu.spzx.manager.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/25/17:36
 * @Description:
 */
public interface FileUploadService {
    String fileUpload(MultipartFile multipartFile);
}
