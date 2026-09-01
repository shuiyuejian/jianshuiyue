package com.jian.jianpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jian.jianpicturebackend.model.dto.user.UserQueryRequest;
import com.jian.jianpicturebackend.model.dto.user.UserUpdateMyPasswordRequest;
import com.jian.jianpicturebackend.model.dto.user.UserUpdateMyRequest;
import com.jian.jianpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jian.jianpicturebackend.model.vo.LoginUserVO;
import com.jian.jianpicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author test
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2026-06-25 22:16:10
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取加密密码
     *
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    LoginUserVO getLoginUserVO(User user);


    /**
     * 获取脱敏的用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息列表
     *
     * @param userList 用户信息列表
     * @return 脱敏后的用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 是否注销成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 更新自己的资料（用户名、头像、简介）
     *
     * @param userUpdateMyRequest 更新请求
     * @param request             请求
     * @return 是否更新成功
     */
    boolean updateMyUser(UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request);

    /**
     * 修改自己的密码
     *
     * @param userUpdateMyPasswordRequest 修改密码请求
     * @param request                     请求
     * @return 是否修改成功
     */
    boolean updateMyPassword(UserUpdateMyPasswordRequest userUpdateMyPasswordRequest, HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否是管理员
     *
     * @param user 用户
     * @return 是否是管理员
     */
    boolean isAdmin(User user);
}
