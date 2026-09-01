package com.jian.jianpicturebackend.aop;

import com.jian.jianpicturebackend.annotation.AuthCheck;
import com.jian.jianpicturebackend.exception.BusinessException;
import com.jian.jianpicturebackend.exception.ErrorCode;
import com.jian.jianpicturebackend.model.entity.User;
import com.jian.jianpicturebackend.model.enums.UserRoleEnum;
import com.jian.jianpicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 拦截所有需要权限的方法
     *
     * @param joinPoint 切入点
     * @param authCheck 权限注解
     * @return 方法执行结果
     * @throws Throwable 抛出异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        //获取接口的权限
        String mustRole = authCheck.mustRole();
        // 获取当前请求
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //无需权限放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 要求必须有管理员权限，但是用户没有权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //通过校验，放行
        return joinPoint.proceed();
    }
}
