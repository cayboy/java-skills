package com.hit.effective.chapter5.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author:Charies Gavin
 * date:2018/6/8,8:43
 * https:github.com/guobinhit
 * description:自定义MoreExceptionTest注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MoreExceptionTest {
    Class<? extends Exception>[] value();
}
