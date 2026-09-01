package com.jian.jianpicturebackend.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.jian.jianpicturebackend.constant.UserConstant;
import com.jian.jianpicturebackend.annotation.AuthCheck;
import com.jian.jianpicturebackend.common.BaseResponse;
import com.jian.jianpicturebackend.common.ResultUtils;
import com.jian.jianpicturebackend.config.CosClientConfig;
import com.jian.jianpicturebackend.exception.BusinessException;
import com.jian.jianpicturebackend.exception.ErrorCode;
import com.jian.jianpicturebackend.exception.ThrowUtils;
import com.jian.jianpicturebackend.manager.CosManager;
import com.jian.jianpicturebackend.model.entity.User;
import com.jian.jianpicturebackend.service.UserService;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private UserService userService;

    /**
     * 上传头像
     *
     * @param multipartFile 头像文件
     * @param request       请求
     * @return 头像访问地址
     */
    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        // 校验登录
        User loginUser = userService.getLoginUser(request);
        // 校验文件
        validAvatarFile(multipartFile);
        // 拼接文件路径：按用户隔离
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("avatar/%s/%s", loginUser.getId(), uploadFilename);
        File file = null;
        try {
            file = File.createTempFile(uuid, null);
            multipartFile.transferTo(file);
            cosManager.putObject(uploadPath, file);
            // 返回文件访问地址
            return ResultUtils.success(cosClientConfig.getHost() + "/" + uploadPath);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败");
        } finally {
            // 删除临时文件
            if (file != null && !file.delete()) {
                log.error("临时文件删除失败,path = {}", file.getAbsolutePath());
            }
        }
    }

    /**
     * 校验头像文件
     *
     * @param multipartFile 文件
     */
    private void validAvatarFile(MultipartFile multipartFile) {
        // 校验文件是否为空
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 10 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 10MB");
        // 校验文件类型
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("png", "jpg", "jpeg", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart MultipartFile multipartFile) {
        //文件目录
        //获取原始文件名
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("test/%s", filename);

        File file = null;
        //上传文件
        try {
            //创建空的临时文件
            file = File.createTempFile(filepath, null);
            //将前端文件存到临时文件
            multipartFile.transferTo(file);
            //上传到cos
            cosManager.putObject(filepath, file);
            //返回文件访问地址
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            //删除临时文件
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("文件删除失败,filepath = {}", filepath);
                }
            }
        }

    }

    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     * @throws IOException 异常
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/download")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInputStream = null;

        try {
            //获取文件
            COSObject cosObject = cosManager.getObject(filepath);
            //转为输入流
            cosObjectInputStream = cosObject.getObjectContent();
            //存入byte
            byte[] bytes = IOUtils.toByteArray(cosObjectInputStream);
            //设置响应头
            // 设置内容类型
            response.setContentType("application/octet-stream;charset=UTF-8");
            //设置文件名
            response.setHeader("Content-Disposition", "attachment;filename=" + filepath);
            //写入响应流
            response.getOutputStream().write(bytes);
            // 刷新输出流
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件下载失败");
        } finally {
            //释放流
            if (cosObjectInputStream != null) {
                cosObjectInputStream.close();
            }
        }
    }

}

