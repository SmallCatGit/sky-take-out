package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口(文件上传)
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        log.info("文件上传:{}", file);
        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 获取文件名后缀所在的索引
            int suffix;
            String newFileName;
            if (originalFilename != null && !originalFilename.equals(" ")) {
                suffix = originalFilename.lastIndexOf(".");
                // 截取文件后缀,包含.
                String objectName = originalFilename.substring(suffix);
                // 构造新文件
                newFileName = UUID.randomUUID().toString() + objectName;
            }else return Result.error("文件不存在");
            String filePath = aliOssUtil.upload(file.getBytes(), newFileName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败:{}", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
