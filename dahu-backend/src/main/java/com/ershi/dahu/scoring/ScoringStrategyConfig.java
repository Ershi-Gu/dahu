package com.ershi.dahu.scoring;


import com.ershi.dahu.model.enums.AppTypeEnum;
import com.ershi.dahu.model.enums.ScoringStrategyEnum;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component // 打上Component注解，所有使用该注解的类都会自动注入Spring
public @interface ScoringStrategyConfig {

    /**
     * 应用类别
     * @return int
     */
    AppTypeEnum appType();

    /**
     * 应用评分策略
     * @return int
     */
    ScoringStrategyEnum scoringStrategy();
}
