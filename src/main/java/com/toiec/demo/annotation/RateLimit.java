package com.toiec.demo.annotation;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    String name() default "";
    int capacity() default 20;          // số request tối đa
    int refillTokens() default 10;      // token thêm mỗi phút
    int refillPeriodMinutes() default 1;
}