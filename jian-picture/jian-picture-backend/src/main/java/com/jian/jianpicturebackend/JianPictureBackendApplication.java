package com.jian.jianpicturebackend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.jian.jianpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class JianPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JianPictureBackendApplication.class, args);
    }

}
