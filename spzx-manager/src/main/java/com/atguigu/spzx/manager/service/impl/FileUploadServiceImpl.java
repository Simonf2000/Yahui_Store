package com.atguigu.spzx.manager.service.impl;

import cn.hutool.core.date.DateUtil;
import com.atguigu.spzx.manager.properties.MinioProperties;
import com.atguigu.spzx.manager.service.FileUploadService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/25/17:38
 * @Description:
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    MinioProperties minioProperties;
    @Override
    public String fileUpload(MultipartFile multipartFile) {
        try {
            // 创建一个Minio的客户端对象
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minioProperties.getEndpointUrl())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();

            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());

            // 如果不存在，那么此时就创建一个新的桶
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
            } else {  // 如果存在打印信息
                System.out.println("Bucket '"+minioProperties+"' already exists.");
            }

            String replace = UUID.randomUUID().toString().replace("-", "");

            InputStream fis = multipartFile.getInputStream();
            String originalFilename = multipartFile.getOriginalFilename();

            String dateDir = DateUtil.format(new Date(), "yyyyMMdd");

            String newFileName = dateDir + "/" + replace + "_" + originalFilename;

            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .stream(fis, fis.available(), -1)
                    .object(newFileName)
                    .build();
            minioClient.putObject(putObjectArgs) ;

            // 构建fileUrl
            String fileUrl = minioProperties.getEndpointUrl()+"/"+minioProperties.getBucketName()+"/"+newFileName;
            System.out.println(fileUrl);
            return fileUrl;
        } catch (Exception e) {
           e.printStackTrace();
        }
        return null;
    }
}
