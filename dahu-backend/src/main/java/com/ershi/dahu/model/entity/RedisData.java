package com.ershi.dahu.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 存在Redis中的对象（包含逻辑过期）
 * @author Ershi
 * @date 2024/08/01
 */
@Data
public class RedisData {

    private LocalDateTime expireTime;

    private Object data;
}
