package com.ershi.dahu.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ScoringStrategyEnum {

    CUSTOM("自定义", 0),
    AI("AI", 1);

    private final String text;

    private final Integer value;

    ScoringStrategyEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ScoringStrategyEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ScoringStrategyEnum anEnum : ScoringStrategyEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }


    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public String getText() {
        return text;
    }

    public Integer getValue() {
        return value;
    }
}
