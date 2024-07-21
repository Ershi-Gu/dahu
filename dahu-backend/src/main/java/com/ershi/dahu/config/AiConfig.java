package com.ershi.dahu.config;

import com.zhipu.oapi.ClientV4;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@ConfigurationProperties("ai")
@Data
public class AiConfig {


    /**
     *apiKey，从开放平台获取
     */
    private String apiKey;

    @Bean
    public ClientV4 getClientV4(){
        return new ClientV4.Builder(apiKey).build();
    }
}
