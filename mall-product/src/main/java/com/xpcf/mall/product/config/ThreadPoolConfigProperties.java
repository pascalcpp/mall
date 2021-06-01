package com.xpcf.mall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @author XPCF
 * @version 1.0
 * @date 1/18/2021 3:13 AM
 */
@ConfigurationProperties(prefix = "mall.thread")
//@Component
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
