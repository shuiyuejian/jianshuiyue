package com.jian.jianpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.jian.jianpicturebackend.exception.BusinessException;
import com.jian.jianpicturebackend.exception.ErrorCode;
import com.jian.jianpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Url图片上传
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件url不能为空");
        // 校验 URL 格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件url格式错误");
        }
        // 校验 URL 的协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "文件url协议错误");
        // 发送 HEAD 请求验证文件是否存在
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl)
                    .execute();
            // 未正常返回，无需执行其他判断
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }

            // 文件存在，校验文件类型
            String contentType = httpResponse.header("Content-Type");
            // 不为空，才校验是否合法，相对宽松
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/png", "image/jpeg", "image/jpg", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType), ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 文件存在，校验文件大小
            String contentLengthStr = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long MAX_FILE_SIZE = 10 * 1024 * 1024;
                    ThrowUtils.throwIf(contentLength > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件大小不能超过 10MB");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        } finally {
            // 释放资源
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        String name = FileUtil.getName(fileUrl);
        // 去除 URL 参数和锚点，防止 ? = 等非法字符进入文件名（Windows 下会导致临时文件创建失败）
        name = StrUtil.subBefore(name, "?", false);
        name = StrUtil.subBefore(name, "#", false);
        return name;
    }

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        String fileUrl = (String) inputSource;
        // 设置 30s 下载超时，避免阿里云临时 URL 卡住时无限等待（前端最多等 60s）
        HttpUtil.downloadFile(fileUrl, file, 30000);
    }
}

